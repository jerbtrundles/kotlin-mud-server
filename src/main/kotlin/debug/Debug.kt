package debug

import engine.entity.EntityBase
import engine.entity.behavior.EntityAction
import engine.entity.behavior.EntitySituation
import engine.item.template.ItemTemplates
import engine.player.Player
import engine.world.World

object Debug {
    private class Level

    private const val debugging = true

    const val valuableItemMinimumValue = 200
    const val maxNpcs = 10
    const val maxMonsters = 10
    const val maxJanitors = 5
    const val npcDelayMin = 2000
    const val npcDelayMax = 3000
    const val monsterDelayMin = 3000
    const val monsterDelayMax = 4000
    const val monsterMaxLevel = 5
    const val monsterAttackDebuff = 30
    const val npcAttackBuff = 0
    private const val initialWeapons = 0
    private const val initialArmor = 0
    private const val initialJunk = 0
    private const val initialExpensiveJunk = 0
    private const val initialFood = 20
    private const val initialDrink = 20
    private const val initialContainer = 0

    fun println(str: String) {
        if (debugging) {
            kotlin.io.println("\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t[$str]")
        }
    }

    fun init() {
        addRandomItemsToRandomRooms()
        addExpensiveJunk()
    }

    private fun addExpensiveJunk() {
        repeat(initialExpensiveJunk) {
            ItemTemplates.junk[0].createItemAt(World.getRandomRoom())
        }
    }

    private fun addRandomItemsToRandomRooms() {
        repeat(initialWeapons) {
            ItemTemplates.weapons.random().createItemAt(World.getRandomRoom())
        }
        repeat(initialArmor) {
            ItemTemplates.armor.random().createItemAt(World.getRandomRoom())
        }
        repeat(initialJunk) {
            ItemTemplates.junk.random().createItemAt(World.getRandomRoom())
        }
        repeat(initialFood) {
            ItemTemplates.food.random().createItemAt(World.getRandomRoom())
        }
        repeat(initialDrink) {
            ItemTemplates.drinks.random().createItemAt(World.getRandomRoom())
        }
        repeat(initialContainer) {
            ItemTemplates.containers.random().createItemAt(World.getRandomRoom())
        }
    }

    fun assessSituations(player: Player, entity: EntityBase) {
        EntitySituation.entries.forEach { situation ->
            player.sendToMe("$situation: ${entity.isInSituation(situation)}")
        }
    }

    val debugActionsExcludedFromPrint = arrayOf(
        EntityAction.IDLE,

//        EntityAction.MOVE,
//        EntityAction.SIT,
//        EntityAction.STAND,
//        EntityAction.KNEEL,
//        EntityAction.GET_RANDOM_BETTER_WEAPON,
//        EntityAction.GET_RANDOM_BETTER_ARMOR,
//        EntityAction.GET_RANDOM_ITEM,
//        EntityAction.IDLE_FLAVOR_ACTION,
//        EntityAction.ATTACK_PLAYER,
//        EntityAction.ATTACK_RANDOM_LIVING_HOSTILE,
//        EntityAction.SEARCH_RANDOM_UNSEARCHED_DEAD_HOSTILE,
//        EntityAction.FIND_AND_EQUIP_ANY_WEAPON,
//        EntityAction.FIND_AND_EQUIP_ANY_ARMOR,
//        EntityAction.GET_VALUABLE_ITEM,
//        EntityAction.GET_ANY_ITEM,
//        EntityAction.DESTROY_ANY_ITEM,
//        EntityAction.HEAL_SELF,
//        EntityAction.LOOK,
//        EntityAction.QUIP_TO_RANDOM_ENTITY,
    )
}