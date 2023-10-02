package engine.item

import engine.item.template.ItemTemplateGem

class ItemGem(
    name: String,
    description: String,
    weight: Double,
    value: Int,
    keywords: List<String>,
) : ItemBase(name, description, weight, value, keywords) {
    constructor(template: ItemTemplateGem): this(
        template.name,
        template.description,
        template.weight,
        template.value,
        template.keywords
    )
}