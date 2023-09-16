package engine.world.template

import com.beust.klaxon.Json
import engine.item.template.ItemTemplates
import engine.world.WorldCoordinates

class ShopTemplate(
    @Json(name = "name")
    val name: String,
    @Json(name = "coordinates")
    val coordinatesString: String,
    @Json(name = "items")
    val itemStrings: List<String>
) {
    @Json(ignored = true)
    val coordinates = WorldCoordinates.parseFromString(coordinatesString)
    @Json(ignored = true)
    val soldItemTemplates = itemStrings.map { itemString ->
        ItemTemplates.find(itemString)
    }
}