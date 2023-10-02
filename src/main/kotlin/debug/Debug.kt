package debug

import engine.entity.EntityBase
import engine.entity.behavior.EntityAction
import engine.entity.behavior.EntitySituation
import engine.item.template.ItemTemplates
import engine.player.Player
import engine.world.World

object Debug {
    private enum class Level {
        VERBOSE,
    }

    private val debugLevel = Level.VERBOSE
    private const val debugging = true

    const val valuableItemMinimumValue = 50
    const val maxNpcs = 10
    const val maxMonsters = 10
    const val maxJanitors = 4
    const val npcDelayMin = 2000
    const val npcDelayMax = 3000
    const val monsterDelayMin = 3000
    const val monsterDelayMax = 4000
    const val monsterMaxLevel = 2
    const val monsterAttackDebuff = 30
    const val npcAttackBuff = 0
    private const val initialWeapons = 5
    private const val initialArmor = 5
    private const val initialJunk = 5
    private const val initialGems = 50
    private const val initialFood = 5
    private const val initialDrink = 5
    private const val initialContainer = 5

    fun println(str: String) {
        if (debugging) {
            kotlin.io.println("\t\t\t\t\t\t\t\t\t\t\t\t\t\t\t[$str]")
        }
    }

    fun init() {
        addRandomItemsToRandomRooms()
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
        repeat(initialGems) {
            ItemTemplates.gems.random().createItemAt(World.getRandomRoom())
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