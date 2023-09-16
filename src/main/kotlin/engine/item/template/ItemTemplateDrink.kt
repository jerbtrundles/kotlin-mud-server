package engine.item.template

import engine.item.ItemDrink

class ItemTemplateDrink(
    name: String,
    description: String,
    weight: Double,
    value: Int,
    keywords: List<String>,
    val quaffs: Int
): ItemTemplate(name, description, weight, value, keywords) {
    override fun createItem() = ItemDrink(this)
}