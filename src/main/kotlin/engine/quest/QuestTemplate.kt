package engine.quest

import engine.entity.core.EntityBase
import engine.item.ItemBase

interface QuestTemplate {
    val id: String
    val name: String
    val description: String
    val rewards: List<ItemBase>

    fun toQuest(questGiver: EntityBase): Quest
}
