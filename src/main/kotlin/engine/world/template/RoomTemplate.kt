package engine.world.template

import com.beust.klaxon.Json
import engine.world.*

class RoomTemplate(
    @Json(name = "room-id")
    val id: Int,
    @Json(name = "room-coordinates")
    val coordinatesString: String,
    @Json(name = "room-description")
    val description: String,
    @Json(name = "room-connections")
    val connections: List<Connection>,
    @Json(name = "room-is-shop")
    val isShop: String = "false",
    @Json(name = "room-is-bank")
    val isBank: String = "false"
) {
    @Json(ignored = true)
    val coordinates = WorldCoordinates.parseFromString(coordinatesString)

    fun toRoom(): Room {
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