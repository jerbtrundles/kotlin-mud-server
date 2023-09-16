package engine.item.template

import engine.item.ItemFood

class ItemTemplateFood(
    name: String,
    description: String,
    weight: Double,
    value: Int,
    keywords: List<String>,
    val bites: Int
): ItemTemplate(name, description, weight, value, keywords) {
    override fun createItem() = ItemFood(this)
}