package engine.quest

import engine.entity.core.EntityBase
import engine.item.ItemBase

class FetchQuestTemplate(
    // QuestTemplate
    override val description: String,
    override val id: String,
    override val name: String,
    override val rewards: List<ItemBase>,
    // FetchQuestTemplate
    val itemsToFetch: Map<String, Int>

) : QuestTemplate {
    override fun toQuest(questGiver: EntityBase) =
        FetchQuest(
            id = id,
            name = name,
            description = description,
            rewards = rewards,
            itemsToFetch = itemsToFetch,
            questGiver = questGiver
        )
}