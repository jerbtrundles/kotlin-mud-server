package engine.world

import engine.*
import engine.entity.*
import engine.utility.Common
import engine.player.Player
import engine.game.GameInput
import engine.item.ItemBase
import engine.player.PlayerAction
import java.util.UUID

open class Room(
    val id: Int,
    val coordinates: WorldCoordinates,
    val description: String,
    val connections: List<Connection>,
    val entities: MutableList<EntityBase> = mutableListOf()
) {
    private val uuid = UUID.randomUUID()!!
    private val inventory = Inventory()
    val containsItem
        get() = inventory.isNotEmpty()
    val containsValuableItem
        get() = inventory.containsValuableItem

    // region entities
    val monsters
        get() = entities.filter { it.faction.faction == EntityFactions.MONSTER }
    val npcs
        get() = entities.filter { it.faction.faction == EntityFactions.NPC }
    var players = mutableListOf<Player>()
    val hasNoEntities
        get() = entities.isEmpty() && players.isEmpty()

    fun containsLivingPlayer() = players.any { it.isAlive }
    fun containsLivingEntity() = entities.any { it.isAlive }

    fun addEntity(entity: EntityBase) {
        entities.add(entity)
        sendToAll(entity.arriveString)

        sendEntitiesStringsToPlayers()
    }
    // endregion

    // region strings
    private val directionalExitsString = "Obvious exits: " +
            connections.filter { connection ->
                connection.matchInput.action == PlayerAction.MOVE
            }.joinToString { connection ->
                connection.matchInput.suffix
            }

    private val npcsString: String
        get() = if (npcs.isEmpty()) {
            ""
        } else {
            "You also see " +
                    Common.collectionString(
                        itemStrings = npcs.map { npc -> npc.nameForCollectionString },
                        includeIndefiniteArticles = false
                    ) + ".\n"
        }

    private val monstersString: String
        get() = if (monsters.isEmpty()) {
            ""
        } else {
            "You also see " +
                    Common.collectionString(
                        monsters.map { monster -> monster.nameForCollectionString }
                    ) + ".\n"
        }

    private val inventoryString: String
        get() = if (inventory.isEmpty()) {
            ""
        } else {
            "You also see ${inventory.collectionString}.\n"
        }

    override fun toString() = "Room: $coordinates"

    val displayString
        get() = StringBuilder()
            .appendLine(description)
            .append(inventoryString)
            .append(npcsString)
            .append(monstersString)
            .append(directionalExitsString)
            .toString()

    val roomString
        get() = StringBuilder()
            .appendLine(description)
            .append(directionalExitsString)
            .toString()

    val npcsTextString
        get() = "NPCS:${npcs.joinToString(separator = "\n") { it.nameForCollectionString }}"
    val monstersTextString
        get() = "MONSTERS:${monsters.joinToString(separator = "\n") { it.nameForCollectionString }}"
    val itemsTextString
        get() = inventory.itemsTextString
    // endregion

    // region player
    fun sendDebugTextString(str: String) =
        sendToAll("DEBUG:$str")

    fun sendEntitiesStringsToPlayers() {
        sendToAll(npcsTextString)
        sendToAll(monstersTextString)
    }
    fun addPlayer(player: Player) {
        players.add(player)
        player.sendToMe(itemsTextString)
        sendToOthers(player, player.arriveString)
    }

    fun removePlayer(player: Player, connection: Connection? = null) {
        players.remove(player)
        connection?.let {
            sendToOthers(player, player.departString(connection))
        } ?: sendToOthers(player, Message.PLAYER_LEAVES_GAME, player.name)
    }
    fun firstHostileToPlayerOrNull(keyword: String) =
        entities.firstOrNull {
            it.isHostileTo(EntityFactions.PLAYER)
                    && it.matchesKeyword(keyword)
        }
    fun firstDeadAndUnsearchedHostileToPlayerOrNull(suffix: String) =
        entities.firstOrNull {
            it.matchesKeyword(suffix)
                    && it.isDead
                    && it.hasNotBeenSearched
        }

    fun randomLivingPlayerOrNull() =
        players.filter { it.isAlive }.randomOrNull()
    // endregion

    // region entities
    fun randomLivingHostileOrNull(faction: EntityFaction) =
        entities.filter {
            it.faction.isHostileTo(faction)
        }.randomOrNull()

    fun randomLivingHostileOrNull(faction: EntityFaction, keyword: String) =
        entities.filter {
            it.faction.isHostileTo(faction)
                    && it.matchesKeyword(keyword)
        }.randomOrNull()
    fun removeEntity(entity: EntityBase, connection: Connection? = null) {
        entities.remove(entity)
        if(!entity.isDead) {
            sendToAll(entity.departString(connection))
        }
        sendEntitiesStringsToPlayers()
    }
    // endregion

    // region contains item type
    fun containsWeapon() =
        inventory.containsWeapon()

    fun containsArmor() =
        inventory.containsArmor()

    fun containsFood() =
        inventory.containsFood()

    fun containsDrink() =
        inventory.containsDrink()

    fun containsJunk() =
        inventory.containsJunk()

    fun containsContainer() =
        inventory.containsContainer()
    // endregion

    // region uuid -> equality, hash code
    override fun hashCode() = uuid.hashCode()
    override fun equals(other: Any?) =
        uuid == (other as? Room)?.uuid
    // endregion

    // region announce
    fun sendToAll(str: String) =
        players.forEach { player ->
            player.sendToMe(str)
        }

    fun sendToAll(message: Message, vararg tokens: String) =
        sendToAll(Messages.get(message, *tokens))

    fun sendToOthers(announcingPlayer: Player, message: Message, vararg tokens: String) =
        sendToOthers(announcingPlayer, Messages.get(message, *tokens))

    fun sendToOthers(announcingPlayer: Player, str: String) =
        players.forEach {
            if (it != announcingPlayer) {
                it.sendToMe(str)
            }
        }
    // endregion

    // region add/remove item/inventory
    private fun sendItemListToPlayer() = sendToAll(inventory.itemsTextString)
    fun addItem(item: ItemBase) {
        inventory.addItem(item)
        sendItemListToPlayer()
    }

    fun removeItem(item: ItemBase) {
        inventory.removeItem(item)
        sendItemListToPlayer()
    }

    fun addInventory(other: Inventory) {
        inventory.addInventory(other)
        sendItemListToPlayer()
    }
    // endregion

    // region items
    fun getAndRemoveRandomValuableItemOrNull() =
        inventory.getAndRemoveRandomValuableItem()?.let {
            sendItemListToPlayer()
            it
        }

    fun getAndRemoveRandomWeaponOrNull() =
        inventory.getAndRemoveRandomWeaponOrNull()?.let {
            sendItemListToPlayer()
            it
        }

    fun getAndRemoveRandomArmorOrNull() =
        inventory.getAndRemoveRandomArmorOrNull()?.let {
            sendItemListToPlayer()
            it
        }

    fun getAndRemoveRandomBetterArmorOrNull(minRequiredDefense: Int) =
        inventory.getAndRemoveRandomBetterArmorOrNull(minRequiredDefense)?.let {
            sendItemListToPlayer()
            it
        }

    fun getAndRemoveRandomItemOrNull() =
        inventory.getAndRemoveRandomItem()?.let {
            sendItemListToPlayer()
            it
        }

    fun getBestWeaponOrNull() = inventory.getBestWeaponOrNull()
    fun getBestArmorOrNull() = inventory.getBestArmorOrNull()
    fun getRandomFoodOrNull() = inventory.getRandomFoodOrNull()
    fun getRandomDrinkOrNull() = inventory.getRandomDrinkOrNull()
    fun getAndRemoveRandomBetterWeaponOrNull(minRequiredPower: Int) =
        inventory.getAndRemoveRandomBetterWeaponOrNull(minRequiredPower)?.let {
            sendItemListToPlayer()
            it
        }

    fun getItemWithKeywordOrNull(keyword: String) = inventory.getItemWithKeywordOrNull(keyword)
    fun getAndRemoveWeaponWithKeywordOrNull(keyword: String) = inventory.getAndRemoveWeaponWithKeywordOrNull(keyword)
    fun getAndRemoveArmorWithKeywordOrNull(keyword: String) = inventory.getAndRemoveArmorWithKeywordOrNull(keyword)
    // endregion

    fun matchingConnectionOrNull(gameInput: GameInput) =
        connections.firstOrNull { it.equals(gameInput) }
}

// find an item, item comes with fluff text, maybe a story
// one-liners vs story
// a story has a collection of strings, play one string at a time, move to next, message cooldown, repeat until done
// story cooldown; don't play the same story over and over too quickly