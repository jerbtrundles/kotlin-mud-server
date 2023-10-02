package engine.world

class RoomBank(
    id: Int,
    coordinates: WorldCoordinates,
    description: String,
    connections: List<Connection>
): Room(id, coordinates, description, connections) {
    override fun toString() = "Bank: $coordinates"
}
