package engine.entity

import engine.Inventory
import debug.Debug
import engine.Message
import engine.Messages
import engine.entity.behavior.EntityAction
import engine.entity.behavior.EntityBehavior
import engine.entity.behavior.EntitySituation
import engine.entity.behavior.FlavorText
import engine.game.Game
import engine.game.MovementDirection
import engine.item.*
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
    val delayMin: Int,
    val delayMax: Int,
    var weapon: ItemWeapon? = null,
    var armor: ItemArmor? = null,
    val inventory: Inventory,
    val stringPrefix: String = "",
    val job: String = "",
    val arriveStringSuffix: String = "walks in"
) {
    var currentRoom: Room = World.void
    protected var posture: EntityPosture = EntityPosture.STANDING
    var hasNotBeenSearched = true
    val isDead get() = attributes.currentHealth <= 0
    val isAlive get() = !isDead
    val coordinates
        get() = currentRoom.coordinates

    // region names
    abstract val fullName: String
    abstract val nameWithJob: String
    abstract val conversationalName: String
    abstract val deadConversationalName: String
    abstract val finalCleanupName: String
    abstract val randomName: String
    // "..., a goblin (kneeling), ..."
    val nameForCollectionString
        get() = when {
            isDead -> "$fullName (dead)"
            posture == EntityPosture.KNEELING -> "$fullName (kneeling)"
            posture == EntityPosture.SITTING -> "$fullName (sitting)"
            else -> fullName
        }
    open val nameForStory = fullName
    val prefixedName
        get() = "$stringPrefix$fullName"
    val capitalizedPrefixedName
        get() = prefixedName.replaceFirstChar { it.uppercase() }
    val prefixedRandomName
        get() = "$stringPrefix$randomName"
    val capitalizedPrefixedRandomName
        get() = prefixedRandomName.replaceFirstChar { it.uppercase() }
    val capitalizedConversationalName
        get() = conversationalName.replaceFirstChar { it.uppercase() }
    abstract val arriveName: String
    abstract val deathName: String
    val capitalizedPrefixedDeathName
        get() = "$stringPrefix$deathName"
    // endregion

    // region strings
    fun getString(item: ItemBase) =
        Messages.get(Message.ENTITY_PICKS_UP_ITEM, prefixedRandomName, item.nameWithIndefiniteArticle)

    fun dropString(item: ItemBase) =
        Messages.get(Message.ENTITY_DROPS_ITEM, prefixedRandomName, item.nameWithIndefiniteArticle)

    fun dropString(inventory: Inventory) =
        Messages.get(Message.ENTITY_DROPS_ITEM, prefixedRandomName, inventory.collectionString)

    fun destroyString(item: ItemBase) =
        Messages.get(Message.ENTITY_DESTROYS_ITEM, prefixedRandomName, item.nameWithIndefiniteArticle)

    val arriveString
        get() = Messages.get(Message.ENTITY_ARRIVES, arriveName, arriveStringSuffix)
    val deathString
        get() = Messages.get(Message.ENTITY_DIES, capitalizedPrefixedDeathName)
    val sitString
        get() = Messages.get(Message.ENTITY_SITS, capitalizedPrefixedRandomName)
    val standString
        get() = Messages.get(Message.ENTITY_STANDS_UP, capitalizedPrefixedRandomName)
    val kneelString
        get() = Messages.get(Message.ENTITY_KNEELS, capitalizedPrefixedRandomName)

    fun departString(connection: Connection) =
        if (connection.direction != MovementDirection.NONE) {
            // The goblin heads east.
            // "$prefixedRandomName heads ${connection.direction.toString().lowercase()}."
            Messages.get(
                Message.ENTITY_HEADS_DIRECTION,
                prefixedRandomName,
                connection.direction.toString().lowercase()
            )
        } else if (connection.matchInputString.contains("gates")) {
            // TODO: make this better
            // TODO: other cases for climbing, other connection types
            // TODO: The goblin heads through the gates.
            // "$prefixedRandomName heads through the town gates."
            Messages.get(Message.ENTITY_HEADS_THROUGH_THE_TOWN_GATES, prefixedRandomName)
        } else {
            // TODO: make this better
            // "$prefixedRandomName heads over to the ${connection.matchInput.suffix}."
            Messages.get(Message.ENTITY_HEADS_OVER_TO_THE_CONNECTION, prefixedRandomName, connection.matchInput.suffix)
        }

    fun putAwayString(item: ItemBase) = Messages.get(Message.ENTITY_PUTS_AWAY_ITEM, prefixedRandomName, item.name)
    fun equipString(item: ItemBase) =
        Messages.get(Message.ENTITY_EQUIPS_ITEM, prefixedRandomName, item.nameWithIndefiniteArticle)

    fun removeString(item: ItemBase) = Messages.get(Message.ENTITY_REMOVES_ITEM, prefixedRandomName, item.name)
    // endregion

    // region core, init, cleanup
    suspend fun goLiveYourLifeAndBeFree(initialRoom: Room) {
        doInit(initialRoom)

        while (hasNotBeenSearched && Game.running) {
            doDelay()
            doAction()
        }

        doFinalCleanup()
    }

    protected fun doAction() =
        if (isDead) {
            doIsDead()
        } else {
            val action = behavior.getNextAction(this)

            // debug print here instead of after doAction(), because doAction() can reroute to a secondary action
            // and maybe lead to reverse/inaccurate reporting
            if (!Debug.debugActionsExcludedFromPrint.contains(action)) {
                Debug.println("EntityBase::doAction() - $name - $action")
            }

            doAction(action)
        }

    suspend fun doDelay() =
    // TODO: make this based off of something else
    //  e.g. entity speed, type
        // Debug.println("EntityMonster::doDelay()")
        Game.delayRandom(
            min = delayMin, max = delayMax,
            conditions = listOf(
                ::hasNotBeenSearched
            )
        )

    fun matchesKeyword(keyword: String) = (name == keyword) || keywords.contains(keyword)
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
    val livingHostilesCount
        get() = livingHostilesInCurrentRoom.size
    val deadAndUnsearchedHostilesCount
        get() = deadAndUnsearchedHostilesInCurrentRoom.size

    fun isHostileTo(otherEntity: EntityBase) = faction.isHostileTo(otherEntity.faction)
    fun isHostileTo(otherFaction: EntityFactions) = faction.isHostileTo(faction)

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
                    appendLine(Message.ENTITY_ATTACKS_PLAYER, prefixedName, weaponString)

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
            // entity weapon
            val weaponString = weapon?.name ?: "fists"
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

                appendLine("$prefixedName swings at the ${randomLivingHostile.name} with their $weaponString.")

                if (damage > 0) {
                    appendLine("They hit for $damage damage.")
                } else {
                    appendLine("They miss!")
                }

                randomLivingHostile.attributes.currentHealth -= damage
                if (randomLivingHostile.attributes.currentHealth <= 0) {
                    appendLine(randomLivingHostile.deathString)
                }

                sendToAll(this)
            }
        }

    abstract fun calculateAttackPower(): Int

    fun doSearchRandomUnsearchedDeadHostile() =
        currentRoom.entities.filter { faction.isHostileTo(it.faction) && it.isDead && it.hasNotBeenSearched }
            .randomOrNull()?.let { deadHostile ->
                // build and announce a single drop string out of weapon, armor, and other inventory items
                with(StringBuilder()) {
                    appendLine(Message.ENTITY_SEARCHES_DEAD_ENTITY, capitalizedPrefixedName, deadHostile.name)

                    deadHostile.weapon?.let {
                        currentRoom.addItem(it)
                        appendLine("${deadHostile.prefixedName} drops ${it.nameWithIndefiniteArticle}.")
                    }

                    deadHostile.armor?.let {
                        currentRoom.addItem(it)
                        appendLine("${deadHostile.prefixedName} drops ${it.nameWithIndefiniteArticle}.")
                    }

                    if (deadHostile.inventory.isNotEmpty()) {
                        currentRoom.addInventory(deadHostile.inventory)
                        appendLine(deadHostile.dropString(deadHostile.inventory))
                    }

                    sendToAll(this)
                }

                deadHostile.hasNotBeenSearched = false
            }
    // endregion

    // region speak and say
    private fun doDeadSoloQuip() =
        sendToAll(
            Message.DEAD_ENTITY_QUIPS_SOLO,
            deadConversationalName,
            FlavorText.get(EntityAction.DEAD_QUIP_SOLO)
        )

    private fun doQuipToRandomEntity() =
        currentRoom.entities.randomOrNull()?.let {
            doQuipToEntity(it)
        }

    private fun doQuipToEntity(entity: EntityBase) = sendToAll(getQuip(entity))
    private fun getQuip(entity: EntityBase) = when {
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
            prefixedRandomName,
            deadHostileEntity.conversationalName,
            FlavorText.get(EntityAction.DEAD_ENTITY_SAYS_TO_DEAD_HOSTILE_ENTITY)
        )

    private fun getDeadToLivingHostileQuip(livingHostileEntity: EntityBase) =
        Messages.get(
            Message.ENTITY_SAYS_TO_ENTITY,
            prefixedRandomName,
            livingHostileEntity.conversationalName,
            FlavorText.get(EntityAction.DEAD_ENTITY_SAYS_TO_LIVING_HOSTILE_ENTITY)
        )

    private fun getLivingToDeadHostileQuip(deadHostileEntity: EntityBase) =
        Messages.get(
            Message.ENTITY_SAYS_TO_ENTITY,
            prefixedRandomName,
            deadHostileEntity.conversationalName,
            FlavorText.get(EntityAction.LIVING_ENTITY_SAYS_TO_DEAD_HOSTILE_ENTITY)
        )

    private fun getLivingToLivingHostileQuip(livingHostileEntity: EntityBase) =
        Messages.get(
            Message.ENTITY_SAYS_TO_ENTITY,
            prefixedRandomName,
            livingHostileEntity.conversationalName,
            FlavorText.get(EntityAction.LIVING_ENTITY_SAYS_TO_LIVING_HOSTILE_ENTITY)
        )

    private fun getLivingToLivingFriendlyQuip(livingFriendlyEntity: EntityBase) =
        if (livingFriendlyEntity == this) {
            // i'm alive and quipping to myself...
            // TODO: we can do better than this
            Messages.get(Message.ENTITY_MUMBLES, prefixedRandomName)
        } else {
            Messages.get(
                Message.ENTITY_SAYS_TO_ENTITY,
                prefixedRandomName,
                livingFriendlyEntity.conversationalName,
                FlavorText.get(EntityAction.LIVING_ENTITY_SAYS_TO_LIVING_FRIENDLY_ENTITY)
            )
        }

    private fun getLivingToDeadFriendlyQuip(deadFriendlyEntity: EntityBase) =
        Messages.get(
            Message.ENTITY_SAYS_TO_ENTITY,
            prefixedRandomName,
            deadFriendlyEntity.deadConversationalName,
            FlavorText.get(EntityAction.LIVING_ENTITY_SAYS_TO_DEAD_FRIENDLY_ENTITY)
        )

    private fun getDeadToDeadFriendlyQuip(deadFriendlyEntity: EntityBase) =
        if (deadFriendlyEntity == this) {
            // i'm dead and quipping to myself...
            Messages.get(
                Message.DEAD_ENTITY_QUIPS_SOLO,
                deadConversationalName,
                FlavorText.get(EntityAction.DEAD_QUIP_SOLO)
            )
        } else {
            Messages.get(
                Message.ENTITY_SAYS_TO_ENTITY,
                prefixedRandomName,
                deadFriendlyEntity.deadConversationalName,
                FlavorText.get(EntityAction.DEAD_ENTITY_SAYS_TO_DEAD_FRIENDLY_ENTITY)
            )
        }

    private fun getDeadToLivingFriendlyQuip(livingFriendlyEntity: EntityBase) =
        Messages.get(
            Message.ENTITY_SAYS_TO_ENTITY,
            prefixedRandomName,
            livingFriendlyEntity.randomName,
            FlavorText.get(EntityAction.DEAD_ENTITY_SAYS_TO_LIVING_FRIENDLY_ENTITY)
        )

    fun doMumble() = sendToAll(Message.ENTITY_MUMBLES, prefixedRandomName)

    fun say(what: String) = sendToAll(Message.ENTITY_SAYS, prefixedRandomName, what)

    fun sendToAll(sb: StringBuilder) = sendToAll(sb.trim('\n').toString())
    fun sendToAll(what: String) = currentRoom.sendToAll(what)
    fun sendToAll(message: Message, vararg tokens: String) =
        currentRoom.sendToAll(Messages.get(message, *tokens))
    // endregion

    // region get items, weapons, armor
    private fun doGetValuableItem() {
        currentRoom.getAndRemoveRandomValuableItemOrNull()?.let { item ->
            say(FlavorText.get(EntityAction.GET_VALUABLE_ITEM))
            sendToAll(getString(item))
        }
    }

    private fun doFindAndEquipAnyWeapon() =
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
                    appendLine(FlavorText.get(EntityAction.GET_ANY_ITEM))
                }
                appendLine(equipString(foundWeapon))
                sendToAll(this)
            }

            sendToAll(equipString(foundWeapon))
        } ?: doNothing()

    private fun doFindAndEquipAnyArmor() {
        currentRoom.getAndRemoveRandomArmorOrNull()?.let { foundArmor ->
            armor?.let { oldArmor ->
                sendToAll(dropString(oldArmor))
                currentRoom.addItem(oldArmor)
            }

            armor = foundArmor
            // TODO: consider flavor text
            // say(FlavorText.get(EntityAction.GET_ANY_ITEM))
            sendToAll(getString(foundArmor))
        }
    }

    private fun doGetAndRemoveRandomItem() =
        currentRoom.getAndRemoveRandomItemOrNull()?.let { item ->
            sendToAll(getString(item))
            item
        }

    private fun doRemoveRandomItem() {
        currentRoom.getAndRemoveRandomItemOrNull()?.let { item ->
            sendToAll(destroyString(item))
        }
    }

    private fun doGetRandomBetterWeapon() {
        Debug.println("EntityBase::doGetRandomBetterWeapon()")
        currentRoom.getAndRemoveRandomBetterWeaponOrNull(weapon?.power?.plus(1) ?: 0)?.let { newWeapon ->
            with(StringBuilder()) {
                weapon?.let { oldWeapon ->
                    appendLine(dropString(oldWeapon))
                    currentRoom.addItem(oldWeapon)
                }

                weapon = newWeapon
                appendLine(getString(newWeapon))

                sendToAll(this)
            }
        } ?: {
            Debug.println("EntityBase::doGetRandomWeapon() - no weapon in current room")
            doNothing()
        }
    }

    private fun doGetRandomBetterArmor() {
        currentRoom.getAndRemoveRandomBetterArmorOrNull((armor?.defense?.plus(1)) ?: 0)?.let { newArmor ->
            with(StringBuilder()) {
                armor?.let { oldArmor ->
                    currentRoom.addItem(oldArmor)
                    appendLine(dropString(oldArmor))
                }

                armor = newArmor
                appendLine(getString(newArmor))

                sendToAll(this)
            }
        } ?: doNothing()

        // Debug.println("EntityBase::doGetRandomArmor() - no armor in current room")
        // doNothing()
    }

    private fun foundBetterArmor() =
        (inventory.getBestArmorOrNull() ?: currentRoom.getBestArmorOrNull())?.let { bestArmor ->
            // if we already have armor equipped...
            armor?.let { myArmor ->
                // return whether my defense is less than best-in-room
                myArmor.defense < bestArmor.defense
                // found armor, and I have none equipped
            } ?: true
            // didn't find armor
        } ?: false

    private fun foundBetterWeapon() =
        (inventory.getBestWeaponOrNull() ?: currentRoom.getBestWeaponOrNull())?.let { bestWeapon ->
            weapon?.let { myWeapon ->
                myWeapon.power < bestWeapon.power
            } ?: true // found a weapon, and I have nothing equipped
        } ?: false // didn't find a weapon
    // endregion

    // region posture
    private fun doSit() {
        if (posture != EntityPosture.SITTING) {
            posture = EntityPosture.SITTING
            sendToAll(sitString)
        }
    }

    protected fun doStand() {
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
    protected fun doNothing() {}
    fun doIsDead() {}
    fun doAction(action: EntityAction) {
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
            EntityAction.FIND_AND_EQUIP_ANY_WEAPON -> doFindAndEquipAnyWeapon()
            EntityAction.FIND_AND_EQUIP_ANY_ARMOR -> doFindAndEquipAnyArmor()
            EntityAction.GET_VALUABLE_ITEM -> doGetValuableItem()
            EntityAction.GET_ANY_ITEM -> doGetAndRemoveRandomItem()
            EntityAction.DESTROY_ANY_ITEM -> doRemoveRandomItem()
            EntityAction.HEAL_SELF -> doNothing()
            EntityAction.LOOK -> doNothing()
            EntityAction.QUIP_TO_RANDOM_ENTITY -> doQuipToRandomEntity()
            EntityAction.EAT_RANDOM_FOOD -> doEatRandomFoodItem()
            EntityAction.DRINK_RANDOM_DRINK -> doDrinkRandomDrinkItem()
            else -> doNothing()
        }
    }

    fun doEatRandomFoodItem() =
        inventory.getRandomFoodOrNull()?.let { foodFromInventory ->
            with(StringBuilder()) {
                appendLine(
                    Messages.get(
                        Message.ENTITY_EATS_FOOD_FROM_INVENTORY,
                        prefixedRandomName,
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
                appendLine(Message.ENTITY_EATS_FOOD_ON_GROUND, prefixedRandomName, foodFromRoom.name)

                if (--foodFromRoom.bites == 0) {
                    appendLine(Message.FOOD_OR_DRINK_LAST_OF_IT)
                    currentRoom.removeItem(foodFromRoom)
                }

                sendToAll(this)
            }
        } ?: doNothing()

    fun doDrinkRandomDrinkItem() =
        inventory.getRandomDrinkOrNull()?.let { drinkFromInventory ->
            with(StringBuilder()) {
                appendLine(
                    Messages.get(
                        Message.ENTITY_DRINKS_DRINK_FROM_INVENTORY,
                        prefixedRandomName,
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
                appendLine(Message.ENTITY_DRINKS_DRINK_ON_GROUND, prefixedRandomName, drinkFromRoom.name)

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
                    newValue = capitalizedConversationalName
                )
        )

    private fun doRandomMove() =
        if (posture != EntityPosture.STANDING) {
            Debug.println("EntityBase::doRandomMove() - need to stand")
            doStand()
        } else {
            val connection = currentRoom.connections.random()
            val newRoom = World.getRoomFromCoordinates(connection.coordinates)
            doMove(newRoom, connection)
        }

    fun doMove(newRoom: Room, connection: Connection) {
        // Debug.println("EntityBase::doRandomMove() - $name - move from ${currentRoom.coordinates} to ${newRoom.coordinates}")
        // leaving
        currentRoom.removeEntity(this, connection)
        // move
        currentRoom = newRoom
        // arriving (addEntity handles announce)
        currentRoom.addEntity(this)
    }
    // endregion

    // region situations
    fun isInSituation(situation: EntitySituation) =
        when (situation) {
            EntitySituation.INJURED_MINOR -> attributes.isInjuredMinor()
            EntitySituation.INJURED_MODERATE -> attributes.isInjuredModerate()
            EntitySituation.INJURED_MAJOR -> attributes.isInjuredMajor()
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

            EntitySituation.FOUND_GOOD_ARMOR -> false
            EntitySituation.FOUND_GOOD_ITEM -> false
            EntitySituation.FOUND_ANY_ITEM -> currentRoom.containsItem
            EntitySituation.FOUND_VALUABLE_ITEM -> currentRoom.containsValuableItem

            EntitySituation.FOUND_GOOD_WEAPON -> false
            EntitySituation.WITH_OTHER_MONSTER_SAME_TYPE -> false
            EntitySituation.WITH_PACK -> false
            EntitySituation.WITH_PACK_SAME_TYPE -> false
            EntitySituation.NORMAL -> false

            EntitySituation.HAS_WEAPON_EQUIPPED -> weapon != null
            EntitySituation.FOUND_BETTER_ARMOR -> foundBetterArmor()
            EntitySituation.FOUND_BETTER_WEAPON -> foundBetterWeapon()

            EntitySituation.WEAPON_IN_CURRENT_ROOM -> inventory.containsWeapon()
            EntitySituation.ARMOR_IN_CURRENT_ROOM -> inventory.containsArmor()

            EntitySituation.NO_EQUIPPED_WEAPON -> weapon == null
            EntitySituation.NO_EQUIPPED_ARMOR -> armor == null

            EntitySituation.ANY_UNSEARCHED_DEAD_HOSTILES -> deadAndUnsearchedHostilesCount > 0

            EntitySituation.INVENTORY_OR_CURRENT_ROOM_CONTAINS_WEAPON -> inventory.containsWeapon() || currentRoom.containsWeapon()
            EntitySituation.INVENTORY_OR_CURRENT_ROOM_CONTAINS_ARMOR -> inventory.containsArmor() || currentRoom.containsArmor()
            EntitySituation.INVENTORY_OR_CURRENT_ROOM_CONTAINS_FOOD -> inventory.containsFood() || currentRoom.containsFood()
            EntitySituation.INVENTORY_OR_CURRENT_ROOM_CONTAINS_DRINK -> inventory.containsDrink() || currentRoom.containsDrink()
            EntitySituation.INVENTORY_OR_CURRENT_ROOM_CONTAINS_JUNK -> inventory.containsJunk() || currentRoom.containsJunk()
            EntitySituation.INVENTORY_OR_CURRENT_ROOM_CONTAINS_CONTAINER -> inventory.containsContainer() || currentRoom.containsContainer()

            EntitySituation.ANY -> true
            else -> false
        }

    fun isAlone() = currentRoom.entities.size == 1
    // endregion

    // region init/cleanup
    fun doInit(initialRoom: Room) {
        // set initial room and add self
        currentRoom = initialRoom
        currentRoom.addEntity(this)

        Debug.println("EntityBase::doInit() - adding $fullName to ${currentRoom.coordinates}")
    }

    fun doFinalCleanup() {
        sendToAll(Message.DEAD_ENTITY_DECAYS, finalCleanupName)
        currentRoom.removeEntity(this)
    }
    // endregion
}