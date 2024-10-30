package engine.entity.core

import engine.Inventory
import debug.Debug
import debug.DebugMessageType
import engine.Message
import engine.Messages
import engine.entity.attributes.EntityAttributes
import engine.entity.attributes.EntityClass
import engine.entity.attributes.EntityNames
import engine.entity.attributes.EntityPosture
import engine.entity.behavior.EntityAction
import engine.entity.behavior.EntityBehavior
import engine.entity.behavior.EntitySituation
import engine.entity.behavior.FlavorText
import engine.entity.body.EntityBody
import engine.entity.body.EntityBodyPart
import engine.entity.faction.EntityFaction
import engine.entity.faction.EntityFactions
import engine.game.Game
import engine.game.MovementDirection
import engine.item.*
import engine.magic.Spell
import engine.magic.SpellEffect
import engine.magic.SpellEffectType
import engine.magic.Spells
import engine.utility.Common.collectionString
import engine.utility.appendLine
import engine.utility.Common.d100
import engine.world.Connection
import engine.world.Room
import engine.world.World

abstract class EntityBase(
    val faction: EntityFaction,
    val name: String,
    var level: Int,
    val keywords: List<String>,
    val attributes: EntityAttributes,
    val experience: Int,
    val gold: Int,
    val behavior: EntityBehavior,
    val actionDelayMin: Int,
    val actionDelayMax: Int,
    val body: EntityBody,
    val entityClass: EntityClass,
    var weapon: ItemWeapon? = null,
    val inventory: Inventory,
    private val unequippedWeaponString: String = "fists",
    val spells: MutableList<String> = mutableListOf()
) {
    abstract val canTravelBetweenRegions: Boolean

    val damageSpells: List<Spell>
        get() = spells.map { spell -> Spells[spell] }.filter { it.isDamageSpell() }

    private var currentRoom: Room = World.void
    private var posture: EntityPosture = EntityPosture.STANDING
    var hasNotBeenSearched = true
    val isDead
        get() = attributes.currentHealth <= 0
    val isAlive
        get() = !isDead
    val coordinates
        get() = currentRoom.coordinates

    // region names
    abstract val names: EntityNames

    // "..., a goblin (kneeling), ..."
    val nameForCollectionString
        get() = when {
            isDead -> names.dead
            posture == EntityPosture.KNEELING -> names.kneeling
            posture == EntityPosture.SITTING -> names.sitting
            else -> names.full
        }
    // endregion

    fun isInjuredMinor() =
        attributes.isInjuredMinor()

    fun isInjuredModerate() =
        attributes.isInjuredModerate()

    fun isInjuredMajor() =
        attributes.isInjuredMajor()

    // region strings
    fun getString(item: ItemBase) =
        Messages.get(Message.ENTITY_PICKS_UP_ITEM, names.prefixedRandom(), item.nameWithIndefiniteArticle)

    fun dropString(item: ItemBase) =
        Messages.get(Message.ENTITY_DROPS_ITEM, names.prefixedRandom(), item.nameWithIndefiniteArticle)

    fun unequipAndDropString(item: ItemBase) =
        Messages.get(Message.ENTITY_UNEQUIPS_AND_DROPS_ITEM, names.prefixedRandom(), item.nameWithIndefiniteArticle)

    fun pickUpAndEquip(item: ItemBase) =
        Messages.get(Message.ENTITY_PICKS_UP_AND_EQUIPS_ITEM, names.prefixedRandom(), item.nameWithIndefiniteArticle)

    fun dropString(inventory: Inventory) =
        Messages.get(Message.ENTITY_DROPS_ITEM, names.prefixedRandom(), inventory.collectionString)

    fun destroyString(item: ItemBase) =
        Messages.get(Message.ENTITY_DESTROYS_ITEM, names.prefixedRandom(), item.nameWithIndefiniteArticle)

    val arriveString
        get() = Messages.get(Message.ENTITY_ARRIVES, names.arrive, names.arriveSuffix)
    val deathString
        get() = Messages.get(Message.ENTITY_DIES, names.capitalizedPrefixedDeath)
    val sitString
        get() = Messages.get(Message.ENTITY_SITS, names.capitalizedPrefixedRandom())
    val standString
        get() = Messages.get(Message.ENTITY_STANDS_UP, names.capitalizedPrefixedRandom())
    val kneelString
        get() = Messages.get(Message.ENTITY_KNEELS, names.capitalizedPrefixedRandom())

    // entity weapon
    val weaponString
        get() = weapon?.name ?: unequippedWeaponString


    fun departString(connection: Connection? = null) =
        if (connection == null) {
            Messages.get(Message.ENTITY_LEAVES_GAME, names.prefixedRandom())
        } else if (connection.direction != MovementDirection.NONE) {
            // The goblin heads east.
            // "$names.prefixedRandom() heads ${connection.direction.toString().lowercase()}."
            Messages.get(
                Message.ENTITY_HEADS_DIRECTION,
                names.prefixedRandom(),
                connection.direction.toString().lowercase()
            )
        } else if (connection.matchInputString.contains("gates")) {
            // TODO: make this better
            // TODO: other cases for climbing, other connection types
            // TODO: The goblin heads through the gates.
            // "$names.prefixedRandom() heads through the town gates."
            Messages.get(Message.ENTITY_HEADS_THROUGH_THE_TOWN_GATES, names.prefixedRandom())
        } else if (connection.matchInputString.contains("path")) {
            Messages.get(Message.ENTITY_HEADS_DOWN_THE_DIRT_PATH, names.prefixedRandom())
        } else {
            // TODO: make this better
            // "$names.prefixedRandom() heads over to the ${connection.matchInput.suffix}."
            Messages.get(
                Message.ENTITY_HEADS_OVER_TO_THE_CONNECTION,
                names.prefixedRandom(),
                connection.matchInput.suffix
            )
        }

    fun putAwayString(item: ItemBase) =
        Messages.get(Message.ENTITY_PUTS_AWAY_ITEM, names.prefixedRandom(), item.name)

    fun equipString(item: ItemBase) =
        Messages.get(Message.ENTITY_EQUIPS_ITEM, names.prefixedRandom(), item.nameWithIndefiniteArticle)

    fun removeString(item: ItemBase) =
        Messages.get(Message.ENTITY_REMOVES_ITEM, names.prefixedRandom(), item.name)
    // endregion

    // region core
    private fun canCastSpellWithEffectType(effectType: SpellEffectType): Boolean {
        val canICastThis = spells.any {
            with(Spells[it]) {
                hasEffectType(effectType) && hasEnoughMagicToCast(this)
            }
        }

        Debug.println("Do I have $effectType: $canICastThis", DebugMessageType.ENTITY_CHECK_FOR_MAGIC_EFFECT)
        return canICastThis
    }

    private fun hasEnoughMagicToCast(spell: Spell) =
        attributes.currentMagic >= spell.cost

    protected fun doAction() =
        if (isDead) {
            doIsDead()
        } else {
            val action = behavior.getNextAction(this)
            doAction(action)
        }

    suspend fun doDelay() =
    // TODO: make this based off of something else
    //  e.g. entity speed, type
        // Debug.println("EntityMonster::doDelay()")
        Game.delayRandom(
            min = actionDelayMin, max = actionDelayMax,
            conditions = listOf(
                ::hasNotBeenSearched
            )
        )

    fun matchesKeyword(keyword: String) =
        (name == keyword) || keywords.contains(keyword)
    // endregion

    // region hostilities
    val hostilesInCurrentRoom
        get() = currentRoom.entities.filter { isHostileTo(it) }
    val livingHostilesInCurrentRoom
        get() = hostilesInCurrentRoom.filter { it.isAlive }
    val deadHostilesInCurrentRoom
        get() = hostilesInCurrentRoom.filter { it.isDead }
    val deadAndUnsearchedHostilesInCurrentRoom
        get() = hostilesInCurrentRoom.filter { it.isDead && it.hasNotBeenSearched }
    val allHostilesCount
        get() = hostilesInCurrentRoom.size
    val livingHostilesCount: Int
        get() = livingHostilesInCurrentRoom.size
    val deadAndUnsearchedHostilesCount
        get() = deadAndUnsearchedHostilesInCurrentRoom.size

    fun isHostileTo(otherEntity: EntityBase) =
        faction.isHostileTo(otherEntity.faction)

    fun isHostileTo(otherFaction: EntityFactions) =
        faction.isHostileTo(otherFaction)

    fun doAttackPlayer() =
        if (posture != EntityPosture.STANDING) {
            doStand()
        } else {
            currentRoom.randomLivingPlayerOrNull()?.let { player ->
                // entity weapon
                val weaponString = weapon?.name ?: "fists"
                // entity attack
                val attack = attributes.strength + (weapon?.power ?: 0)
                // player defense
                val defense = player.attributes.baseDefense
                // resultant damage
                val damage = (attack - defense).coerceAtLeast(0)

                with(StringBuilder()) {
                    // TODO: quips at player
                    appendLine(Message.ENTITY_ATTACKS_PLAYER, names.prefixedFull, weaponString)

                    if (damage > 0) {
                        appendLine(Message.ENTITY_HITS_ENTITY_FOR_DAMAGE, damage.toString())
                    } else {
                        appendLine(Message.ENTITY_MISSES_ENTITY)
                    }

                    player.attributes.currentHealth -= damage
                    if (player.attributes.currentHealth <= 0) {
                        player.sendToMe(Message.PLAYER_DIES)
                        appendLine(Message.OTHER_PLAYER_DIES, player.name)
                    }

                    sendToAll(this)
                }
            }
        }

    fun doAttackRandomLivingHostile() =
        currentRoom.randomLivingHostileOrNull(faction)?.let { randomLivingHostile ->
            // entity attack
            val attack = calculateAttackPower()
            // hostile defense
            val defense = randomLivingHostile.attributes.baseDefense
            // resultant damage
            val damage = (attack - defense).coerceAtLeast(0)

            with(StringBuilder()) {
                d100(25) {
                    appendLine(getQuip(randomLivingHostile))
                }

                if (weapon != null) {
                    appendLine(
                        Message.ENTITY_ATTACKS_ENTITY_WEAPON,
                        names.prefixedFull,
                        randomLivingHostile.names.conversational,
                        weaponString
                    )
                } else {
                    appendLine(
                        Message.ENTITY_ATTACKS_ENTITY_NO_WEAPON,
                        names.prefixedFull,
                        randomLivingHostile.names.conversational
                    )
                }

                if (damage > 0) {
                    appendLine(Message.ENTITY_HITS_FOR_DAMAGE, damage.toString())
                } else {
                    appendLine(Message.ENTITY_MISSES)
                }

                randomLivingHostile.attributes.currentHealth -= damage
                if (randomLivingHostile.isDead) {
                    appendLine(randomLivingHostile.deathString)
                    randomLivingHostile.processDeath()
                }

                Debug.println(
                    "EntityBase::doAttackRandomLivingHostile()\n" +
                            "$coordinates - ${names.withJob} -> ${randomLivingHostile.names.withJob} " +
                            "[$damage damage, ${randomLivingHostile.attributes.currentHealth} health left]",
                    DebugMessageType.ENTITY_ATTACK
                )
                sendToAll(this)
            }
        }

    abstract fun calculateAttackPower(): Int
    abstract fun processDeath()

    fun doSearchRandomUnsearchedDeadHostile() =
        currentRoom.entities.filter { faction.isHostileTo(it.faction) && it.isDead && it.hasNotBeenSearched }
            .randomOrNull()?.let { deadHostile ->
                // build and announce a single drop string out of weapon, armor, and other inventory items
                val sb = StringBuilder()
                sb.appendLine(
                    Message.ENTITY_SEARCHES_DEAD_ENTITY,
                    names.capitalizedPrefixedFull,
                    deadHostile.names.conversational
                )

                // drop weapon
                deadHostile.weapon?.let {
                    currentRoom.addItem(it)
                    sb.appendLine(
                        Message.ENTITY_DROPS_ITEM,
                        deadHostile.names.prefixedFull,
                        it.nameWithIndefiniteArticle
                    )
                }

                // drop body armor
                var droppedArmor = mutableListOf<String>()
                deadHostile.body.parts.forEach { bodyPart ->
                    bodyPart.equippedItem?.let {
                        currentRoom.addItem(it)
                        droppedArmor.add(it.name)
                    }
                }
                if (droppedArmor.isNotEmpty()) {
                    sb.appendLine(
                        Message.ENTITY_DROPS_ITEM,
                        deadHostile.names.prefixedFull,
                        collectionString(droppedArmor, includeIndefiniteArticles = true)
                    )
                }

                // drop items
                // TODO: fix this to use Message
                if (deadHostile.inventory.isNotEmpty()) {
                    sb.appendLine(deadHostile.dropString(deadHostile.inventory))
                    currentRoom.addInventory(deadHostile.inventory)
                }

                Debug.println(
                    "EntityBase::doSearchRandomUnsearchedDeadHostile()\n" +
                            "$coordinates - ${names.withJob} - ${deadHostile.names.withJob}",
                    DebugMessageType.ENTITY_SEARCH
                )
                sendToAll(sb)

                // flag for cleanup
                deadHostile.hasNotBeenSearched = false
            }
    // endregion

    // region speak and say
    private fun doDeadSoloQuip() =
        sendToAll(
            Message.DEAD_ENTITY_QUIPS_SOLO,
            names.deadConversational,
            FlavorText.get(EntityAction.DEAD_QUIP_SOLO)
        )

    private fun doQuipToRandomEntity() =
        currentRoom.entities.randomOrNull()?.let {
            doQuipToEntity(it)
        }

    private fun doQuipToEntity(entity: EntityBase) =
        sendToAll(getQuip(entity))

    private fun getQuip(entity: EntityBase) =
        when {
            isAlive && entity.isAlive && isHostileTo(entity) -> getLivingToLivingHostileQuip(entity)
            isAlive && entity.isAlive -> getLivingToLivingFriendlyQuip(entity)
            isAlive && entity.isDead && isHostileTo(entity) -> getLivingToDeadHostileQuip(entity)
            isAlive && entity.isDead -> getLivingToDeadFriendlyQuip(entity)

            isDead && entity.isAlive && isHostileTo(entity) -> getDeadToLivingHostileQuip(entity)
            isDead && entity.isAlive -> getDeadToLivingFriendlyQuip(entity)
            isDead && entity.isDead && isHostileTo(entity) -> getDeadToDeadHostileQuip(entity)
            isDead && entity.isDead -> getDeadToDeadFriendlyQuip(entity)

            else -> ""
        }

    private fun getDeadToDeadHostileQuip(deadHostileEntity: EntityBase) =
        Messages.get(
            Message.ENTITY_SAYS_TO_ENTITY,
            names.prefixedRandom(),
            deadHostileEntity.names.conversational,
            FlavorText.get(EntityAction.DEAD_ENTITY_SAYS_TO_DEAD_HOSTILE_ENTITY)
        )

    private fun getDeadToLivingHostileQuip(livingHostileEntity: EntityBase) =
        Messages.get(
            Message.ENTITY_SAYS_TO_ENTITY,
            names.prefixedRandom(),
            livingHostileEntity.names.conversational,
            FlavorText.get(EntityAction.DEAD_ENTITY_SAYS_TO_LIVING_HOSTILE_ENTITY)
        )

    private fun getLivingToDeadHostileQuip(deadHostileEntity: EntityBase) =
        Messages.get(
            Message.ENTITY_SAYS_TO_ENTITY,
            names.prefixedRandom(),
            deadHostileEntity.names.conversational,
            FlavorText.get(EntityAction.LIVING_ENTITY_SAYS_TO_DEAD_HOSTILE_ENTITY)
        )

    private fun getLivingToLivingHostileQuip(livingHostileEntity: EntityBase) =
        Messages.get(
            Message.ENTITY_SAYS_TO_ENTITY,
            names.prefixedRandom(),
            livingHostileEntity.names.conversational,
            FlavorText.get(EntityAction.LIVING_ENTITY_SAYS_TO_LIVING_HOSTILE_ENTITY)
        )

    private fun getLivingToLivingFriendlyQuip(livingFriendlyEntity: EntityBase) =
        if (livingFriendlyEntity == this) {
            // i'm alive and quipping to myself...
            // TODO: we can do better than this
            Messages.get(Message.ENTITY_MUMBLES, names.prefixedRandom())
        } else {
            Messages.get(
                Message.ENTITY_SAYS_TO_ENTITY,
                names.prefixedRandom(),
                livingFriendlyEntity.names.conversational,
                FlavorText.get(EntityAction.LIVING_ENTITY_SAYS_TO_LIVING_FRIENDLY_ENTITY)
            )
        }

    private fun getLivingToDeadFriendlyQuip(deadFriendlyEntity: EntityBase) =
        Messages.get(
            Message.ENTITY_SAYS_TO_ENTITY,
            names.prefixedRandom(),
            deadFriendlyEntity.names.deadConversational,
            FlavorText.get(EntityAction.LIVING_ENTITY_SAYS_TO_DEAD_FRIENDLY_ENTITY)
        )

    private fun getDeadToDeadFriendlyQuip(deadFriendlyEntity: EntityBase) =
        if (deadFriendlyEntity == this) {
            // i'm dead and quipping to myself...
            Messages.get(
                Message.DEAD_ENTITY_QUIPS_SOLO,
                names.deadConversational,
                FlavorText.get(EntityAction.DEAD_QUIP_SOLO)
            )
        } else {
            Messages.get(
                Message.ENTITY_SAYS_TO_ENTITY,
                names.prefixedRandom(),
                deadFriendlyEntity.names.deadConversational,
                FlavorText.get(EntityAction.DEAD_ENTITY_SAYS_TO_DEAD_FRIENDLY_ENTITY)
            )
        }

    private fun getDeadToLivingFriendlyQuip(livingFriendlyEntity: EntityBase) =
        Messages.get(
            Message.ENTITY_SAYS_TO_ENTITY,
            names.prefixedRandom(),
            livingFriendlyEntity.names.random(),
            FlavorText.get(EntityAction.DEAD_ENTITY_SAYS_TO_LIVING_FRIENDLY_ENTITY)
        )

    fun doMumble() =
        sendToAll(Message.ENTITY_MUMBLES, names.prefixedRandom())

    abstract fun say(what: String): String

    fun sendToAll(sb: StringBuilder) =
        sendToAll(sb.trim('\n').toString())

    fun sendToAll(what: String) =
        currentRoom.sendToAll(what)

    fun sendToAll(message: Message, vararg tokens: String) =
        currentRoom.sendToAll(Messages.get(message, *tokens))
    // endregion

    // region get items
    private fun doGetValuableItem() =
        currentRoom.getAndRemoveRandomValuableItemOrNull()?.let { item ->
            with(StringBuilder()) {
                // TODO: this doesn't feel like a great solution
                //  goal for now - NPCs can talk; monsters can't
                val sayText = say(FlavorText.get(EntityAction.GET_VALUABLE_ITEM))
                if (sayText.isNotEmpty()) {
                    appendLine(sayText)
                }
                appendLine(getString(item))

                Debug.println(
                    "EntityBase::doGetValuableItem()\n" +
                            "$coordinates - ${names.withJob} - ${item.name}",
                    DebugMessageType.ENTITY_GET_VALUABLE_ITEM
                )
                sendToAll(this)
            }
        }


    private fun doGetAndRemoveRandomItem() =
        currentRoom.getAndRemoveRandomItemOrNull()?.let { item ->
            sendToAll(getString(item))
            item
        }

    private fun doRemoveRandomItem() =
        currentRoom.getAndRemoveRandomItemOrNull()?.let { item ->
            sendToAll(destroyString(item))
        }
    // endregion

    // region posture
    private fun doSit() {
        if (posture != EntityPosture.SITTING) {
            posture = EntityPosture.SITTING
            sendToAll(sitString)
        }
    }

    private fun doStand() {
        if (posture != EntityPosture.STANDING) {
            posture = EntityPosture.STANDING
            sendToAll(standString)
        }
    }

    private fun doKneel() {
        if (posture != EntityPosture.KNEELING) {
            posture = EntityPosture.KNEELING
            sendToAll(kneelString)
        }
    }
    // endregion

    // region non-hostile actions
    private fun doNothing() {}
    private fun doIsDead() {}
    private fun doAction(action: EntityAction) {
        when (action) {
            EntityAction.MOVE -> doRandomMove()
            EntityAction.SIT -> doSit()
            EntityAction.STAND -> doStand()
            EntityAction.KNEEL -> doKneel()
            EntityAction.GET_RANDOM_BETTER_WEAPON -> doGetRandomBetterWeapon()
            EntityAction.GET_RANDOM_BETTER_ARMOR -> doGetRandomBetterArmor()
            EntityAction.GET_RANDOM_ITEM -> doGetAndRemoveRandomItem()
            EntityAction.IDLE -> doIdle()
            EntityAction.IDLE_FLAVOR_ACTION -> doIdleFlavorAction()
            EntityAction.ATTACK_PLAYER -> doAttackPlayer()
            EntityAction.ATTACK_RANDOM_LIVING_HOSTILE -> doAttackRandomLivingHostile()
            EntityAction.SEARCH_RANDOM_UNSEARCHED_DEAD_HOSTILE -> doSearchRandomUnsearchedDeadHostile()
            EntityAction.FIND_AND_EQUIP_RANDOM_WEAPON -> doFindAndEquipRandomWeapon()
            EntityAction.FIND_AND_EQUIP_RANDOM_ARMOR -> doFindAndEquipRandomArmor()
            EntityAction.GET_VALUABLE_ITEM -> doGetValuableItem()
            EntityAction.GET_ANY_ITEM -> doGetAndRemoveRandomItem()
            EntityAction.DESTROY_ANY_ITEM -> doRemoveRandomItem()
            EntityAction.HEAL_SELF -> doNothing()
            EntityAction.LOOK -> doNothing()
            EntityAction.QUIP_TO_RANDOM_ENTITY -> doQuipToRandomEntity()
            EntityAction.EAT_RANDOM_FOOD -> doEatRandomFoodItem()
            EntityAction.DRINK_RANDOM_DRINK -> doDrinkRandomDrinkItem()
            EntityAction.HEAL_OTHER -> doHealOther()
            EntityAction.CAST_FIRE_AT_LIVING_HOSTILE -> doCastFireAtLivingHostile()
            EntityAction.CAST_DAMAGE_SPELL_AT_LIVING_HOSTILE -> doCastDamageSpellAtLivingHostile()
            else -> doNothing()
        }
    }

    private fun doCastDamageSpellAtLivingHostile() {
        currentRoom.randomLivingHostileOrNull(faction)?.let { randomLivingHostile ->
            doCastSpell(
                spell = damageSpells.random(),
                target = randomLivingHostile
            )
        }
    }

    private fun doCastFireAtLivingHostile() {
        currentRoom.randomLivingHostileOrNull(faction)?.let { randomLivingHostile ->
            doCastSpell(
                spell = Spells["minor fire"],
                target = randomLivingHostile
            )
        } // TODO: can this be null?
    }

    private fun doHealOther() =
        currentRoom.getRandomInjuredFriendlyOrNull(friend = this)?.let { randomInjuredFriendly ->
            doCastSpell(
                spell = Spells["minor heal"],
                target = randomInjuredFriendly
            )
        } // TODO: can this be null?


    private fun doCastSpell(spell: Spell, target: EntityBase? = null) =
        with(StringBuilder()) {
            appendLine(
                target?.let {
                    if (this@EntityBase == it) {
                        // targeting self
                        Messages.get(
                            // x casts spell on themselves
                            Message.ENTITY_CASTS_SPELL_ON_SELF,
                            names.capitalizedPrefixedRandom(),
                            spell.name
                        )
                    } else {
                        Messages.get(
                            // messaging: x casts spell on y
                            Message.ENTITY_CASTS_SPELL_ON_ENTITY,
                            names.capitalizedPrefixedRandom(),
                            spell.name,
                            target.names.prefixedRandom()
                        )
                    }
                } ?: Messages.get(
                    // TODO: no spells to test this on yet;
                    //  is this good enough?
                    //  what about when spells affect a room and/or multiple entities?
                    Message.ENTITY_CASTS_SPELL, names.capitalizedPrefixedRandom(), spell.name
                )
            )

            attributes.currentMagic -= spell.cost

            spell.effects.forEach { processSpellEffect(it, target, this) }

            if (target != null && target.isDead) {
                appendLine(target.deathString)
                target.processDeath()
            }

            target?.let {
                // TODO: make this better
                val strength = spell.effects.sumOf { effect -> effect.strength }

                Debug.println(
                    "EntityBase::doCastSpell()\n" +
                            "$coordinates - ${names.withJob} - ${spell.name} - ${it.names.withJob} " +
                            "[$strength strength, ${it.attributes.currentHealth} health left]",
                    DebugMessageType.ENTITY_CAST_SPELL
                )
            } ?: Debug.println("self")

            sendToAll(this)
        }

    private fun processSpellEffect(effect: SpellEffect, target: EntityBase?, sb: StringBuilder) {
        // TODO: consider self-casts, multi-casts, etc.
        //  consider whether target is best left as EntityBase?
        when (effect.type) {
            SpellEffectType.RESTORE_HEALTH -> processRestoreHealth(effect, target!!, sb)
            SpellEffectType.FIRE_DAMAGE -> processFireDamage(effect, target!!, sb)
            else -> {}
        }
    }

    private fun processRestoreHealth(effect: SpellEffect, target: EntityBase, sb: StringBuilder) {
        // <target> is healed for <effect.strength>
        target.attributes.currentHealth += effect.strength
        sb.appendLine(Message.ENTITY_IS_HEALED, target.names.prefixedRandom(), effect.strength.toString())
    }

    private fun processFireDamage(effect: SpellEffect, target: EntityBase, sb: StringBuilder) {
        target.attributes.currentHealth -= effect.strength
        sb.appendLine(Message.FIREBALL_HURTLES_AT_ENTITY, target.names.prefixedRandom(), effect.strength.toString())
    }

    private fun doEatRandomFoodItem() =
        inventory.getRandomFoodOrNull()?.let { foodFromInventory ->
            with(StringBuilder()) {
                appendLine(
                    Messages.get(
                        Message.ENTITY_EATS_FOOD_FROM_INVENTORY,
                        names.prefixedRandom(),
                        foodFromInventory.name
                    )
                )

                if (--foodFromInventory.bites == 0) {
                    appendLine(Message.FOOD_OR_DRINK_LAST_OF_IT)
                    currentRoom.removeItem(foodFromInventory)
                }

                sendToAll(this)
            }
        } ?: currentRoom.getRandomFoodOrNull()?.let { foodFromRoom ->
            with(StringBuilder()) {
                appendLine(Message.ENTITY_EATS_FOOD_ON_GROUND, names.prefixedRandom(), foodFromRoom.name)

                if (--foodFromRoom.bites == 0) {
                    appendLine(Message.FOOD_OR_DRINK_LAST_OF_IT)
                    currentRoom.removeItem(foodFromRoom)
                }

                sendToAll(this)
            }
        } ?: doNothing()

    private fun doDrinkRandomDrinkItem() =
        inventory.getRandomDrinkOrNull()?.let { drinkFromInventory ->
            with(StringBuilder()) {
                appendLine(
                    Messages.get(
                        Message.ENTITY_DRINKS_DRINK_FROM_INVENTORY,
                        names.prefixedRandom(),
                        drinkFromInventory.name
                    )
                )

                if (--drinkFromInventory.quaffs == 0) {
                    appendLine(Message.FOOD_OR_DRINK_LAST_OF_IT)
                    currentRoom.removeItem(drinkFromInventory)
                }

                sendToAll(this)
            }
        } ?: currentRoom.getRandomDrinkOrNull()?.let { drinkFromRoom ->
            with(StringBuilder()) {
                appendLine(Message.ENTITY_DRINKS_DRINK_ON_GROUND, names.prefixedRandom(), drinkFromRoom.name)

                if (--drinkFromRoom.quaffs == 0) {
                    appendLine(Message.FOOD_OR_DRINK_LAST_OF_IT)
                    currentRoom.removeItem(drinkFromRoom)
                }

                sendToAll(this)
            }
        } ?: doNothing()

    private fun doIdle() = doAction(EntityBehavior.randomIdleAction())
    private fun doIdleFlavorAction() =
        sendToAll(
            FlavorText.get(EntityAction.IDLE_FLAVOR_ACTION)
                .replace(
                    oldValue = "capitalizedConversationalName",
                    newValue = names.capitalizedConversational
                )
        )

    private fun doRandomMove() =
        if (posture != EntityPosture.STANDING) {
            Debug.println("EntityBase::doRandomMove() - need to stand")
            doStand()
        } else {
            val connection =
                if (canTravelBetweenRegions) {
                    currentRoom.connections.random()
                } else {
                    currentRoom.connectionsInRegion.random()
                }

            World.getRoomFromCoordinates(connection.coordinates)?.let { newRoom ->
                doMove(newRoom, connection)
            }
                ?: Debug.println("EntityBase::doRandomMove() - BAD ROOM RETURNED BY World.getRoomFromCoordinates() - ${connection.coordinatesString}")
        }

    private fun doMove(newRoom: Room, connection: Connection) {
        // pass the connection as part of the move message
        currentRoom.removeEntity(this, connection)
        currentRoom = newRoom
        currentRoom.addEntity(this)
    }
    // endregion

    // region situations
    fun isInSituation(situation: EntitySituation) =
        when (situation) {
            EntitySituation.ANY -> true

            EntitySituation.INJURED_MINOR -> isInjuredMinor()
            EntitySituation.INJURED_MODERATE -> isInjuredModerate()
            EntitySituation.INJURED_MAJOR -> isInjuredMajor()

            EntitySituation.INJURED_FRIENDLY_IN_ROOM -> currentRoom.containsInjuredFriendly(friend = this)

            EntitySituation.SITTING -> posture == EntityPosture.SITTING
            EntitySituation.NOT_SITTING -> posture != EntityPosture.SITTING
            EntitySituation.STANDING -> posture == EntityPosture.STANDING
            EntitySituation.KNEELING -> posture == EntityPosture.KNEELING

            EntitySituation.ALONE -> isAlone()
            EntitySituation.NOT_ALONE -> !isAlone()

            EntitySituation.ROOM_CONTAINS_LIVING_PLAYER -> currentRoom.containsLivingPlayer()

            EntitySituation.NO_MONSTERS -> currentRoom.monsters.isEmpty()
            EntitySituation.SINGLE_MONSTER -> currentRoom.monsters.size == 1
            EntitySituation.MULTIPLE_MONSTERS -> currentRoom.monsters.size > 1

            EntitySituation.NO_NPCS -> currentRoom.npcs.isEmpty()
            EntitySituation.SINGLE_NPC -> currentRoom.npcs.size == 1
            EntitySituation.MULTIPLE_NPCS -> currentRoom.npcs.size > 1

            EntitySituation.NO_HOSTILES -> allHostilesCount == 0
            EntitySituation.SINGLE_HOSTILE -> allHostilesCount == 1
            EntitySituation.MULTIPLE_HOSTILES -> allHostilesCount > 1
            EntitySituation.ANY_LIVING_HOSTILES -> livingHostilesCount > 0

            EntitySituation.FOUND_ANY_ITEM -> currentRoom.containsItem()
            EntitySituation.FOUND_VALUABLE_ITEM -> currentRoom.containsValuableItem()

            // TODO: implement these situation checks
            EntitySituation.FOUND_GOOD_ARMOR -> false
            EntitySituation.FOUND_GOOD_ITEM -> false
            EntitySituation.FOUND_GOOD_WEAPON -> false
            EntitySituation.WITH_OTHER_MONSTER_SAME_TYPE -> false
            EntitySituation.WITH_PACK -> false
            EntitySituation.WITH_PACK_SAME_TYPE -> false
            EntitySituation.NORMAL -> false

            EntitySituation.HAS_WEAPON_EQUIPPED -> weapon != null
            EntitySituation.FOUND_BETTER_ARMOR -> foundBetterArmor()
            EntitySituation.FOUND_BETTER_WEAPON -> foundBetterWeapon()

            EntitySituation.WEAPON_IN_CURRENT_ROOM -> currentRoom.containsWeapon()
            EntitySituation.ARMOR_IN_CURRENT_ROOM -> currentRoom.containsArmor()

            EntitySituation.NO_EQUIPPED_WEAPON -> weapon == null

            EntitySituation.ANY_UNSEARCHED_DEAD_HOSTILES -> deadAndUnsearchedHostilesCount > 0

            EntitySituation.INVENTORY_OR_CURRENT_ROOM_CONTAINS_WEAPON -> inventory.containsWeapon() || currentRoom.containsWeapon()
            EntitySituation.INVENTORY_OR_CURRENT_ROOM_CONTAINS_ARMOR -> inventory.containsArmor() || currentRoom.containsArmor()
            EntitySituation.INVENTORY_OR_CURRENT_ROOM_CONTAINS_FOOD -> inventory.containsFood() || currentRoom.containsFood()
            EntitySituation.INVENTORY_OR_CURRENT_ROOM_CONTAINS_DRINK -> inventory.containsDrink() || currentRoom.containsDrink()
            EntitySituation.INVENTORY_OR_CURRENT_ROOM_CONTAINS_JUNK -> inventory.containsJunk() || currentRoom.containsJunk()
            EntitySituation.INVENTORY_OR_CURRENT_ROOM_CONTAINS_CONTAINER -> inventory.containsContainer() || currentRoom.containsContainer()

            EntitySituation.CAN_CAST_HEALING_SPELL -> canCastSpellWithEffectType(SpellEffectType.RESTORE_HEALTH)
            EntitySituation.CAN_CAST_FIRE_DAMAGE_SPELL -> canCastSpellWithEffectType(SpellEffectType.FIRE_DAMAGE)
            EntitySituation.CAN_CAST_DAMAGE_SPELL -> damageSpells.isNotEmpty()
            else -> false
        }

    private fun isAlone() = currentRoom.entities.size == 1
    // endregion

    // region init
    suspend fun goLiveYourLifeAndBeFree(initialRoom: Room) {
        doInit(initialRoom)

        while (hasNotBeenSearched && Game.running) {
            doDelay()
            doAction()
        }

        doFinalCleanup()
    }

    private fun doInit(initialRoom: Room) {
        Debug.println(
            "EntityBase::doInit() - adding ${names.full} to ${initialRoom.coordinates}",
            DebugMessageType.ENTITY_ADD_TO_ROOM
        )

        // set initial room and add self
        currentRoom = initialRoom
        currentRoom.addEntity(this)
    }

    suspend fun naturalHealthRestoration() {
        while (isAlive && Game.running) {
            if (attributes.currentHealth < attributes.maximumHealth) {
                val old = "${attributes.currentHealth}/${attributes.maximumHealth}"
                val new =
                    "${attributes.currentHealth + attributes.naturalHealthRestorationRate}/${attributes.maximumHealth}"
                Debug.println(
                    "Natural health restoration $name: $old -> $new",
                    DebugMessageType.ENTITY_PASSIVE_EFFECT
                )
            }
            attributes.currentHealth += attributes.naturalHealthRestorationRate
            doDelay()
        }
    }

    suspend fun naturalMagicRestoration() {
        while (isAlive && Game.running) {
            if (attributes.currentMagic < attributes.maximumMagic) {
                val old = "${attributes.currentMagic}/${attributes.maximumMagic}"
                val new =
                    "${attributes.currentMagic + attributes.naturalMagicRestorationRate}/${attributes.maximumMagic}"
                Debug.println(
                    "Natural magic restoration $name: $old -> $new",
                    DebugMessageType.ENTITY_PASSIVE_EFFECT
                )
            }
            attributes.currentMagic += attributes.naturalMagicRestorationRate
            doDelay()
        }
    }

// endregion

    // region cleanup
    private fun doFinalCleanup() {
        sendToAll(Message.DEAD_ENTITY_DECAYS, names.finalCleanup)
        currentRoom.removeEntity(this)
    }    // endregion

    // region equip weapon
    private fun doFindAndEquipRandomWeapon() =
        // check personal inventory first
        (inventory.getAndRemoveRandomWeaponOrNull()
        // check room next
            ?: currentRoom.getAndRemoveRandomWeaponOrNull())?.let { foundWeapon ->
            with(StringBuilder()) {
                weapon?.let { oldWeapon ->
                    appendLine(dropString(oldWeapon))
                    currentRoom.addItem(oldWeapon)
                }

                weapon = foundWeapon

                d100(10) {
                    appendLine(say(FlavorText.get(EntityAction.GET_ANY_ITEM)))
                }
                appendLine(equipString(foundWeapon))
                sendToAll(this)
            }
        } ?: doNothing()

    private fun doGetRandomBetterWeapon() {
        (inventory.getAndRemoveRandomBetterWeaponOrNull(weapon?.power?.plus(1) ?: 0)
            ?: currentRoom.getAndRemoveRandomBetterWeaponOrNull(weapon?.power?.plus(1) ?: 0))
            ?.let { newWeapon ->
                with(StringBuilder()) {
                    weapon?.let { oldWeapon ->
                        appendLine(dropString(oldWeapon))
                        currentRoom.addItem(oldWeapon)
                    }

                    val oldWeapon = "[weapon] ${weapon?.name ?: "nothing"}"

                    weapon = newWeapon
                    appendLine(getString(newWeapon))

                    Debug.println(
                        "EntityBase::doGetRandomBetterWeapon()\n" +
                                "$coordinates - ${names.withJob} - ($oldWeapon -> ${newWeapon.name})",
                        DebugMessageType.ENTITY_FIND_WEAPON
                    )
                    sendToAll(this)
                }
            } ?: doNothing()
    }

    private fun foundBetterWeapon() =
        (inventory.getBestWeaponOrNull() ?: currentRoom.getBestWeaponOrNull())?.let { bestWeapon ->
            weapon?.let { myWeapon ->
                myWeapon.power < bestWeapon.power
            } ?: true // found a weapon, and I have nothing equipped
        } ?: false // didn't find a weapon
    // endregion

    // region equip armor
    private fun findRandomArmorOrNull(bodyPart: EntityBodyPart) =
        (inventory.getAndRemoveRandomArmorOrNull(bodyPart)
            ?: currentRoom.getAndRemoveRandomArmorOrNull(bodyPart))

    private fun doFindAndEquipRandomArmor() =
        body.parts.forEach { bodyPart ->
            findRandomArmorOrNull(bodyPart)?.let { newArmor ->
                bodyPart.equippedItem?.let { oldArmor ->
                    sendToAll(unequipAndDropString(oldArmor))
                    currentRoom.addItem(oldArmor)
                }

                // debug only
                val oldArmor = "[${bodyPart.slot}] ${bodyPart.equippedItem?.name ?: "nothing"}"

                bodyPart.equippedItem = newArmor

                Debug.println(
                    "EntityBase::doFindAndEquipAnyArmor()\n" +
                            "$coordinates - ${names.withJob} - ($oldArmor -> ${newArmor.name})",
                    DebugMessageType.ENTITY_FIND_WEAPON
                )

                // TODO: consider flavor text
                // say(FlavorText.get(EntityAction.GET_ANY_ITEM))
                sendToAll(pickUpAndEquip(newArmor))
            }
        }

    private fun foundBetterArmor() =
        body.parts.any { bodyPart ->
            foundBetterArmor(bodyPart)
        }

    private fun foundBetterArmor(bodyPart: EntityBodyPart) =
        inventory.containsBetterArmor(bodyPart) ||
                currentRoom.containsBetterArmor(bodyPart)

    private fun doGetRandomBetterArmor() {
        val sb = StringBuilder()

        body.parts.forEach { bodyPart ->
            (inventory.getAndRemoveBetterArmorOrNull(bodyPart)
                ?: currentRoom.getAndRemoveBetterArmorOrNull(bodyPart))?.let { newArmor ->
                bodyPart.equippedItem?.let { oldArmor ->
                    currentRoom.addItem(oldArmor)
                    sb.appendLine(unequipAndDropString(oldArmor))
                }

                // used for debug string; not used elsewhere
                val oldArmor = "[${bodyPart.slot}] ${bodyPart.equippedItem?.name ?: "nothing"}"

                bodyPart.equippedItem = newArmor
                sb.appendLine(pickUpAndEquip(newArmor))

                Debug.println(
                    "EntityBase::doGetRandomBetterArmor()\n" +
                            "$coordinates - ${names.withJob} - ($oldArmor -> ${newArmor.name})",
                    DebugMessageType.ENTITY_FIND_ARMOR
                )

                Debug.println(sb, DebugMessageType.ECHO_MESSAGE)
                sendToAll(sb)

                // just do one replacement
                return
            }
        }
    }
    // endregion
}

// entity has a creature type (HUMANOID, SPIDER, etc.)
// each creature type has a default behavior
// e.g. spiders don't do the same things humanoids do
// e.g. "raider" that favors loot gathering, which creature types can be raiders?
// combo creature type with profession type
// creature type has default behavior
// profession type has default behavior
// how to reconcile?
// default humanoid is boring, profession can add flair (raider)