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
import engine.world.Connection
import engine.world.Room
import engine.world.World
import kotlin.random.Random

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
    val stringPrefix: String = "",
    val job: String = "",
    val arriveStringSuffix: String = "walks in",
) {
    val inventory: Inventory = Inventory()
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
    abstract val nameForCollectionString: String
    open val nameForStory = fullName
    val prefixedName
        get() = "$stringPrefix$fullName"
    val capitalizedPrefixedName = prefixedName.replaceFirstChar { it.uppercase() }
    val prefixedRandomName
        get() = "$stringPrefix$randomName"
    val capitalizedPrefixedRandomName
        get() = prefixedRandomName.replaceFirstChar { it.uppercase() }
    abstract val arriveName: String
    abstract val deathName: String
    // endregion

    // region strings
    fun getString(item: ItemBase) =
        "$prefixedRandomName picks up ${item.nameWithIndefiniteArticle}."

    fun dropString(item: ItemBase) =
        "$prefixedRandomName drops ${item.nameWithIndefiniteArticle}."

    fun dropString(inventory: Inventory) =
        "$prefixedRandomName drops ${inventory.collectionString}."

    fun destroyString(item: ItemBase) =
        "$prefixedRandomName destroys ${item.nameWithIndefiniteArticle}."

    val arriveString by lazy { "$arriveName $arriveStringSuffix." }
    val deathString by lazy { "$stringPrefix$deathName dies." }
    val sitString
        get() = "$capitalizedPrefixedRandomName sits down."
    val standString
        get() = "$capitalizedPrefixedRandomName stands up."
    val kneelString
        get() = "$capitalizedPrefixedRandomName kneels."

    fun departString(connection: Connection) =
        if (connection.direction != MovementDirection.NONE) {
            // The goblin heads east.
            // "$prefixedRandomName heads ${connection.direction.toString().lowercase()}."
            Messages.get(Message.ENTITY_HEADS_DIRECTION, prefixedRandomName, connection.direction.toString().lowercase())
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
    fun equipString(item: ItemBase) = Messages.get(Message.ENTITY_EQUIPS_ITEM, prefixedRandomName, item.nameWithIndefiniteArticle)
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
                // already verified if evaluated as a situation
                // TODO: can this method be called any other way?
                //  if so, might need to add verification

                // entity weapon
                val weaponString = weapon?.name ?: "fists"
                // entity attack
                val attack = attributes.strength + (weapon?.power ?: 0)
                // player defense
                val defense = player.attributes.baseDefense
                // resultant damage
                val damage = (attack - defense).coerceAtLeast(0)

                announceToAll(Message.ENTITY_ATTACKS_PLAYER, prefixedName, weaponString)

                if (damage > 0) {
                    announceToAll(Message.ENTITY_HITS_ENTITY_FOR_DAMAGE, damage.toString())
                } else {
                    announceToAll(Message.ENTITY_MISSES_ENTITY)
                }

                player.attributes.currentHealth -= damage
                if (player.attributes.currentHealth <= 0) {
                    player.sendMessage(Message.PLAYER_DIES)
                    announceToAll(Message.OTHER_PLAYER_DIES, player.name)
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

            if (Random.nextInt() % 4 == 0) {
                doQuipToEntity(randomLivingHostile)
            }

            announceToAll("$prefixedName swings at the ${randomLivingHostile.name} with their $weaponString.")

            if (damage > 0) {
                announceToAll("They hit for $damage damage.")
            } else {
                announceToAll("They miss!")
            }

            randomLivingHostile.attributes.currentHealth -= damage
            if (randomLivingHostile.attributes.currentHealth <= 0) {
                announceToAll(randomLivingHostile.deathString)
            }
        }

    abstract fun calculateAttackPower(): Int

    fun doSearchRandomUnsearchedDeadHostile() =
        currentRoom.entities.filter { faction.isHostileTo(it.faction) && it.isDead && it.hasNotBeenSearched }
            .randomOrNull()?.let { deadHostile ->
                announceToAll(Message.ENTITY_SEARCHES_DEAD_ENTITY, capitalizedPrefixedName, deadHostile.name)

                deadHostile.weapon?.let {
                    currentRoom.inventory.items.add(it)
                    announceToAll("${deadHostile.prefixedName} drops ${it.nameWithIndefiniteArticle}.")
                }
                deadHostile.armor?.let {
                    currentRoom.inventory.items.add(it)
                    announceToAll("${deadHostile.prefixedName} drops ${it.nameWithIndefiniteArticle}.")
                }

                if (deadHostile.inventory.items.isNotEmpty()) {
                    currentRoom.inventory.items.addAll(deadHostile.inventory.items)
                    announceToAll(deadHostile.dropString(deadHostile.inventory))
                }

                deadHostile.hasNotBeenSearched = false
            }
    // endregion

    // region speak and say
    private fun doDeadSoloQuip() =
        announceToAll(
            Message.DEAD_ENTITY_QUIPS_SOLO,
            deadConversationalName,
            FlavorText.get(EntityAction.DEAD_QUIP_SOLO)
        )

    private fun doQuipToRandomEntity() =
        currentRoom.entities.randomOrNull()?.let {
            doQuipToEntity(it)
        }

    private fun doQuipToEntity(entity: EntityBase) =
        when {
            isAlive && entity.isAlive && isHostileTo(entity) -> doLivingQuipToLivingHostileEntity(entity)
            isAlive && entity.isAlive -> doLivingQuipToLivingFriendlyEntity(entity)
            isAlive && entity.isDead && isHostileTo(entity) -> doLivingQuipToDeadHostileEntity(entity)
            isAlive && entity.isDead -> doLivingQuipToDeadFriendlyEntity(entity)

            isDead && entity.isAlive && isHostileTo(entity) -> doDeadQuipToLivingHostileEntity(entity)
            isDead && entity.isAlive -> doDeadQuipToLivingFriendlyEntity(entity)
            isDead && entity.isDead && isHostileTo(entity) -> doDeadQuipToDeadHostileEntity(entity)
            isDead && entity.isDead -> doDeadQuipToDeadFriendlyEntity(entity)

            else -> {}
        }

    fun doDeadQuipToDeadHostileEntity(deadHostileEntity: EntityBase) =
        announceToAll(
            Message.ENTITY_SAYS_TO_ENTITY,
            prefixedRandomName,
            deadHostileEntity.conversationalName,
            FlavorText.get(EntityAction.DEAD_ENTITY_SAYS_TO_DEAD_HOSTILE_ENTITY)
        )

    fun doDeadQuipToLivingHostileEntity(livingHostileEntity: EntityBase) =
        announceToAll(
            Message.ENTITY_SAYS_TO_ENTITY,
            prefixedRandomName,
            livingHostileEntity.conversationalName,
            FlavorText.get(EntityAction.DEAD_ENTITY_SAYS_TO_LIVING_HOSTILE_ENTITY)
        )

    fun doLivingQuipToDeadHostileEntity(deadHostileEntity: EntityBase) =
        announceToAll(
            Message.ENTITY_SAYS_TO_ENTITY,
            prefixedRandomName,
            deadHostileEntity.conversationalName,
            FlavorText.get(EntityAction.LIVING_ENTITY_SAYS_TO_DEAD_HOSTILE_ENTITY)
        )

    fun doLivingQuipToLivingHostileEntity(livingHostileEntity: EntityBase) =
        announceToAll(
            Message.ENTITY_SAYS_TO_ENTITY,
            prefixedRandomName,
            livingHostileEntity.conversationalName,
            FlavorText.get(EntityAction.LIVING_ENTITY_SAYS_TO_LIVING_HOSTILE_ENTITY)
        )

    private fun doLivingQuipToLivingFriendlyEntity(livingFriendlyEntity: EntityBase) =
        if (livingFriendlyEntity == this) {
            // i'm alive and quipping to myself...
            // TODO: we can do better than this
            doMumble()
        } else {
            announceToAll(
                Message.ENTITY_SAYS_TO_ENTITY,
                prefixedRandomName,
                livingFriendlyEntity.conversationalName,
                FlavorText.get(EntityAction.LIVING_ENTITY_SAYS_TO_LIVING_FRIENDLY_ENTITY)
            )
        }

    private fun doLivingQuipToDeadFriendlyEntity(deadFriendlyEntity: EntityBase) =
        announceToAll(
            Message.ENTITY_SAYS_TO_ENTITY,
            prefixedRandomName,
            deadFriendlyEntity.deadConversationalName,
            FlavorText.get(EntityAction.LIVING_ENTITY_SAYS_TO_DEAD_FRIENDLY_ENTITY)
        )

    private fun doDeadQuipToDeadFriendlyEntity(deadFriendlyEntity: EntityBase) =
        if (deadFriendlyEntity == this) {
            // i'm dead and quipping to myself...
            doDeadSoloQuip()
        } else {
            announceToAll(
                Message.ENTITY_SAYS_TO_ENTITY,
                prefixedRandomName,
                deadFriendlyEntity.deadConversationalName,
                FlavorText.get(EntityAction.DEAD_ENTITY_SAYS_TO_DEAD_FRIENDLY_ENTITY)
            )
        }

    private fun doDeadQuipToLivingFriendlyEntity(livingFriendlyEntity: EntityBase) = announceToAll(
        Message.ENTITY_SAYS_TO_ENTITY,
        prefixedRandomName,
        livingFriendlyEntity.randomName,
        FlavorText.get(EntityAction.DEAD_ENTITY_SAYS_TO_LIVING_FRIENDLY_ENTITY)
    )

    fun doMumble() = announceToAll(Message.ENTITY_MUMBLES, prefixedRandomName)

    fun say(what: String) = announceToAll(Message.ENTITY_SAYS, prefixedRandomName, what)

    fun announceToAll(what: String) = currentRoom.announceToAll(what)
    fun announceToAll(message: Message, vararg tokens: String) =
        currentRoom.announceToAll(Messages.get(message, *tokens))
    // endregion

    // region get items, weapons, armor
    private fun doGetValuableItem() {
        currentRoom.inventory.getAndRemoveRandomValuableItem()?.let { item ->
            say(FlavorText.get(EntityAction.GET_VALUABLE_ITEM))
            announceToAll(getString(item))
        }
    }

    private fun doFindAndEquipAnyWeapon() {
        currentRoom.inventory.getAndRemoveRandomWeaponOrNull()?.let { foundWeapon ->
            weapon?.let { oldWeapon ->
                announceToAll(dropString(oldWeapon))
                currentRoom.inventory.items.add(oldWeapon)
            }

            weapon = foundWeapon
            // TODO: consider flavor text
            // say(FlavorText.get(EntityAction.GET_ANY_ITEM))
            announceToAll(getString(foundWeapon))
        }
    }

    private fun doFindAndEquipAnyArmor() {
        currentRoom.inventory.getAndRemoveRandomArmorOrNull()?.let { foundArmor ->
            armor?.let { oldArmor ->
                announceToAll(dropString(oldArmor))
                currentRoom.inventory.items.add(oldArmor)
            }

            armor = foundArmor
            // TODO: consider flavor text
            // say(FlavorText.get(EntityAction.GET_ANY_ITEM))
            announceToAll(getString(foundArmor))
        }
    }

    private fun doGetAndRemoveRandomItem() =
        currentRoom.inventory.getAndRemoveRandomItem()?.let { item ->
            announceToAll(getString(item))
            item
        }

    private fun doRemoveRandomItem() {
        currentRoom.inventory.getAndRemoveRandomItem()?.let { item ->
            announceToAll(destroyString(item))
        }
    }

    private fun doGetRandomBetterWeapon() {
        Debug.println("EntityBase::doGetRandomBetterWeapon()")
        currentRoom.inventory.getAndRemoveRandomBetterWeaponOrNull(weapon?.power?.plus(1) ?: 0)?.let { newWeapon ->
            weapon?.let { oldWeapon ->
                announceToAll(dropString(oldWeapon))
                currentRoom.inventory.items.add(oldWeapon)
            }

            weapon = newWeapon
            announceToAll(getString(newWeapon))
        } ?: {
            Debug.println("EntityBase::doGetRandomWeapon() - no weapon in current room")
            doNothing()
        }
    }

    private fun doGetRandomBetterArmor() {
        currentRoom.inventory.getAndRemoveRandomBetterArmorOrNull((armor?.defense?.plus(1)) ?: 0)?.let { newArmor ->
            armor?.let { oldArmor ->
                currentRoom.inventory.items.add(oldArmor)
                announceToAll(dropString(oldArmor))
            }

            armor = newArmor
            announceToAll(getString(newArmor))
        } ?: doNothing()

        // Debug.println("EntityBase::doGetRandomArmor() - no armor in current room")
        // doNothing()
    }

    private fun foundBetterArmor() =
        // if we find armor in the current room...
        currentRoom.inventory.getBestArmorOrNull()?.let { bestArmor ->
            // if we already have armor equipped...
            armor?.let {
                // return whether my defense is less than best-in-room
                it.defense < bestArmor.defense
                // found armor, and I have none equipped
            } ?: true
            // didn't find armor
        } ?: false

    private fun foundBetterWeapon() =
        currentRoom.inventory.getBestWeaponOrNull()?.let { bestWeapon ->
            weapon?.let {
                it.power < bestWeapon.power
            } ?: true // found a weapon, and I have nothing equipped
        } ?: false // didn't find a weapon

    private fun doGetRandomItemFromRoom() =
        currentRoom.inventory.items.randomOrNull()?.let { item ->
            inventory.items.add(item)
            currentRoom.inventory.items.remove(item)

            announceToAll(getString(item))
        }
    // endregion

    // region posture
    private fun doSit() {
        if (posture != EntityPosture.SITTING) {
            posture = EntityPosture.SITTING
            announceToAll(sitString)
        }
    }

    protected fun doStand() {
        if (posture != EntityPosture.STANDING) {
            posture = EntityPosture.STANDING
            announceToAll(standString)
        }
    }

    private fun doKneel() {
        if (posture != EntityPosture.KNEELING) {
            posture = EntityPosture.KNEELING
            announceToAll(kneelString)
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
        inventory.getRandomTypedItemOrNull<ItemFood>()?.let { foodFromInventory ->
            announceToAll(Message.ENTITY_EATS_FOOD_FROM_INVENTORY, prefixedRandomName, foodFromInventory.name)

            if(--foodFromInventory.bites == 0) {
                announceToAll(Message.FOOD_OR_DRINK_LAST_OF_IT)
                currentRoom.inventory.items.remove(foodFromInventory)
            }
        } ?: currentRoom.inventory.getRandomTypedItemOrNull<ItemFood>()?.let { foodFromRoom ->
            announceToAll(Message.ENTITY_EATS_FOOD_ON_GROUND, prefixedRandomName, foodFromRoom.name)

            if(--foodFromRoom.bites == 0) {
                announceToAll(Message.FOOD_OR_DRINK_LAST_OF_IT)
                currentRoom.inventory.items.remove(foodFromRoom)
            }
        } ?: doNothing()

    fun doDrinkRandomDrinkItem() =
        inventory.getRandomTypedItemOrNull<ItemDrink>()?.let { drinkFromInventory ->
            announceToAll(Message.ENTITY_DRINKS_DRINK_FROM_INVENTORY, prefixedRandomName, drinkFromInventory.name)

            if(--drinkFromInventory.quaffs == 0) {
                announceToAll(Message.FOOD_OR_DRINK_LAST_OF_IT)
                currentRoom.inventory.items.remove(drinkFromInventory)
            }
        } ?: currentRoom.inventory.getRandomTypedItemOrNull<ItemDrink>()?.let { drinkFromRoom ->
            announceToAll(Message.ENTITY_DRINKS_DRINK_ON_GROUND, prefixedRandomName, drinkFromRoom.name)

            if(--drinkFromRoom.quaffs == 0) {
                announceToAll(Message.FOOD_OR_DRINK_LAST_OF_IT)
                currentRoom.inventory.items.remove(drinkFromRoom)
            }
        } ?: doNothing()

    private fun doIdle() = doAction(EntityBehavior.randomIdleAction())
    private fun doIdleFlavorAction() =
        announceToAll(
            FlavorText.get(EntityAction.IDLE_FLAVOR_ACTION)
                .replace("capitalizedConversationalName", conversationalName.replaceFirstChar { it.uppercase() })
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
            EntitySituation.FOUND_ANY_ITEM -> currentRoom.inventory.items.isNotEmpty()
            EntitySituation.FOUND_VALUABLE_ITEM -> currentRoom.inventory.items.any { it.value > Debug.valuableItemMinimumValue }

            EntitySituation.FOUND_GOOD_WEAPON -> false
            EntitySituation.WITH_OTHER_MONSTER_SAME_TYPE -> false
            EntitySituation.WITH_PACK -> false
            EntitySituation.WITH_PACK_SAME_TYPE -> false
            EntitySituation.NORMAL -> false

            EntitySituation.HAS_WEAPON_EQUIPPED -> weapon != null
            EntitySituation.FOUND_BETTER_ARMOR -> foundBetterArmor()
            EntitySituation.FOUND_BETTER_WEAPON -> foundBetterWeapon()

            EntitySituation.WEAPON_IN_CURRENT_ROOM -> inventory.items.any { it is ItemWeapon }
            EntitySituation.ARMOR_IN_CURRENT_ROOM -> inventory.items.any { it is ItemArmor }

            EntitySituation.NO_EQUIPPED_WEAPON -> weapon == null
            EntitySituation.NO_EQUIPPED_ARMOR -> armor == null

            EntitySituation.ANY_UNSEARCHED_DEAD_HOSTILES -> deadAndUnsearchedHostilesCount > 0
            EntitySituation.CURRENT_ROOM_CONTAINS_WEAPON -> currentRoom.containsWeapon
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
        announceToAll(Message.DEAD_ENTITY_DECAYS, finalCleanupName)
        currentRoom.entities.remove(this)
    }
    // endregion
}