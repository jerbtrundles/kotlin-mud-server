package engine.item

import engine.item.template.ItemTemplateJunk

class ItemJunk(
    name: String,
    description: String,
    weight: Double,
    value: Int,
    keywords: List<String>,
) : ItemBase(name, description, weight, value, keywords) {
    constructor(template: ItemTemplateJunk): this(
        template.name,
        template.description,
        template.weight,
        template.value,
        template.keywords
    )
}