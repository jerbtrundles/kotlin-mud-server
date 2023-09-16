package engine.item.template

import engine.item.ItemJunk

class ItemTemplateJunk(
    name: String,
    description: String,
    weight: Double,
    value: Int,
    keywords: List<String>
): ItemTemplate(name, description, weight, value, keywords) {
    override fun createItem() = ItemJunk(this)
}