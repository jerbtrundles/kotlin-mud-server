package engine.world

data class WorldCoordinates(
    val region: String,
    val subregion: Int,
    val room: Int
) {
    override fun toString(): String {
        return "[$region, $subregion, $room]"
    }

    companion object {
        fun parseFromString(str: String): WorldCoordinates {
            val tokens = str.split(",").map { it.trim() }
            return WorldCoordinates(
                region = tokens[0],
                subregion = tokens[1].toInt(),
                room = tokens[2].toInt()
            )
        }
    }
}