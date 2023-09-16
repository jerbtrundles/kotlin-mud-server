package engine.player

enum class PlayerAction {
    NONE,
    LOOK,
    MOVE,
    GET_ITEM,
    DROP_ITEM,
    SHOW_EQUIPMENT,
    PUT_ITEM,
    OPEN_CONTAINER,
    CLOSE_CONTAINER,
    EQUIP_ITEM,
    REMOVE_EQUIPMENT,
    SHOW_INVENTORY,
    EAT,
    DRINK,
    BUY_ITEM,
    SELL_ITEM,
    PRICE_ITEM,
    LIST_ITEMS,
    CHECK_GOLD,
    SIT,
    STAND,
    KNEEL,
    LIE_DOWN,
    ATTACK,
    SHOW_HEALTH,
    SEARCH,
    DEPOSIT_MONEY,
    WITHDRAW_MONEY,
    CHECK_BANK_ACCOUNT_BALANCE,
    ASSESS,
    QUIT;

    companion object {
        fun fromString(str: String) = when (str) {
            "look", "l" -> LOOK
            "go", "move" -> MOVE
            "get", "take" -> GET_ITEM
            "drop" -> DROP_ITEM
            "equipment" -> SHOW_EQUIPMENT
            "put" -> PUT_ITEM
            "close" -> CLOSE_CONTAINER
            "open" -> OPEN_CONTAINER
            "equip", "wear" -> EQUIP_ITEM
            "remove", "unequip" -> REMOVE_EQUIPMENT
            "inventory", "i" -> SHOW_INVENTORY
            "eat" -> EAT
            "drink", "quaff" -> DRINK
            "buy" -> BUY_ITEM
            "sell" -> SELL_ITEM
            "price" -> PRICE_ITEM
            "list" -> LIST_ITEMS
            "gold" -> CHECK_GOLD
            "sit" -> SIT
            "stand" -> STAND
            "kneel" -> KNEEL
            "attack", "kill" -> ATTACK
            "health" -> SHOW_HEALTH
            "search" -> SEARCH
            "quit", "exit", "q", "x" -> QUIT
            "withdraw" -> WITHDRAW_MONEY
            "deposit" -> DEPOSIT_MONEY
            "balance", "check" -> CHECK_BANK_ACCOUNT_BALANCE
            "assess" -> ASSESS
            "lie" -> LIE_DOWN
            else -> NONE
        }
    }
}