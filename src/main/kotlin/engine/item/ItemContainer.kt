package engine.item

import engine.Inventory
import com.beust.klaxon.Json
import engine.item.template.ItemTemplateContainer

class ItemContainer(
    name: String,
    description: String,
    weight: Double,
    value: Int,
    keywords: List<String>,
    @Json(ignored = true)
    val inventory: Inventory = Inventory(),
    @Json(ignored = true)
    var closed: Boolean = true
) : ItemBase(name, description, weight, value, keywords) {
    constructor(template: ItemTemplateContainer): this(
        template.name,
        template.description,
        template.weight,
        template.value,
        template.keywords
    )

    val inventoryString: String
    get() = if (inventory.items.isEmpty()) {
            "You don't see anything in the $name."
        } else {
            "In the $name, you see ${inventory.collectionString}."
        }
}