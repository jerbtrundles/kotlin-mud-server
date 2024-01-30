package engine.world

import debug.Debug
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.streams.asSequence

object World {
    var regions = mapOf<String, Region>()

    val void = Room(
        id = -1,
        coordinates = WorldCoordinates("", -1, -1),
        description = "A cold, dark region of nothingness.",
        connections = listOf(
            Connection("0, 0, 0 - go out")
        )
    )

    val zero
        get() = regions["town"]!!.subregions[0].rooms[0]

    fun getRoomFromCoordinates(coordinates: WorldCoordinates): Room? =
        try {
            regions[coordinates.region]?.subregions?.get(coordinates.subregion)?.rooms?.get(coordinates.room)
        } catch (e: Exception) {
            Debug.println("BAD COORDINATES: $coordinates")
            null
        }

    val allRooms
        get() = regions.values.flatMap { region ->
            region.subregions.flatMap { subregion ->
                subregion.rooms
            }
        }

    fun getRandomRoom() = allRooms.random()

    fun load(c: Class<() -> Unit>) {
        val resourcePath = Paths.get(c.getResource("/world/")!!.toURI())
        regions = Files.walk(resourcePath)
            .use { paths ->
                paths
                    .filter { Files.isRegularFile(it) }
                    .map { filePath ->
                        val relativePath = "/world/" + resourcePath.relativize(filePath).toString()
                        val region = Region.fromFilePath(c, relativePath)
                        region.id to region
                    }
                    .asSequence()
                    .toMap()
            }
    }
}