package engine.player

import debug.Debug
import engine.Inventory
import engine.Message
import engine.Messages
import engine.entity.EntityAttributes
import engine.entity.EntityFaction
import engine.entity.EntityPosture
import engine.game.Game
import engine.game.GameInput
import engine.game.MovementDirection
import engine.item.*
import engine.utility.appendLine
import engine.world.*
import io.ktor.websocket.*
import kotlinx.coroutines.runBlocking

class Player(
    val name: String,
    private val webSocketSession: DefaultWebSocketSession
) {
    val attributes = EntityAttributes.player
    var coordinates = WorldCoordinates(0, 0, 0)
    var level = 1
    var experience = 0
    val currentRoom
        get() = World.getRoomFromCoordinates(coordinates)
    var posture = EntityPosture.STANDING
    val inventory: Inventory = Inventory()
    var weapon: ItemWeapon? = null
    var armor: ItemArmor? = null

    val faction = EntityFaction.factionPlayer

    var gold = 0
    var bankAccountBalance = 100
    val bankAccountBalanceString
        get() = Messages.get(Message.PLAYER_BANK_ACCOUNT_BALANCE, bankAccountBalance.toString())

    val isAlive
        get() = attributes.currentHealth > 0

    val arriveString = "$name has arrived."
    val healthString
        get() = "${attributes.healthString}\n${attributes.magicString}"
    val inventoryString
        get() = if (inventory.items.isEmpty()) {
            Messages.get(Message.PLAYER_NOT_CARRYING_ANYTHING)
        } else {
            "You are carrying ${inventory.collectionString}."
        }
    val goldString
        get() = Messages.get(Message.PLAYER_CURRENT_GOLD, gold.toString())

    // region move
    private fun doMove(gameInput: GameInput) =
        // directional move vs general connection
        currentRoom.matchingConnectionOrNull(gameInput)?.let {
            currentRoom.removePlayer(this)
            coordinates = it.coordinates
            currentRoom.addPlayer(this)

            sendCurrentRoomString()

            doLook()
        } ?: doUnknown()
    // endregion

    private fun sendCurrentRoomString() {
        val region = World.regions[coordinates.region]
        val subregion = region.subregions[coordinates.subregion]
        val room = subregion.rooms[coordinates.room]

        val message = "ROOM:" + Messages.get(
            Message.LOOK_CURRENT_ROOM,
            region.displayString,
            subregion.displayString,
            room.roomString
        )
        sendToMe(message)
    }

    fun departString(connection: Connection) =
        if (connection.direction != MovementDirection.NONE) {
            // The goblin heads east.
            "$name heads ${connection.direction.toString().lowercase()}."
        } else if (connection.matchInputString.contains("gates")) {
            // TODO: make this better
            // TODO: other cases for climbing, other connection types
            // TODO: The goblin heads through the gates.
            "$name heads through the town gates."
        } else {
            // TODO: make this better
            "$name heads over to the ${connection.matchInput.suffix}."
        }

    fun doInitialLook() {
        sendCurrentRoomString()
        doLook()
    }

    // region look
    private fun doLook(gameInput: GameInput? = null) =
        gameInput?.run {
            when (gameInput.words.size) {
                // "look"
                1 -> doLookCurrentRoom()
                // "look item"
                2 -> doLookAtItemWithKeyword(keyword = gameInput.words[1])
                // 3+ - "look at item", "look in item"
                else -> when (words[1]) {
                    "in" -> doLookInItemWithKeyword(keyword = gameInput.suffixAfterWord(1))
                    "at" -> doLookAtItemWithKeyword(keyword = gameInput.suffixAfterWord(1))
                    else -> doUnknown()
                }
            }
        } ?: doLookCurrentRoom()

    private fun doLookAtItemWithKeyword(keyword: String) =
        getItemWithKeyword(keyword)?.let {
            sendToMe(it.description)
        } ?: doUnknown()

    private fun doLookInItemWithKeyword(keyword: String) =
        inventory.getContainerWithKeywordOrNull(keyword)?.let {
            if (it.closed) {
                sendToMe(Message.ITEM_IS_CLOSED, it.name)
            } else {
                sendToMe(it.inventoryString)
            }
        } ?: doUnknown()

    private fun doLookCurrentRoom() {
        val region = World.regions[coordinates.region]
        val subregion = region.subregions[coordinates.subregion]
        val room = subregion.rooms[coordinates.room]

        sendToMe(Message.LOOK_CURRENT_ROOM, region.displayString, subregion.displayString, room.displayString)

        // refresh client entities list
        // called both when player says "look" and when player moves between rooms
        // TODO: ugly; make this better
        sendToMe(currentRoom.npcsTextString)
        sendToMe(currentRoom.monstersTextString)
    }
    // endregion

    // region player posture
    private fun doStand() =
        if (posture == EntityPosture.STANDING) {
            sendToMe(Message.PLAYER_ALREADY_STANDING)
        } else {
            posture = EntityPosture.STANDING
            sendToMe(Message.PLAYER_STANDS_UP)
            sendToOthers(Message.OTHER_PLAYER_STANDS_UP, name)
        }

    private fun doSit() =
        if (posture == EntityPosture.SITTING) {
            sendToMe(Message.PLAYER_ALREADY_SITTING)
        } else {
            posture = EntityPosture.SITTING
            sendToMe(Message.PLAYER_SITS)
            sendToOthers(Message.OTHER_PLAYER_SITS)
        }

    private fun doKneel() =
        if (posture == EntityPosture.KNEELING) {
            sendToMe(Message.PLAYER_ALREADY_KNEELING)
        } else {
            posture = EntityPosture.KNEELING
            sendToMe(Message.PLAYER_KNEELS)
            sendToOthers(Message.OTHER_PLAYER_KNEELS)
        }

    private fun doLieDown() =
        if (posture == EntityPosture.LYING_DOWN) {
            sendToMe(Message.PLAYER_ALREADY_LYING_DOWN)
        } else {
            posture = EntityPosture.LYING_DOWN
            sendToMe(Message.PLAYER_LIES_DOWN)
            sendToOthers(Message.OTHER_PLAYER_LIES_DOWN, name)
        }
    // endregion

    // region item interactions
    private fun doPutItem(gameInput: GameInput) =
        // e.g. put sword in coffer
        if (gameInput.words[2] != "in") {
            doUnknown()
        } else {
            inventory.getContainerWithKeywordOrNull(gameInput.words[3])?.let { container ->
                if (container.closed) {
                    sendToMe(Message.ITEM_IS_CLOSED, container.name)
                } else {
                    getItemWithKeyword(gameInput.words[1])?.let { item ->
                        container.inventory.items.add(item)
                        inventory.items.remove(item)
                        sendToMe(
                            Message.PLAYER_PUTS_ITEM_IN_CONTAINER,
                            item.nameWithIndefiniteArticle,
                            container.nameWithIndefiniteArticle
                        )
                        sendToOthers(
                            Message.OTHER_PLAYER_PUTS_ITEM_IN_CONTAINER,
                            name,
                            item.nameWithIndefiniteArticle,
                            container.nameWithIndefiniteArticle
                        )
                    } ?: doUnknown()
                }
            } ?: doUnknown()
        }

    private fun doDropItem(gameInput: GameInput) =
        inventory.getItemWithKeywordOrNull(gameInput.suffix)?.let {
            inventory.removeItem(it)
            currentRoom.addItem(it)
            sendToMe(Message.PLAYER_DROPS_ITEM, it.nameWithIndefiniteArticle)
            sendToOthers(Message.OTHER_PLAYER_DROPS_ITEM, name, it.nameWithIndefiniteArticle)
        } ?: doUnknown()

    private fun doGetItemFromRoom(gameInput: GameInput) =
        currentRoom.getItemWithKeywordOrNull(gameInput.suffix)?.let { item ->
            inventory.addItem(item)
            currentRoom.removeItem(item)
            sendToMe(Message.PLAYER_GETS_ITEM, item.nameWithIndefiniteArticle)
            sendToOthers(Message.OTHER_PLAYER_GETS_ITEM, item.nameWithIndefiniteArticle)
        } ?: doUnknown()

    private fun doGetItemFromContainer(gameInput: GameInput) =
        // e.g. take potion from backpack
        if (gameInput.words[2] != "from") {
            doUnknown()
        } else {
            inventory.getContainerWithKeywordOrNull(gameInput.words[3])?.let { container ->
                container.getAndRemoveItemWithKeywordOrNull(gameInput.words[1])?.let { item ->
                    inventory.addItem(item)

                    sendToMe(
                        Message.PLAYER_GETS_ITEM_FROM_CONTAINER,
                        container.nameWithIndefiniteArticle,
                        item.nameWithIndefiniteArticle
                    )
                    sendToOthers(
                        Message.OTHER_PLAYER_GETS_ITEM_FROM_CONTAINER,
                        name,
                        container.nameWithIndefiniteArticle,
                        item.nameWithIndefiniteArticle
                    )
                } ?: doUnknown()
            } ?: doUnknown()
        }

    private fun doGetItem(gameInput: GameInput) =
        when (gameInput.words.size) {
            1 -> sendToMe(Message.GET_WHAT)
            2 -> doGetItemFromRoom(gameInput)
            4 -> doGetItemFromContainer(gameInput)
            else -> doUnknown()
        }
    // endregion

    // region consumables
    private fun doEat(gameInput: GameInput) =
        inventory.getFoodWithKeywordOrNull(gameInput.suffix)?.let {
            if (--it.bites == 0) {
                inventory.items.remove(it)

                sendToMe(Message.PLAYER_EATS_FOOD_FINAL, it.name)
                sendToOthers(Message.OTHER_PLAYER_EATS_FOOD_FINAL, name, it.name)
            } else {
                sendToMe(Message.PLAYER_EATS_FOOD, it.name, Messages.biteString(it.bites))
                sendToOthers(Message.PLAYER_EATS_FOOD, name, it.name, Messages.biteString(it.bites))
            }
        } ?: doUnknown()

    private fun doDrink(gameInput: GameInput) =
        inventory.getDrinkWithKeywordOrNull(gameInput.suffix)?.let {
            if (--it.quaffs == 0) {
                inventory.items.remove(it)

                sendToMe(Message.PLAYER_DRINKS_FINAL, it.name)
                sendToOthers(Message.OTHER_PLAYER_DRINKS_FINAL, name, it.name)
            } else {
                sendToMe(Message.PLAYER_DRINKS, it.name, Messages.quaffString(it.quaffs))
                sendToOthers(Message.OTHER_PLAYER_DRINKS, name, it.name, Messages.quaffString(it.quaffs))
            }
        } ?: doUnknown()
    // endregion

    // region containers
    private fun doOpenContainer(gameInput: GameInput) =
        inventory.getContainerWithKeywordOrNull(gameInput.suffix)?.let {
            if (!it.closed) {
                sendToMe(Message.ITEM_ALREADY_OPEN, it.nameWithIndefiniteArticle)
            } else {
                it.closed = false
                sendToMe(Message.PLAYER_OPENS_ITEM, it.nameWithIndefiniteArticle)
                sendToOthers(Message.OTHER_PLAYER_OPENS_ITEM, name, it.nameWithIndefiniteArticle)
            }
        } ?: doUnknown()

    private fun doCloseContainer(gameInput: GameInput) =
        inventory.getContainerWithKeywordOrNull(gameInput.suffix)?.let {
            if (it.closed) {
                sendToMe(Message.ITEM_ALREADY_CLOSED, it.nameWithIndefiniteArticle)
            } else {
                it.closed = true
                sendToMe(Message.PLAYER_CLOSES_ITEM, it.nameWithIndefiniteArticle)
                sendToMe(Message.OTHER_PLAYER_CLOSES_ITEM, name, it.nameWithIndefiniteArticle)
            }
        } ?: doUnknown()
    // endregion

    // region single-line handlers
    private fun doShowBankAccountBalance() =
        sendToMe(bankAccountBalanceString)

    private fun doShowGold() = sendToMe(goldString)
    private fun doShowInventory() = sendToMe(inventoryString)
    private fun doShowHealth() = sendToMe(healthString)
    private fun doUnknown() = sendToMe(Message.UNHANDLED_PLAYER_INPUT)
    // endregion

    // region inventory helpers
    private fun getItemWithKeyword(keyword: String): ItemBase? =
        inventory.getItemWithKeywordOrNull(keyword)
            ?: currentRoom.getItemWithKeywordOrNull(keyword)
    // endregion

    // region equip/unequip
    private fun doShowEquipment() =
        with(StringBuilder()) {
            weapon?.let {
                appendLine(Message.PLAYER_SHOW_EQUIPPED_ITEM, it.nameWithIndefiniteArticle)
            } ?: appendLine(Message.PLAYER_NO_WEAPON_EQUIPPED)

            armor?.let {
                appendLine(Message.PLAYER_SHOW_EQUIPPED_ITEM, it.nameWithIndefiniteArticle)
            } ?: appendLine(Message.PLAYER_NO_ARMOR_EQUIPPED)

            sendToMe(this)
        }

    private fun doRemoveEquipment(gameInput: GameInput) =
        if (weapon != null && weapon!!.keywords.contains(gameInput.words[1])) {
            val weaponToRemove = weapon!!
            inventory.items.add(weaponToRemove)
            weapon = null
            sendToMe(Message.PLAYER_REMOVES_ITEM, weaponToRemove.name)
        } else if (armor != null && armor!!.keywords.contains(gameInput.words[1])) {
            val armorToRemove = armor!!
            inventory.items.add(armorToRemove)
            armor = null
            sendToMe(Message.PLAYER_REMOVES_ITEM, armorToRemove.name)
        } else {
            doUnknown()
        }

    private fun doEquipItem(gameInput: GameInput) =
        // find weapon from player inventory
        inventory.getAndRemoveWeaponWithKeywordOrNull(gameInput.words[1])?.let {
            doEquipWeaponFromPlayerInventory(it)
            // find weapon from current room
        } ?: currentRoom.getAndRemoveWeaponWithKeywordOrNull(gameInput.words[1])?.let {
            doEquipWeaponFromCurrentRoom(it)
            // find armor from player inventory
        } ?: inventory.getAndRemoveArmorWithKeywordOrNull(gameInput.words[1])?.let {
            doEquipArmorFromPlayerInventory(it)
            // find armor from current room
        } ?: currentRoom.getAndRemoveArmorWithKeywordOrNull(gameInput.words[1])?.let {
            doEquipArmorFromCurrentRoom(it)
        } ?: doUnknown()

    private fun doEquipWeaponFromCurrentRoom(weaponFromCurrentRoom: ItemWeapon) {
        weapon?.let { alreadyEquippedWeapon ->
            sendToMe(Message.PLAYER_ITEM_ALREADY_EQUIPPED, alreadyEquippedWeapon.nameWithIndefiniteArticle)
        } ?: {
            weapon = weaponFromCurrentRoom
            currentRoom.removeItem(weaponFromCurrentRoom)

            sendToMe(Message.PLAYER_PICKS_UP_AND_EQUIPS_ITEM, weaponFromCurrentRoom.name)
            sendToOthers(Message.OTHER_PLAYER_PICKS_UP_AND_EQUIPS_ITEM, name, weaponFromCurrentRoom.name)
        }
    }

    private fun doEquipArmorFromCurrentRoom(armorFromCurrentRoom: ItemArmor) {
        armor?.let { alreadyEquippedArmor ->
            sendToMe(Message.PLAYER_ITEM_ALREADY_EQUIPPED, alreadyEquippedArmor.nameWithIndefiniteArticle)
        } ?: {
            armor = armorFromCurrentRoom
            currentRoom.removeItem(armorFromCurrentRoom)

            sendToMe(Message.PLAYER_PICKS_UP_AND_EQUIPS_ITEM, armorFromCurrentRoom.name)
            sendToOthers(Message.OTHER_PLAYER_PICKS_UP_AND_EQUIPS_ITEM, name, armorFromCurrentRoom.name)
        }
    }

    private fun doEquipWeaponFromPlayerInventory(weaponFromInventory: ItemWeapon) {
        weapon?.let { alreadyEquippedWeapon ->
            sendToMe(Message.PLAYER_ITEM_ALREADY_EQUIPPED, alreadyEquippedWeapon.nameWithIndefiniteArticle)
        } ?: {
            weapon = weaponFromInventory
            inventory.items.remove(weaponFromInventory)

            sendToMe(Message.PLAYER_EQUIPS_ITEM_FROM_INVENTORY, weaponFromInventory.nameWithIndefiniteArticle)
            sendToOthers(
                Message.OTHER_PLAYER_EQUIPS_ITEM_FROM_INVENTORY,
                name,
                weaponFromInventory.nameWithIndefiniteArticle
            )
        }
    }

    private fun doEquipArmorFromPlayerInventory(armorFromInventory: ItemArmor) {
        armor?.let { alreadyEquippedArmor ->
            sendToMe(Message.PLAYER_ITEM_ALREADY_EQUIPPED, alreadyEquippedArmor.nameWithIndefiniteArticle)
        } ?: {
            armor = armorFromInventory
            inventory.items.remove(armorFromInventory)

            sendToMe(Message.PLAYER_EQUIPS_ITEM_FROM_INVENTORY, armorFromInventory.nameWithIndefiniteArticle)
            sendToMe(
                Message.OTHER_PLAYER_EQUIPS_ITEM_FROM_INVENTORY,
                name,
                armorFromInventory.nameWithIndefiniteArticle
            )
        }
    }
    // endregion

    // region attack/search entities
    private fun doAttack(gameInput: GameInput) {
        currentRoom.randomLivingHostileOrNull(faction, gameInput.suffix)?.let { livingHostile ->
            val messageToMe = StringBuilder()
            val messageToOthers = StringBuilder()

            val weaponString = weapon?.name ?: "fists"
            val attack = attributes.strength + (weapon?.power ?: 0)
            val defense = livingHostile.attributes.baseDefense
            val damage = (attack - defense).coerceAtLeast(0)

            messageToMe.appendLine(Message.PLAYER_ATTACKS_ENTITY_WITH_WEAPON, livingHostile.name, weaponString)

            if (damage > 0) {
                messageToMe.appendLine(Message.PLAYER_HITS_FOR_DAMAGE, damage.toString())
                messageToOthers.appendLine(Message.OTHER_PLAYER_HITS_FOR_DAMAGE, damage.toString())
            } else {
                messageToMe.appendLine(Message.PLAYER_MISSES)
                messageToOthers.appendLine(Message.OTHER_PLAYER_MISSES, name)
            }

            livingHostile.attributes.currentHealth -= damage
            if (livingHostile.attributes.currentHealth <= 0) {
                messageToMe.appendLine(Message.ENTITY_DIES, livingHostile.name)
                messageToOthers.appendLine(Message.ENTITY_DIES, livingHostile.name)

                experience += livingHostile.experience
                messageToMe.appendLine(Message.PLAYER_GAINS_EXPERIENCE, livingHostile.experience.toString())
            }

            sendToMe(messageToMe)
            sendToOthers(messageToOthers)
        } ?: doUnknown()
    }

    private fun doSearch(gameInput: GameInput) =
        currentRoom.firstDeadAndUnsearchedHostileToPlayerOrNull(gameInput.suffix)?.let { deadHostile ->
            deadHostile.hasNotBeenSearched = false

            val toMe =
                StringBuilder().appendLine(Message.PLAYER_SEARCHES_DEAD_ENTITY, deadHostile.name)
            val toOthers =
                StringBuilder().appendLine(Message.OTHER_PLAYER_SEARCHES_DEAD_ENTITY, name, deadHostile.name)

            if (deadHostile.gold > 0) {
                toMe.appendLine(Message.PLAYER_FINDS_GOLD_ON_DEAD_ENTITY, deadHostile.gold.toString(), deadHostile.name)
                toOthers.appendLine(Message.OTHER_ENTITY_FINDS_GOLD)

                gold += deadHostile.gold
                toMe.appendLine(goldString)
            }

            if (deadHostile.inventory.items.isNotEmpty()) {
                currentRoom.addInventory(deadHostile.inventory)

                with(
                    Messages.get(
                        Message.ENTITY_DROPS_ITEM,
                        deadHostile.prefixedFullName,
                        deadHostile.inventory.collectionString
                    )
                ) {
                    toMe.appendLine(this)
                    toOthers.appendLine(this)
                }
            }

            sendToMe(toMe.toString())
            sendToOthers(toOthers.toString())
        } ?: doUnknown()
    // endregion

    // region shops
    private fun doSellItem(gameInput: GameInput) =
        (currentRoom as? RoomShop)?.let {
            inventory.getItemWithKeywordOrNull(gameInput.suffix)?.let { itemToSell ->
                inventory.items.remove(itemToSell)
                gold += itemToSell.sellValue

                with(StringBuilder()) {
                    appendLine(Message.PLAYER_SELLS_ITEM_TO_MERCHANT, itemToSell.name, itemToSell.sellValue.toString())
                    appendLine(goldString)
                    sendToMe(this)
                }

                sendToOthers(Message.OTHER_ENTITY_DOES_COMMERCE)
            } ?: doUnknown()
        } ?: doRoomIsNotShop()

    private fun doListItems() =
        (currentRoom as? RoomShop)?.let { shop ->
            sendToMe(shop.itemsString)
        } ?: doRoomIsNotShop()

    private fun doRoomIsNotShop() = sendToMe(Message.PLAYER_ROOM_IS_NOT_SHOP)
    private fun doRoomIsNotBank() = sendToMe(Message.PLAYER_ROOM_IS_NOT_BANK)

    private fun doPriceItem(gameInput: GameInput) =
        (currentRoom as? RoomShop)?.run {
            inventory.getItemWithKeywordOrNull(gameInput.suffix)?.let {
                sendToMe(Message.PLAYER_CAN_SELL_ITEM_HERE, it.name, it.sellValue.toString())
            } ?: doUnknown()
        } ?: doRoomIsNotShop()

    private fun doBuyItem(gameInput: GameInput) =
        // TODO: messaging to other players
        (currentRoom as? RoomShop)?.let { shop ->
            shop.soldItemTemplates.firstOrNull { it.matches(gameInput.suffix) }?.let { template ->
                with(StringBuilder()) {
                    if (gold >= template.value) {
                        val item = template.createItem()
                        gold -= template.value
                        appendLine(Message.PLAYER_BUYS_ITEM, item.nameWithIndefiniteArticle, item.value.toString())
                        appendLine(goldString)
                        inventory.items.add(item)
                    } else {
                        appendLine(
                            Message.PLAYER_NOT_ENOUGH_GOLD_TO_BUY_ITEM,
                            gold.toString(),
                            template.name,
                            template.value.toString()
                        )
                    }

                    sendToMe(this)
                }
            } ?: doUnknown()
        } ?: doRoomIsNotShop()
    // endregion

    private fun doDepositMoney(gameInput: GameInput) =
        (currentRoom as? RoomBank)?.run {
            when (gameInput.words.size) {
                1 -> doUnknown()
                else -> {
                    gameInput.words[1].toIntOrNull()?.let { depositAmount ->
                        sendToOthers(Message.OTHER_ENTITY_DOES_BANKING, name)

                        with(StringBuilder()) {
                            if (depositAmount > gold) {
                                appendLine(Message.PLAYER_NOT_ENOUGH_GOLD_TO_DEPOSIT)
                            } else {
                                bankAccountBalance += depositAmount
                                gold -= depositAmount

                                appendLine(Message.PLAYER_DEPOSITS_GOLD, depositAmount.toString())
                                appendLine(Message.PLAYER_BANK_ACCOUNT_BALANCE, bankAccountBalance.toString())
                                appendLine(Message.PLAYER_CURRENT_GOLD, gold.toString())
                            }

                            sendToMe(this)
                        }
                    } ?: doUnknown()
                }
            }
        } ?: doRoomIsNotBank()

    private fun doWithdrawMoney(gameInput: GameInput) =
        (currentRoom as? RoomBank)?.let {
            when (gameInput.words.size) {
                1 -> doUnknown()
                else -> {
                    gameInput.words[1].toIntOrNull()?.let { withdrawAmount ->
                        sendToOthers(Message.OTHER_ENTITY_DOES_BANKING, name)

                        with(StringBuilder()) {
                            if (withdrawAmount > bankAccountBalance) {
                                appendLine(Message.PLAYER_NOT_ENOUGH_GOLD_TO_WITHDRAW, gold.toString())
                            } else {
                                bankAccountBalance -= withdrawAmount
                                gold += withdrawAmount

                                appendLine(Message.PLAYER_WITHDRAWS_GOLD, withdrawAmount.toString())
                                appendLine(bankAccountBalanceString)
                                appendLine(goldString)
                            }

                            sendToMe(this)
                        }
                    } ?: doUnknown()
                }
            }
        } ?: doRoomIsNotBank()

    private fun doCheckBankAccountBalance() =
        (currentRoom as? RoomBank)?.let {
            sendToMe(bankAccountBalanceString)
            sendToOthers(Message.OTHER_ENTITY_DOES_BANKING)
        } ?: doRoomIsNotBank()

    private fun doAssess(gameInput: GameInput) =
        when (gameInput.words.size) {
            1 -> doUnknown()
            2 -> currentRoom.firstHostileToPlayerOrNull(gameInput.suffix)?.let { hostile ->
                Debug.assessSituations(this, hostile)
            } ?: doUnknown()

            else -> doUnknown()
        }

    fun onInput(input: String) = onInput(GameInput(input))
    fun onInput(gameInput: GameInput) {
        when (gameInput.action) {
            PlayerAction.ATTACK -> doAttack(gameInput)
            PlayerAction.EQUIP_ITEM -> doEquipItem(gameInput)
            PlayerAction.REMOVE_EQUIPMENT -> doRemoveEquipment(gameInput)

            PlayerAction.BUY_ITEM -> doBuyItem(gameInput)
            PlayerAction.SELL_ITEM -> doSellItem(gameInput)

            PlayerAction.GET_ITEM -> doGetItem(gameInput)
            PlayerAction.DROP_ITEM -> doDropItem(gameInput)
            PlayerAction.PUT_ITEM -> doPutItem(gameInput)

            PlayerAction.EAT -> doEat(gameInput)
            PlayerAction.DRINK -> doDrink(gameInput)

            PlayerAction.OPEN_CONTAINER -> doOpenContainer(gameInput)
            PlayerAction.CLOSE_CONTAINER -> doCloseContainer(gameInput)

            PlayerAction.SIT -> doSit()
            PlayerAction.STAND -> doStand()
            PlayerAction.KNEEL -> doKneel()
            PlayerAction.LIE_DOWN -> doLieDown()

            PlayerAction.LOOK -> doLook(gameInput)
            PlayerAction.MOVE -> doMove(gameInput)

            PlayerAction.WITHDRAW_MONEY -> doWithdrawMoney(gameInput)
            PlayerAction.DEPOSIT_MONEY -> doDepositMoney(gameInput)
            PlayerAction.CHECK_BANK_ACCOUNT_BALANCE -> doCheckBankAccountBalance()

            PlayerAction.SEARCH -> doSearch(gameInput)
            PlayerAction.CHECK_GOLD -> doShowGold()
            PlayerAction.SHOW_EQUIPMENT -> doShowEquipment()
            PlayerAction.SHOW_HEALTH -> doShowHealth()
            PlayerAction.SHOW_INVENTORY -> doShowInventory()

            PlayerAction.LIST_ITEMS -> doListItems()
            PlayerAction.PRICE_ITEM -> doPriceItem(gameInput)

            // assess monster situations
            PlayerAction.ASSESS -> doAssess(gameInput)

            PlayerAction.QUIT -> Game.running = false

            PlayerAction.NONE -> doUnknown()

            else -> doUnknown()
        }
    }

    fun sendToMe(message: Message, vararg tokens: String) =
        sendToMe(Messages.get(message, *tokens))

    fun sendToMe(str: String) = runBlocking {
        webSocketSession.send(str)
    }

    fun sendToOthers(message: Message, vararg tokens: String) =
        sendToOthers(Messages.get(message, *tokens))

    fun sendToOthers(str: String) =
        currentRoom.sendToOthers(this, str)

    fun sendToAll(message: Message, vararg tokens: String) =
        currentRoom.sendToAll(Messages.get(message, *tokens))

    fun sendToAll(str: String) =
        currentRoom.sendToAll(str)

    fun sendToMe(sb: StringBuilder) =
        sendToMe(sb.trim('\n').toString())

    fun sendToOthers(sb: StringBuilder) =
        sendToOthers(sb.trim('\n').toString())

    override fun equals(other: Any?) =
        when {
            other is Player -> other.webSocketSession == webSocketSession
            else -> false
        }
}
