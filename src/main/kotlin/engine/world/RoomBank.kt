package engine.world

import engine.Inventory

class RoomBank(
    id: Int,
    coordinates: WorldCoordinates,
    description: String,
    connections: List<Connection>,
    inventory: Inventory = Inventory()
): Room(id, coordinates, description, connections, inventory) {
    override fun toString() = "Bank: $coordinates"
}
