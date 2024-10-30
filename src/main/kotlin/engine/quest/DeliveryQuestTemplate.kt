package engine.quest

import engine.entity.core.EntityBase
import engine.item.ItemBase

class DeliveryQuestTemplate(
    // QuestTemplate
    override val id: String,
    override val description: String,
    override val name: String,
    override val rewards: List<ItemBase>,
    // DeliveryQuestTemplate
    private val itemToDeliver: ItemBase,
    private val npcRecipient: EntityBase
) : QuestTemplate {
    override fun toQuest(questGiver: EntityBase) =
        DeliveryQuest(
            id = id,
            name = name,
            description = description,
            rewards = rewards,
            questGiver = questGiver,
            itemToDeliver = itemToDeliver,
            npcRecipient = npcRecipient
        )
}