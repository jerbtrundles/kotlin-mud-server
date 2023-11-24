package engine.world

import engine.utility.Common
import engine.world.template.RegionTemplate

object World {
    var regions = listOf<Region>()

    val void = Room(
        id = -1,
        coordinates = WorldCoordinates(-1, -1, -1),
        description = "A cold, dark region of nothingness.",
        connections = listOf(
            Connection("0, 0, 0 - go out")
        )
    )

    val zero
        get() = regions[0].subregions[0].rooms[0]

    fun getRoomFromCoordinates(coordinates: WorldCoordinates) =
        regions[coordinates.region]
            .subregions[coordinates.subregion]
            .rooms[coordinates.room]

    val allRooms
        get() = regions.flatMap { region ->
            region.subregions.flatMap { subregion ->
                subregion.rooms
            }
        }

    fun getRandomRoom() = allRooms.random()

    fun load(c: Class<() -> Unit>) {
        val regionTemplates = Common.parseArrayFromJson<RegionTemplate>(c, "/world.json")
        regions = regionTemplates.map { regionTemplate -> regionTemplate.toRegion() }
    }
}