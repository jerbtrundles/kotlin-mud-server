package engine.world

import engine.Inventory
import engine.item.template.ItemTemplate

class RoomShop(
    id: Int,
    coordinates: WorldCoordinates,
    description: String,
    connections: List<Connection>,
    inventory: Inventory = Inventory(),
    val soldItemTemplates: List<ItemTemplate>
): Room(id, coordinates, description, connections, inventory) {
    override fun toString() = "Shop: $coordinates"

    val itemsString: String
        get() {
            val sb = StringBuilder()
            sb.appendLine("This shop has the following items for sale:")
            soldItemTemplates.forEach { template ->
                sb.appendLine(template.shopItemString)
            }
            return sb.toString()
        }
}