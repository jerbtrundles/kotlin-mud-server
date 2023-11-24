package engine.world.template

import com.beust.klaxon.Json
import engine.world.*

class RoomTemplate(
    @Json(name = "coordinates")
    val coordinatesString: String,
    @Json(name = "description")
    val description: String,
    @Json(name = "connections")
    val connectionStrings: List<String>,
    @Json(name = "is-shop")
    val isShop: String = "false",
    @Json(name = "is-bank")
    val isBank: String = "false"
) {
    @Json(ignored = true)
    val coordinates = WorldCoordinates.parseFromString(coordinatesString)
    @Json(ignored = true)
    val connections = connectionStrings.map { Connection(it) }

    fun toRoom(id: Int): Room {
        return if (isShop == "true") {
            val shopTemplate = ShopTemplates.templates.firstOrNull { shopTemplate ->
                shopTemplate.coordinates == coordinates
            } ?: throw Exception("Couldn't find matching shop template at coordinates: $coordinatesString.")

            RoomShop(id, coordinates, description, connections, shopTemplate.soldItemTemplates)
        } else if (isBank == "true") {
            RoomBank(id, coordinates, description, connections)
        } else {
            Room(id, coordinates, description, connections)
        }
    }
}