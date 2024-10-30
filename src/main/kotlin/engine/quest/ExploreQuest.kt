package engine.quest

import engine.entity.core.EntityBase
import engine.item.ItemBase
import engine.world.WorldCoordinates

class ExploreQuest(
    // Quest
    id: String,
    name: String,
    description: String,
    rewards: List<ItemBase>,
    questGiver: EntityBase,
    // ExploreQuest
    val locationsToVisit: Set<WorldCoordinates>
) : Quest(
    id = id,
    name = name,
    description = description,
    rewards = rewards,
    questGiver = questGiver
) {
    private val visitedLocations: MutableSet<WorldCoordinates> = mutableSetOf()

    fun visitLocation(location: WorldCoordinates) {
        if (location in locationsToVisit) {
            visitedLocations.add(location)
        } else {
            // location not part of quest
        }
    }

    override fun isCompleted() =
        visitedLocations.containsAll(locationsToVisit)
}
