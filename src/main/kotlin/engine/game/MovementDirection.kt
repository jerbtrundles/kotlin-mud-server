package engine.game

enum class MovementDirection {
    NORTHWEST,
    NORTH,
    NORTHEAST,
    WEST,
    OUT,
    EAST,
    SOUTHWEST,
    SOUTH,
    SOUTHEAST,
    NONE;

    companion object {
        fun parseFromString(str: String): MovementDirection {
            return when (str.removePrefix("go ")) {
                "northwest", "nw" -> NORTHWEST
                "north", "n" -> NORTH
                "northeast", "ne" -> NORTHEAST
                "west", "w" -> WEST
                "out", "o" -> OUT
                "east", "e" -> EAST
                "southwest", "sw" -> SOUTHWEST
                "south", "s" -> SOUTH
                "southeast", "se" -> SOUTHEAST
                else -> NONE
            }
        }

        fun isDirectionalWord(word: String): Boolean {
            return parseFromString(word) != NONE
        }
    }
}