package engine.item.template

import engine.item.ItemContainer

class ItemTemplateContainer(
    name: String,
    description: String,
    weight: Double,
    value: Int,
    keywords: List<String>,
): ItemTemplate(name, description, weight, value, keywords) {
    override fun createItem() = ItemContainer(this)
}