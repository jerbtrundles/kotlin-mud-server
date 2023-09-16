package engine.item

import engine.item.template.ItemTemplateFood

class ItemFood(
    name: String,
    description: String,
    weight: Double,
    value: Int,
    keywords: List<String>,
    var bites: Int
) : ItemBase(name, description, weight, value, keywords) {
    constructor(template: ItemTemplateFood): this(
        template.name,
        template.description,
        template.weight,
        template.value,
        template.keywords,
        template.bites
    )
}