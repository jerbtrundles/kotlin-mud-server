package engine.world

import java.lang.StringBuilder

class Subregion(
    val id: Int,
    val name: String,
    val rooms: List<Room>
) {
    override fun toString() = name

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