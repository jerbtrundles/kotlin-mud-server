package engine.world

import com.beust.klaxon.Json
import engine.game.GameInput
import engine.game.MovementDirection

open class Connection(
    @Json(name = "connection-coordinates")
    val coordinatesString: String,
    @Json(name = "connection-input")
    val matchInputString: String,
) {
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