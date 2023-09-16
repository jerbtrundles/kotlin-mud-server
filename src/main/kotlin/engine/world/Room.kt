package engine.world

import engine.Inventory
import engine.Message
import engine.Messages
import engine.entity.*
import engine.utility.Common
import engine.player.Player
import engine.game.GameInput
import engine.item.ItemWeapon
import engine.player.PlayerAction
import java.util.UUID

open class Room(
    val id: Int,
    val coordinates: WorldCoordinates,
    val description: String,
    val connections: List<Connection>,
    val inventory: Inventory = Inventory(),
    val entities: MutableList<EntityBase> = mutableListOf()
) {
    private val uuid = UUID.randomUUID()!!

    fun matchingConnectionOrNull(gameInput: GameInput) = connections.firstOrNull { it.equals(gameInput) }

    val monsters
        get() = entities.filter { it.faction.faction == EntityFactions.MONSTER }
    val npcs
        get() = entities.filter { it.faction.faction == EntityFactions.NPC }

    var players = mutableListOf<Player>()
    val hasNoEntities
        get() = entities.isEmpty() && players.isEmpty()

    private val directionalExitsString = "Obvious exits: " +
            connections.filter { connection ->
                connection.matchInput.action == PlayerAction.MOVE
            }.joinToString { connection ->
                connection.matchInput.suffix
            }

    private val entitiesString
        get() = if (entities.isEmpty()) {
            ""
        } else {
            "You also see " +
                    Common.collectionString(
                        itemStrings = entities.map { it.nameForCollectionString },
                        includeIndefiniteArticles = false
                    ) + ".\n"
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
        get() = if (inventory.items.isEmpty()) {
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

    fun containsLivingPlayer() = players.any { it.isAlive }
    fun containsLivingEntity() = entities.any { it.isAlive }

    fun addEntity(entity: EntityBase) {
        entities.add(entity)
        announceToAll(entity.arriveString)
    }

    fun removeEntity(entity: EntityBase, connection: Connection? = null) {
        entities.remove(entity)
        connection?.let {
            announceToAll(entity.departString(connection))
        }
    }

    fun addPlayer(player: Player) {
        players.add(player)
        announceToOthers(player, player.arriveString)
    }

    fun removePlayer(player: Player, connection: Connection? = null) {
        players.remove(player)
        connection?.let {
            announceToOthers(player, player.departString(connection))
        } ?: announceToOthers(player, Message.PLAYER_LEAVES_GAME, player.name)
    }

    fun firstLivingHostileToPlayerOrNull(keyword: String) =
        firstHostileToPlayerOrNull(keyword)?.let { it.isAlive }

    fun randomLivingHostileToPlayerOrNull(keyword: String) =
        entities.filter {
            it.isHostileTo(EntityFactions.PLAYER)
                    && !it.isDead
                    && it.matchesKeyword(keyword)
        }.randomOrNull()

    fun firstDeadAndUnsearchedHostileToPlayerOrNull(suffix: String) =
        entities.firstOrNull {
            it.matchesKeyword(suffix)
                    && it.isDead
                    && it.hasNotBeenSearched
        }

    fun randomLivingPlayerOrNull() =
        players.filter { it.isAlive }.randomOrNull()
    fun randomLivingHostileOrNull(faction: EntityFaction) =
        entities.filter {
            it.faction.isHostileTo(faction)
        }.randomOrNull()

    fun firstHostileToPlayerOrNull(keyword: String) =
        entities.firstOrNull {
            it.isHostileTo(EntityFactions.PLAYER)
                    && it.matchesKeyword(keyword)
        }

    val containsWeapon
        get() = inventory.items.any { it is ItemWeapon }

    // region uuid -> equality, hash code
    override fun hashCode() = uuid.hashCode()
    override fun equals(other: Any?) =
        uuid == (other as? Room)?.uuid
    // endregion

    // region announce
    fun announceToAll(str: String) =
        players.forEach { player ->
            player.sendToMe(str)
        }

    fun announceToAll(message: Message, vararg tokens: String) =
        players.forEach { player ->
            player.sendToMe(message, *tokens)
        }

    fun announceToOthers(player: Player, message: Message, vararg tokens: String) =
        players.filter { it != player }.forEach {
            it.sendToMe(message, *tokens)
        }

    fun announceToOthers(announcingPlayer: Player, str: String) =
        players.forEach {
            if (it != announcingPlayer) {
                it.sendToMe(str)
            }
        }

    fun announceToPlayer(player: Player, str: String) =
        player.sendMessage(str)
    fun announceToPlayer(player: Player, message: Message, vararg tokens: String) =
        announceToPlayer(player, Messages.get(message, *tokens))
    // endregion
}

// find an item, item comes with fluff text, maybe a story
// one-liners vs story
// a story has a collection of strings, play one string at a time, move to next, message cooldown, repeat until done
// story cooldown; don't play the same story over and over too quickly