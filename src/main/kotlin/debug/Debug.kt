package debug

import engine.entity.core.EntityBase
import engine.entity.behavior.EntitySituation
import engine.item.template.ItemTemplates
import engine.player.Player
import engine.world.World
import kotlin.text.StringBuilder

object Debug {
    const val VALUABLE_ITEM_MINIMUM_VALUE = 50
    const val npcDelayMin = 1000
    const val npcDelayMax = 2000
    const val monsterDelayMin = 6000
    const val monsterDelayMax = 8000
    const val monsterAttackDebuff = 0
    const val npcAttackBuff = 0
    private const val initialWeapons = 0
    private const val initialArmor = 100
    private const val initialJunk = 0
    private const val initialGems = 10
    private const val initialFood = 0
    private const val initialDrink = 0
    private const val initialContainer = 0

    private const val extraTabs = "\t\t"
    private const val tabs = "\t\t\t\t\t\t\t\t\t\t\t\t\t"

    private const val debugging = true

    fun println(str: String, messageType: DebugMessageType = DebugMessageType.DEFAULT) {
        if (debugging && DebugMessageType.enabled(messageType)) {
            val lines = str.lines().filter { it.isNotEmpty() }
            lines.forEachIndexed { i, line ->
                // indent any extra lines
                if (i > 0) {
                    print(extraTabs)
                }

                kotlin.io.println("$tabs[$line]")
            }
        } else {
            // kotlin.io.println("$tabs[SKIPPED MESSAGE OF TYPE $messageType]")
        }
    }

    fun println(sb: StringBuilder, messageType: DebugMessageType = DebugMessageType.DEFAULT) =
        println(sb.toString(), messageType)

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
}