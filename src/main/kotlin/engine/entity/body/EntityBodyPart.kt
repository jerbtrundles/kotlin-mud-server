package engine.entity.body

import engine.item.armor.ItemArmor
import engine.item.armor.ItemArmorSlot

class EntityBodyPart(
    val slot: ItemArmorSlot,
    var equippedItem: ItemArmor? = null
) {
    companion object {
        fun humanoidHead() = EntityBodyPart(ItemArmorSlot.HUMANOID_HEAD)
        fun humanoidChest() = EntityBodyPart(ItemArmorSlot.HUMANOID_CHEST)
        fun humanoidArms() = EntityBodyPart(ItemArmorSlot.HUMANOID_ARMS)
        fun humanoidLegs() = EntityBodyPart(ItemArmorSlot.HUMANOID_LEGS)
        fun humanoidHands() = EntityBodyPart(ItemArmorSlot.HUMANOID_HANDS)
        fun humanoidFeet() = EntityBodyPart(ItemArmorSlot.HUMANOID_FEET)
    }
}