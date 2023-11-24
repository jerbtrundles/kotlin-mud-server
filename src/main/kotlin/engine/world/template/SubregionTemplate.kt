package engine.world.template

import com.beust.klaxon.Json
import engine.world.Subregion

class SubregionTemplate(
    @Json(name = "subregion-name")
    val name: String,
    @Json(name = "rooms")
    val roomTemplates: List<RoomTemplate>
) {
    fun toSubregion(id: Int): Subregion {
        return Subregion(
            id = id,
            name = name,
            rooms = roomTemplates.mapIndexed { i, roomTemplate -> roomTemplate.toRoom(id = i) }
        )
    }
}