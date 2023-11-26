package engine.world

import kotlin.text.StringBuilder

class Subregion(
    val id: Int,
    val name: String,
    val rooms: List<Room>
) {
    override fun toString() = "Subregion: $id - $name"
    val displayString = name

    fun toDebugString(): String {
        val sb = StringBuilder()
        sb.appendLine("world.Subregion ID: $id")
        sb.appendLine("world.Subregion name: $name")
        rooms.forEach { room ->
            sb.append(room)
        }
        return sb.toString()
    }
}