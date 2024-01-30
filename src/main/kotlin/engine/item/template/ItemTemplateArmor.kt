package engine.item.template

import engine.item.armor.ItemArmor
import engine.item.armor.ItemArmorSlot

class ItemTemplateArmor(
    name: String,
    description: String,
    weight: Double,
    value: Int,
    keywords: List<String>,
    val defense: Int,
    val slot: ItemArmorSlot
): ItemTemplate(name, description, weight, value, keywords) {
    override fun createItem() = ItemArmor(this)
}