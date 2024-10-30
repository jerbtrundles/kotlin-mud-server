package engine.quest

import engine.entity.core.EntityBase
import engine.item.ItemBase

abstract class Quest(
    val id: String,
    val name: String,
    val description: String,
    val rewards: List<ItemBase>,
    val questGiver: EntityBase
) {
    abstract fun isCompleted(): Boolean
}
