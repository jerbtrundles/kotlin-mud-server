package engine.world

class Region(
    val id: Int,
    val name: String,
    val subregions: List<Subregion>
) {
    override fun toString() = name
    val displayString = name

    fun toDebugString(): String {
        val sb = StringBuilder()
        sb.appendLine("world.Region ID: $id")
        sb.appendLine("world.Region name: $name")
        subregions.forEach { subregion ->
            sb.appendLine(subregion)
        }
        return sb.toString()
    }
}