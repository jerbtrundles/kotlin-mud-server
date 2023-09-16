package engine.item

import engine.item.template.ItemTemplateDrink

class ItemDrink(
    name: String,
    description: String,
    weight: Double,
    value: Int,
    keywords: List<String>,
    var quaffs: Int
) : ItemBase(name, description, weight, value, keywords) {
    constructor(template: ItemTemplateDrink): this(
        template.name,
        template.description,
        template.weight,
        template.value,
        template.keywords,
        template.quaffs
    )
}