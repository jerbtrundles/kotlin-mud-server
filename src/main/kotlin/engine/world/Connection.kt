package engine.world

import com.beust.klaxon.Json
import engine.game.GameInput
import engine.game.MovementDirection

open class Connection(
    inputString: String
) {
    companion object {
        private const val separator = " - "
    }

    @Json(ignored = true)
    val coordinatesString = inputString.substringBefore(separator)
    @Json(ignored = true)
    val matchInputString = inputString.substringAfter(separator)

    @Json(ignored = true)
    val coordinates = WorldCoordinates.parseFromString(coordinatesString)
    @Json(ignored = true)
    val matchInput = GameInput(matchInputString)
    @Json(ignored = true)
    val direction = MovementDirection.parseFromString(matchInput.suffix)

    override fun toString() = "${coordinatesString}\n${matchInputString}"

    override fun equals(other: Any?): Boolean {
        return if(other is GameInput) {
            matchInput == other
        } else {
            super.equals(other)
        }
    }

    override fun hashCode(): Int {
        return coordinatesString.hashCode() * 31 + matchInputString.hashCode()
    }
}