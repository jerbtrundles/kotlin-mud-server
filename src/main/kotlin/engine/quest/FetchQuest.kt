package engine.quest

import engine.entity.core.EntityBase
import engine.item.ItemBase
import engine.item.template.ItemTemplates
import engine.world.World

// questgiver: bring me # <item(s)>
class FetchQuest(
    id: String,
    name: String,
    description: String,
    rewards: List<ItemBase>,
    questGiver: EntityBase,
    val itemsToFetch: Map<String, Int>
) : Quest(
    id = id,
    name = name,
    description = description,
    rewards = rewards,
    questGiver = questGiver
) {
    companion object {
        fun random(questGiver: EntityBase): FetchQuest? {
            val randomRegionWithMonsters = World.regions.values.random()
            if(randomRegionWithMonsters.entityManager.allMonsters.isNotEmpty()) {
                val randomMonster = randomRegionWithMonsters.entityManager.allMonsters.random()

                return FetchQuest(
                    questGiver = questGiver,
                    id = "Random fetch quest",
                    name = "Kill $randomMonster",
                    rewards = listOf(ItemTemplates.gems.random().createItem()),
                    itemsToFetch = mapOf("carrot" to 1),
                    description = ""
                )
            } else {
                // region doesn't have any monsters

                return null
            }
        }
    }

    private val collectedItems: MutableMap<String, Int> =
        itemsToFetch.keys.associateWith { 0 }.toMutableMap()
    fun collectedItemString(itemName: String) =
        "Quest: [$name], $itemName (${collectedItems[itemName]}/${itemsToFetch[itemName]})"


    // where to display collected item string?
    // might move this to keywords
    fun collectItem(itemName: String) {
        if (itemsToFetch.contains(itemName)) {
            collectedItems[itemName] = collectedItems.getOrDefault(itemName, 0) + 1
        } else {
            // item not part of quest
        }
    }

    override fun isCompleted() =
        itemsToFetch.all { (item, count) ->
            collectedItems.getOrDefault(item, 0) >= count
        }

    fun needs(item: ItemBase) =
        (itemsToFetch[item.name] ?: 0) > (collectedItems[item.name] ?: 0)
}
