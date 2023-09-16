package engine.item.template

import engine.item.ItemArmor

class ItemTemplateArmor(
    name: String,
    description: String,
    weight: Double,
    value: Int,
    keywords: List<String>,
    val defense: Int
): ItemTemplate(name, description, weight, value, keywords) {
    override fun createItem() = ItemArmor(this)
}