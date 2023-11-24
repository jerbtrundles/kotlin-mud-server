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
    const val npcDelayMin = 2000
    const val npcDelayMax = 3000
    const val monsterDelayMin = 3000
    const val monsterDelayMax = 4000
    const val monsterAttackDebuff = 30
    const val npcAttackBuff = 0
    private const val initialWeapons = 5
    private const val initialArmor = 5
    private const val initialJunk = 0
    private const val initialGems = 0
    private const val initialFood = 0
    private const val initialDrink = 0
    private const val initialContainer = 0

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

    fun assessSituations(player: Player, entity: EntityBase) =
        EntitySituation.entries.forEach { situation ->
            player.sendToMe("$situation: ${entity.isInSituation(situation)}")
        }

    val debugActionsExcludedFromPrint =
        arrayOf(
            EntityAction.IDLE,
        )
}