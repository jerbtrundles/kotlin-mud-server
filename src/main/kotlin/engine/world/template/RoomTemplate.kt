package engine.world.template

import engine.Inventory
import com.beust.klaxon.Json
import engine.world.*
import engine.world.template.ShopTemplates.templates

class RoomTemplate(
    @Json(name = "room-id")
    val id: Int,
    @Json(name = "room-coordinates")
    val coordinatesString: String,
    @Json(name = "room-description")
    val description: String,
    @Json(name = "room-connections")
    val connections: List<Connection>,
    @Json(name = "room-is-bank")
    val isBank: String = "false"
) {
    @Json(ignored = true)
    val coordinates = WorldCoordinates.parseFromString(coordinatesString)

    fun toRoom(): Room {
        val shopTemplate = templates.firstOrNull { shopTemplate ->
            shopTemplate.coordinates == coordinates
        }

        return if(shopTemplate != null) {
            // todo: fix to avoid passing in empty inventory; doesn't matter much either way
            RoomShop(id, coordinates, description, connections, Inventory(), shopTemplate.soldItemTemplates)
        } else if(isBank == "true") {
            RoomBank(id, coordinates, description, connections)
        } else {
            Room(id, coordinates, description, connections
            )
        }
    }
}