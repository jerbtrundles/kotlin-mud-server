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
    private fun doMove(gameInput: GameInput) {
        // directional move vs general connection
        val matchingConnection =
            currentRoom.matchingConnectionOrNull(gameInput)
        matchingConnection?.let {
            currentRoom.removePlayer(this)
            coordinates = it.coordinates
            currentRoom.addPlayer(this)
            doLook()
        } ?: doUnknown()
    }
    // endregion

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
            sendMessage(it.description)
        } ?: doUnknown()

    private fun doLookInItemWithKeyword(keyword: String) =
        getTypedItemByKeyword<ItemContainer>(keyword)?.let {
            if (it.closed) {
                sendMessage(Message.ITEM_IS_CLOSED, it.name)
            } else {
                sendMessage(it.inventoryString)
            }
        } ?: doUnknown()

    private fun doLookCurrentRoom() {
        val region = World.regions[coordinates.region]
        val subregion = region.subregions[coordinates.subregion]
        val room = subregion.rooms[coordinates.room]

        sendMessage(Message.LOOK_CURRENT_ROOM, region.displayString, subregion.toString(), room.displayString)
    }
    // endregion

    // region player posture
    private fun doStand() =
        if (posture == EntityPosture.STANDING) {
            sendMessage(Message.PLAYER_ALREADY_STANDING)
        } else {
            posture = EntityPosture.STANDING
            sendMessage(Message.PLAYER_STANDS_UP)
        }

    private fun doSit() =
        if (posture == EntityPosture.SITTING) {
            sendMessage(Message.PLAYER_ALREADY_SITTING)
        } else {
            posture = EntityPosture.SITTING
            sendMessage(Message.PLAYER_SITS)
        }

    private fun doKneel() =
        if (posture == EntityPosture.KNEELING) {
            sendMessage(Message.PLAYER_ALREADY_KNEELING)
        } else {
            posture = EntityPosture.KNEELING
            sendMessage(Message.PLAYER_KNEELS)
        }

    private fun doLieDown() =
        if (posture == EntityPosture.LYING_DOWN) {
            sendMessage(Message.PLAYER_ALREADY_LYING_DOWN)
        } else {
            posture = EntityPosture.LYING_DOWN
            sendMessage(Message.PLAYER_LIES_DOWN)
        }
    // endregion

    // region item interactions
    private fun doPutItem(gameInput: GameInput) =
        // e.g. put sword in coffer
        if (gameInput.words[2] != "in") {
            doUnknown()
        } else {
            val container = getTypedItemByKeyword<ItemContainer>(gameInput.words[3])
            container?.let {
                if (it.closed) {
                    sendMessage(Message.ITEM_IS_CLOSED, it.name)
                } else {
                    getItemWithKeyword(gameInput.words[1])?.run {
                        container.inventory.items.add(this)
                        inventory.items.remove(this)
                        sendMessage(Message.PLAYER_PUTS_ITEM_IN_CONTAINER, it.name, container.name)
                    } ?: doUnknown()
                }
            } ?: doUnknown()
        }

    private fun doDropItem(gameInput: GameInput) =
        inventory.getItemByKeyword(gameInput.suffix)?.let {
            inventory.items.remove(it)
            currentRoom.inventory.items.add(it)
            sendMessage(Message.PLAYER_DROPS_ITEM, it.nameWithIndefiniteArticle)
        } ?: doUnknown()

    private fun doGetItemFromRoom(gameInput: GameInput) =
        currentRoom.inventory.getItemByKeyword(gameInput.suffix)?.let {
            inventory.items.add(it)
            currentRoom.inventory.items.remove(it)
            sendMessage(Message.PLAYER_GETS_ITEM, it.nameWithIndefiniteArticle)
        } ?: doUnknown()

    private fun doGetItemFromContainer(gameInput: GameInput) =
        // e.g. take potion from backpack
        if (gameInput.words[2] != "from") {
            doUnknown()
        } else {
            val container = getTypedItemByKeyword<ItemContainer>(gameInput.words[3])
            container?.let { validContainer ->
                validContainer.inventory.getItemByKeyword(gameInput.words[1])?.let { validItem ->
                    inventory.items.add(validItem)
                    container.inventory.items.remove(validItem)
                    sendMessage(Message.PLAYER_GETS_ITEM_FROM_CONTAINER, validContainer.name, validItem.name)
                } ?: doUnknown()
            } ?: doUnknown()
        }

    private fun doGetItem(gameInput: GameInput) =
        when (gameInput.words.size) {
            1 -> sendMessage(Message.GET_WHAT)
            2 -> doGetItemFromRoom(gameInput)
            4 -> doGetItemFromContainer(gameInput)
            else -> doUnknown()
        }
    // endregion

    // region consumables
    private fun doEat(gameInput: GameInput) =
        getTypedItemByKeyword<ItemFood>(gameInput.suffix)?.let {
            if (--it.bites == 0) {
                sendMessage(Message.PLAYER_EATS_FOOD_FINAL, it.name)
                inventory.items.remove(it)
            } else {
                sendMessage(Message.PLAYER_EATS_FOOD, it.name, Messages.biteString(it.bites))
            }
        } ?: doUnknown()


    private fun doDrink(gameInput: GameInput) =
        getTypedItemByKeyword<ItemDrink>(gameInput.suffix)?.let {
            if (--it.quaffs == 0) {
                sendMessage(Message.PLAYER_DRINKS_FINAL, it.name)
                inventory.items.remove(it)
            } else {
                sendMessage(Message.PLAYER_DRINKS, it.name, Messages.quaffString(it.quaffs))
            }
        } ?: doUnknown()
    // endregion

    // region containers
    private fun doOpenContainer(gameInput: GameInput) =
        getTypedItemByKeyword<ItemContainer>(gameInput.suffix)?.let {
            if (it.closed) {
                it.closed = false
                sendMessage(Message.PLAYER_OPENS_ITEM, it.name)

            } else {
                sendMessage(Message.ITEM_ALREADY_OPEN, it.name)
            }
        } ?: doUnknown()

    private fun doCloseContainer(gameInput: GameInput) =
        getTypedItemByKeyword<ItemContainer>(gameInput.suffix)?.let {
            if (it.closed) {
                sendMessage(Message.ITEM_ALREADY_CLOSED, it.name)
            } else {
                it.closed = true
                sendMessage(Message.PLAYER_CLOSES_ITEM, it.name)
            }
        } ?: doUnknown()
    // endregion

    // region single-line handlers
    private fun doShowBankAccountBalance() =
        sendMessage(bankAccountBalanceString)

    private fun doShowGold() = sendMessage(goldString)
    private fun doShowInventory() = sendMessage(inventoryString)
    private fun doShowHealth() = sendMessage(healthString)
    private fun doUnknown() = sendMessage(Message.UNHANDLED_PLAYER_INPUT)
    // endregion

    // region inventory helpers
    private fun getItemWithKeyword(keyword: String): ItemBase? =
        inventory.getItemByKeyword(keyword)
            ?: currentRoom.inventory.getItemByKeyword(keyword)

    private inline fun <reified T> getTypedItemByKeyword(keyword: String): T? =
        inventory.getTypedItemByKeyword<T>(keyword)
            ?: currentRoom.inventory.getTypedItemByKeyword<T>(keyword)
    // endregion

    // region equip/unequip
    private fun doShowEquipment() {
        weapon?.let {
            sendMessage(Message.PLAYER_SHOW_EQUIPPED_ITEM, it.nameWithIndefiniteArticle)
        } ?: sendMessage(Message.PLAYER_NO_WEAPON_EQUIPPED)

        armor?.let {
            sendMessage(Message.PLAYER_SHOW_EQUIPPED_ITEM, it.nameWithIndefiniteArticle)
        } ?: sendMessage(Message.PLAYER_NO_ARMOR_EQUIPPED)
    }

    private fun doRemoveEquipment(gameInput: GameInput) =
        if (weapon != null && weapon!!.keywords.contains(gameInput.words[1])) {
            val weaponToRemove = weapon!!
            inventory.items.add(weaponToRemove)
            weapon = null
            sendMessage(Message.PLAYER_REMOVES_ITEM, weaponToRemove.name)
        } else if (armor != null && armor!!.keywords.contains(gameInput.words[1])) {
            val armorToRemove = armor!!
            inventory.items.add(armorToRemove)
            armor = null
            sendMessage(Message.PLAYER_REMOVES_ITEM, armorToRemove.name)
        } else {
            doUnknown()
        }

    private fun doEquipItem(gameInput: GameInput) {
        // find weapon from player inventory
        inventory.getTypedItemByKeyword<ItemWeapon>(gameInput.words[1])?.let {
            doEquipWeaponFromPlayerInventory(it)
            // find weapon from current room
        } ?: currentRoom.inventory.getTypedItemByKeyword<ItemWeapon>(gameInput.words[1])?.let {
            doEquipWeaponFromCurrentRoom(it)
            // find armor from player inventory
        } ?: inventory.getTypedItemByKeyword<ItemArmor>(gameInput.words[1])?.let {
            doEquipArmorFromPlayerInventory(it)
            // find armor from current room
        } ?: currentRoom.inventory.getTypedItemByKeyword<ItemArmor>(gameInput.words[1])?.let {
            doEquipArmorFromCurrentRoom(it)
        } ?: doUnknown()
    }

    private fun doEquipWeaponFromCurrentRoom(weaponFromCurrentRoom: ItemWeapon) {
        weapon?.let { alreadyEquippedWeapon ->
            sendMessage(Message.PLAYER_ITEM_ALREADY_EQUIPPED, alreadyEquippedWeapon.nameWithIndefiniteArticle)
        } ?: {
            weapon = weaponFromCurrentRoom
            currentRoom.inventory.items.remove(weaponFromCurrentRoom)
            sendMessage(Message.PLAYER_PICKS_UP_AND_EQUIPS_ITEM, weaponFromCurrentRoom.name)
        }
    }

    private fun doEquipArmorFromCurrentRoom(armorFromCurrentRoom: ItemArmor) {
        armor?.let { alreadyEquippedArmor ->
            sendMessage(Message.PLAYER_ITEM_ALREADY_EQUIPPED, alreadyEquippedArmor.nameWithIndefiniteArticle)
        } ?: {
            armor = armorFromCurrentRoom
            currentRoom.inventory.items.remove(armorFromCurrentRoom)
            sendMessage(Message.PLAYER_PICKS_UP_AND_EQUIPS_ITEM, armorFromCurrentRoom.name)
        }
    }

    private fun doEquipWeaponFromPlayerInventory(weaponFromInventory: ItemWeapon) {
        weapon?.let { alreadyEquippedWeapon ->
            sendMessage(Message.PLAYER_ITEM_ALREADY_EQUIPPED, alreadyEquippedWeapon.nameWithIndefiniteArticle)
        } ?: {
            weapon = weaponFromInventory
            inventory.items.remove(weaponFromInventory)
            sendMessage(Message.PLAYER_EQUIPS_ITEM_FROM_INVENTORY, weaponFromInventory.name)
        }
    }

    private fun doEquipArmorFromPlayerInventory(armorFromInventory: ItemArmor) {
        armor?.let { alreadyEquippedArmor ->
            sendMessage(Message.PLAYER_ITEM_ALREADY_EQUIPPED, alreadyEquippedArmor.nameWithIndefiniteArticle)
        } ?: {
            armor = armorFromInventory
            inventory.items.remove(armorFromInventory)
            sendMessage(Message.PLAYER_EQUIPS_ITEM_FROM_INVENTORY, armorFromInventory.name)
        }
    }
    // endregion

    // region attack/search entities
    private fun doAttack(gameInput: GameInput) {
        currentRoom.randomLivingHostileOrNull(faction)?.let { hostile ->
            val weaponString = weapon?.name ?: "fists"
            val attack = attributes.strength + (weapon?.power ?: 0)
            val defense = hostile.attributes.baseDefense
            val damage = (attack - defense).coerceAtLeast(0)

            sendMessage(Message.PLAYER_ATTACKS_ENTITY_WITH_WEAPON, hostile.name, weaponString)

            if (damage > 0) {
                sendToMe(Message.PLAYER_HITS_FOR_DAMAGE, damage.toString())
                sendToOthers(Message.OTHER_PLAYER_HITS_FOR_DAMAGE, damage.toString())
            } else {
                sendToMe(Message.PLAYER_MISSES)
                sendToOthers(Message.OTHER_PLAYER_MISSES, name)
            }

            hostile.attributes.currentHealth -= damage
            if (hostile.attributes.currentHealth <= 0) {
                experience += hostile.experience

                currentRoom.announceToAll(Message.MONSTER_DIES, hostile.name)
                sendMessage(Message.PLAYER_GAINS_EXPERIENCE, hostile.experience.toString())
            }
        } ?: doUnknown()
    }

    fun sendToMe(str: String) = sendMessage(str)
    fun sendToMe(message: Message, vararg tokens: String) = sendMessage(message, *tokens)
    fun sendToOthers(message: Message, vararg tokens: String) = currentRoom.announceToOthers(this, message, *tokens)

    private fun doSearch(gameInput: GameInput) {
        currentRoom.firstDeadAndUnsearchedHostileToPlayerOrNull(gameInput.suffix)?.let { deadHostile ->
            sendMessage(Message.PLAYER_SEARCHES_DEAD_ENTITY, deadHostile.name)
            sendMessage(Message.PLAYER_FINDS_GOLD_ON_DEAD_ENTITY, deadHostile.gold.toString(), deadHostile.name)
            gold += deadHostile.gold
            sendMessage(goldString)

            if (deadHostile.inventory.items.isNotEmpty()) {
                currentRoom.inventory.items.addAll(deadHostile.inventory.items)
                currentRoom.announceToAll(
                    Message.ENTITY_DROPS_ITEM,
                    deadHostile.prefixedName,
                    deadHostile.inventory.collectionString
                )
            }

            deadHostile.hasNotBeenSearched = false
        } ?: doUnknown()
    }
    // endregion

    // region shops
    private fun doSellItem(gameInput: GameInput) {
        (currentRoom as? RoomShop)?.let { shop ->
            inventory.getItemByKeyword(gameInput.suffix)?.let { itemToSell ->
                inventory.items.remove(itemToSell)
                gold += itemToSell.sellValue
                sendMessage(
                    Message.PLAYER_SELLS_ITEM_TO_MERCHANT,
                    itemToSell.name,
                    itemToSell.sellValue.toString()
                )
                sendMessage(goldString)
            } ?: doUnknown()
        } ?: doRoomIsNotShop()
    }

    private fun doListItems() {
        (currentRoom as? RoomShop)?.let { shop ->
            sendMessage(shop.itemsString)
        } ?: doRoomIsNotShop()
    }

    private fun doRoomIsNotShop() = sendMessage(Message.PLAYER_ROOM_IS_NOT_SHOP)
    private fun doRoomIsNotBank() = sendMessage(Message.PLAYER_ROOM_IS_NOT_BANK)

    private fun doPriceItem(gameInput: GameInput) {
        (currentRoom as? RoomShop)?.run {
            inventory.getItemByKeyword(gameInput.suffix)?.let {
                sendMessage(Message.PLAYER_CAN_SELL_ITEM_HERE, it.name, it.sellValue.toString())
            } ?: doUnknown()
        } ?: doRoomIsNotShop()
    }

    private fun doBuyItem(gameInput: GameInput) {
        (currentRoom as? RoomShop)?.run {
            soldItemTemplates.firstOrNull { it.matches(gameInput.suffix) }?.let { template ->
                if (gold >= template.value) {
                    val item = template.createItem()
                    gold -= template.value
                    sendMessage(Message.PLAYER_BUYS_ITEM, item.nameWithIndefiniteArticle, item.value.toString())
                    sendMessage(goldString)
                    inventory.items.add(item)
                } else {
                    sendMessage(
                        Message.PLAYER_NOT_ENOUGH_GOLD_TO_BUY_ITEM,
                        gold.toString(),
                        template.name,
                        template.value.toString()
                    )
                }
            } ?: doUnknown()
        } ?: doRoomIsNotShop()
    }
    // endregion

    private fun doDepositMoney(gameInput: GameInput) {
        (currentRoom as? RoomBank)?.run {
            when (gameInput.words.size) {
                1 -> doUnknown()
                else -> {
                    gameInput.words[1].toIntOrNull()?.let { depositAmount ->
                        if (depositAmount > gold) {
                            sendMessage(Message.PLAYER_NOT_ENOUGH_GOLD_TO_DEPOSIT)
                        } else {
                            bankAccountBalance += depositAmount
                            gold -= depositAmount

                            sendMessage(Message.PLAYER_DEPOSITS_GOLD, depositAmount.toString())
                            sendMessage(Message.PLAYER_BANK_ACCOUNT_BALANCE, bankAccountBalance.toString())
                            sendMessage(Message.PLAYER_CURRENT_GOLD, gold.toString())
                        }
                    } ?: doUnknown()
                }
            }
        } ?: doRoomIsNotBank()
    }

    private fun doWithdrawMoney(gameInput: GameInput) {
        (currentRoom as? RoomBank)?.run {
            when (gameInput.words.size) {
                1 -> doUnknown()
                else -> {
                    gameInput.words[1].toIntOrNull()?.let { withdrawAmount ->
                        if (withdrawAmount > bankAccountBalance) {
                            sendMessage(Message.PLAYER_NOT_ENOUGH_GOLD_TO_WITHDRAW, gold.toString())
                        } else {
                            bankAccountBalance -= withdrawAmount
                            gold += withdrawAmount

                            sendMessage(Message.PLAYER_WITHDRAWS_GOLD, withdrawAmount.toString())
                            sendMessage(bankAccountBalanceString)
                            sendMessage(goldString)
                        }
                    } ?: doUnknown()
                }
            }
        } ?: doRoomIsNotBank()
    }

    private fun doCheckBankAccountBalance() {
        (currentRoom as? RoomBank)?.run {
            sendMessage(bankAccountBalanceString)
        } ?: doRoomIsNotBank()
    }

    private fun doAssess(gameInput: GameInput) {
        when (gameInput.words.size) {
            1 -> doUnknown()
            2 -> currentRoom.firstHostileToPlayerOrNull(gameInput.suffix)?.let { hostile ->
                Debug.assessSituations(this, hostile)
            } ?: doUnknown()

            else -> doUnknown()
        }
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

    fun sendMessage(str: String) = runBlocking {
        webSocketSession.send(str)
    }

    fun sendMessage(message: Message, vararg tokens: String) =
        sendMessage(Messages.get(message, *tokens))

    override fun equals(other: Any?): Boolean {
        return when {
            other is Player -> other.webSocketSession == webSocketSession
            else -> false
        }
    }
}