package engine.item

import engine.item.template.ItemTemplateArmor

class ItemArmor(
    name: String,
    description: String,
    weight: Double,
    value: Int,
    keywords: List<String>,
    val defense: Int
) : ItemBase(name, description, weight, value, keywords) {
    constructor(template: ItemTemplateArmor): this(
        template.name,
        template.description,
        template.weight,
        template.value,
        template.keywords,
        template.defense
    )
}