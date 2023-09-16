package engine.item

import engine.item.template.ItemTemplateWeapon

class ItemWeapon(
    name: String,
    description: String,
    weight: Double,
    value: Int,
    keywords: List<String>,
    val power: Int
) : ItemBase(name, description, weight, value, keywords) {
    constructor(template: ItemTemplateWeapon): this(
        template.name,
        template.description,
        template.weight,
        template.value,
        template.keywords,
        template.power
    )
}