package engine.quest

import engine.entity.core.EntityBase
import engine.item.ItemBase
import engine.world.WorldCoordinates

class ExploreQuestTemplate(
    // QuestTemplate
    override val id: String,
    override val name: String,
    override val description: String,
    override val rewards: List<ItemBase>,
    // ExploreQuestTemplate
    val locationsToVisit: Set<WorldCoordinates>
) : QuestTemplate {
    override fun toQuest(questGiver: EntityBase) =
        ExploreQuest(
            id = id,
            name = name,
            description = description,
            rewards = rewards,
            questGiver = questGiver,
            locationsToVisit = locationsToVisit
        )
}