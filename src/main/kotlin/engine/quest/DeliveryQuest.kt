package engine.quest

import engine.entity.core.EntityBase
import engine.item.ItemBase
import engine.world.Room
import engine.world.WorldCoordinates

class DeliveryQuest(
    // Quest
    id: String,
    name: String,
    description: String,
    rewards: List<ItemBase>,
    questGiver: EntityBase,
    // DeliveryQuest
    val itemToDeliver: ItemBase,
    val npcRecipient: EntityBase
) : Quest(
    id = id,
    name = name,
    description = description,
    rewards = rewards,
    questGiver = questGiver
) {
    private var itemDelivered: Boolean = false

    fun attemptDelivery(item: ItemBase, room: Room) {
        if (item == itemToDeliver && room.entities.contains(npcRecipient)) {
            // message delivery

            itemDelivered = true
        }
    }

    override fun isCompleted() = itemDelivered
}
