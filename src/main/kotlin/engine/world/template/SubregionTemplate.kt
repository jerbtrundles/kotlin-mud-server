package engine.world.template

import com.beust.klaxon.Json
import engine.world.Subregion

class SubregionTemplate(
    @Json(name = "subregion-id")
    val id: Int,
    @Json(name = "subregion-name")
    val name: String,
    @Json(name = "subregion-rooms")
    val roomTemplates: List<RoomTemplate>
) {
    fun toSubregion(): Subregion {
        return Subregion(
            id = id,
            name = name,
            rooms = roomTemplates.map { roomTemplate -> roomTemplate.toRoom() }
        )
    }
}