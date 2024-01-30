package engine.item.armor

import engine.item.ItemBase
import engine.item.template.ItemTemplateArmor

class ItemArmor(
    name: String,
    description: String,
    weight: Double,
    value: Int,
    keywords: List<String>,
    val defense: Int,
    val slot: ItemArmorSlot
) : ItemBase(name, description, weight, value, keywords) {
    constructor(template: ItemTemplateArmor) : this(
        name = template.name,
        description = template.description,
        weight = template.weight,
        value = template.value,
        keywords = template.keywords,
        defense = template.defense,
        slot = template.slot
    )
}
