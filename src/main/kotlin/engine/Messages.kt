package engine

import connection.ConnectionManager
import io.ktor.websocket.*
import kotlinx.coroutines.runBlocking

enum class Message {
    PLAYER_NOT_ENOUGH_GOLD_TO_DEPOSIT,
    LOOK_CURRENT_ROOM,
    ITEM_IS_CLOSED,
    PLAYER_ALREADY_STANDING,
    PLAYER_STANDS_UP,
    ENTITY_STANDS_UP,
    UNHANDLED_PLAYER_INPUT,
    ITEM_ALREADY_CLOSED,
    PLAYER_CLOSES_ITEM,
    PLAYER_OPENS_ITEM,
    ITEM_ALREADY_OPEN,
    PLAYER_REMOVES_ITEM,
    PLAYER_NO_ARMOR_EQUIPPED,
    PLAYER_SHOW_EQUIPPED_ITEM,
    PLAYER_NO_WEAPON_EQUIPPED,
    PLAYER_ITEM_ALREADY_EQUIPPED,
    PLAYER_PICKS_UP_AND_EQUIPS_ITEM,
    PLAYER_EQUIPS_ITEM_FROM_INVENTORY,
    PLAYER_ATTACKS_ENTITY_WITH_WEAPON,
    PLAYER_HITS_FOR_DAMAGE,
    PLAYER_MISSES,
    PLAYER_ALREADY_SITTING,
    PLAYER_SITS,
    ENTITY_SITS,
    PLAYER_ALREADY_KNEELING,
    PLAYER_KNEELS,
    ENTITY_KNEELS,
    PLAYER_PUTS_ITEM_IN_CONTAINER,
    PLAYER_DROPS_ITEM,
    PLAYER_GETS_ITEM,
    PLAYER_GETS_ITEM_FROM_CONTAINER,
    GET_WHAT,
    PLAYER_EATS_FOOD_FINAL,
    PLAYER_EATS_FOOD,
    PLAYER_DRINKS,
    PLAYER_DRINKS_FINAL,
    MONSTER_DIES,
    PLAYER_GAINS_EXPERIENCE,
    PLAYER_NOT_ENOUGH_GOLD_TO_BUY_ITEM,
    PLAYER_NOT_ENOUGH_GOLD_TO_WITHDRAW,
    PLAYER_BUYS_ITEM,
    PLAYER_CAN_SELL_ITEM_HERE,
    PLAYER_ROOM_IS_NOT_SHOP,
    PLAYER_ROOM_IS_NOT_BANK,
    PLAYER_DEPOSITS_GOLD,
    PLAYER_WITHDRAWS_GOLD,
    PLAYER_SEARCHES_DEAD_ENTITY,
    PLAYER_FINDS_GOLD_ON_DEAD_ENTITY,
    PLAYER_SELLS_ITEM_TO_MERCHANT,
    PLAYER_HAS_ARRIVED,
    PLAYER_LEAVES_GAME,
    PLAYER_LEAVES_ROOM,
    PLAYER_DIES,
    OTHER_PLAYER_DIES,
    OTHER_PLAYER_MISSES,
    OTHER_PLAYER_HITS_FOR_DAMAGE,
    ENTITY_SPEAKS_WITH_ENTITY,
    ENTITY_HITS_ENTITY_FOR_DAMAGE,
    ENTITY_MISSES_ENTITY,
    ENTITY_SEARCHES_DEAD_ENTITY,
    ENTITY_SAYS_TO_ENTITY,
    ENTITY_SAYS,
    DEAD_ENTITY_QUIPS_SOLO,
    ENTITY_ATTACKS_PLAYER,
    DEAD_ENTITY_DECAYS,
    ENTITY_MUMBLES,
    ENTITY_PUTS_AWAY_ITEM,
    ENTITY_EQUIPS_ITEM,
    ENTITY_REMOVES_ITEM,
    ENTITY_HEADS_OVER_TO_THE_CONNECTION,
    ENTITY_HEADS_THROUGH_THE_TOWN_GATES,
    ENTITY_HEADS_DIRECTION,
    PLAYER_LIES_DOWN,
    PLAYER_ALREADY_LYING_DOWN,
    ENTITY_DROPS_ITEM,
    PLAYER_CURRENT_GOLD,
    PLAYER_BANK_ACCOUNT_BALANCE,
    PLAYER_NOT_CARRYING_ANYTHING,
    ENTITY_EATS_FOOD_ON_GROUND,
    ENTITY_EATS_FOOD_FROM_INVENTORY,
    ENTITY_DRINKS_DRINK_ON_GROUND,
    ENTITY_DRINKS_DRINK_FROM_INVENTORY,
    FOOD_OR_DRINK_LAST_OF_IT
}

object Messages {
    fun get(message: Message, vararg tokens: String): String {
        var s = when (message) {
            Message.ITEM_ALREADY_CLOSED -> "The %1 is already closed."
            Message.ITEM_IS_CLOSED -> "The %1 is closed."
            Message.LOOK_CURRENT_ROOM -> "[%1 - %2]\n%3"
            Message.PLAYER_NOT_ENOUGH_GOLD_TO_DEPOSIT -> "You don't have that much gold to deposit."
            Message.PLAYER_ALREADY_STANDING -> "You're already standing."
            Message.PLAYER_STANDS_UP -> "You stand up."
            Message.UNHANDLED_PLAYER_INPUT -> "I don't know, boss. Try something else."
            Message.PLAYER_CLOSES_ITEM -> "You close the %1."
            Message.PLAYER_OPENS_ITEM -> "You open the %1."
            Message.ITEM_ALREADY_OPEN -> "The %1 is already open."
            Message.PLAYER_REMOVES_ITEM -> "You remove the %1."
            Message.PLAYER_NO_WEAPON_EQUIPPED -> "You don't have a weapon equipped."
            Message.PLAYER_NO_ARMOR_EQUIPPED -> "You don't have any armor equipped."
            Message.PLAYER_SHOW_EQUIPPED_ITEM -> "You have %1 equipped."
            Message.PLAYER_ITEM_ALREADY_EQUIPPED -> "You already have %1 equipped."
            Message.PLAYER_PICKS_UP_AND_EQUIPS_ITEM -> "You pick up and equip the %1."
            Message.PLAYER_EQUIPS_ITEM_FROM_INVENTORY -> "You equip the %1 from your inventory."
            Message.PLAYER_ATTACKS_ENTITY_WITH_WEAPON -> "You swing at the %1 with your %2."
            Message.PLAYER_HITS_FOR_DAMAGE -> "You hit for %1 damage."
            Message.PLAYER_MISSES -> "You miss!"
            Message.PLAYER_ALREADY_SITTING -> "You're already sitting."
            Message.PLAYER_SITS -> "You sit down."
            Message.PLAYER_ALREADY_KNEELING -> "You're already kneeling."
            Message.PLAYER_KNEELS -> "You kneel."
            Message.PLAYER_PUTS_ITEM_IN_CONTAINER -> "You put the %1 into the %2."
            Message.PLAYER_DROPS_ITEM -> "You drop %1."
            Message.PLAYER_GETS_ITEM -> "You pick up %1."
            Message.PLAYER_GETS_ITEM_FROM_CONTAINER -> "You take the %1 from the %2."
            Message.GET_WHAT -> "Get what?"
            Message.PLAYER_EATS_FOOD_FINAL -> "You take a bite of your %1. That was the last of it."
            Message.PLAYER_EATS_FOOD -> "You take a bite of your %1. %2"
            Message.PLAYER_DRINKS -> "You take a sip of your %1. %2"
            Message.PLAYER_DRINKS_FINAL -> "You take a sip of your %1. That was the last of it."
            Message.MONSTER_DIES -> "The %1 dies."
            Message.PLAYER_GAINS_EXPERIENCE -> "You've gained %1 experience."
            Message.PLAYER_NOT_ENOUGH_GOLD_TO_BUY_ITEM -> "You don't have enough gold (%1) to buy the %2 (%3)."
            Message.PLAYER_BUYS_ITEM -> "You purchase %1 from the merchant for %2 gold."
            Message.PLAYER_CAN_SELL_ITEM_HERE -> "You can sell the %1 here for %2 gold."
            Message.PLAYER_ROOM_IS_NOT_BANK -> "You don't see a bank teller anywhere around here."
            Message.PLAYER_ROOM_IS_NOT_SHOP -> "You don't see a merchant anywhere around here."
            Message.PLAYER_DEPOSITS_GOLD -> "You deposit %1 gold."
            Message.PLAYER_NOT_ENOUGH_GOLD_TO_WITHDRAW -> "You don't have that much gold (%1) in your account to withdraw."
            Message.PLAYER_WITHDRAWS_GOLD -> "You withdraw %1 gold."
            Message.PLAYER_SEARCHES_DEAD_ENTITY -> "You search the %1."
            Message.PLAYER_FINDS_GOLD_ON_DEAD_ENTITY -> "You find %1 gold on the %2."
            Message.PLAYER_SELLS_ITEM_TO_MERCHANT -> "You sell your %1 to the merchant and receive %2 gold."
            Message.PLAYER_HAS_ARRIVED -> "%1 has arrived."
            Message.PLAYER_LEAVES_GAME -> "%1 has left."
            Message.PLAYER_LEAVES_ROOM -> "%1 heads %2."
            Message.PLAYER_DIES -> "You have died."
            Message.OTHER_PLAYER_DIES -> "%1 has died."
            Message.OTHER_PLAYER_MISSES -> "%1 misses!"
            Message.OTHER_PLAYER_HITS_FOR_DAMAGE -> "They hit for %1 damage."
            Message.ENTITY_SPEAKS_WITH_ENTITY -> "%1 exchanges a few words with %2."
            Message.ENTITY_HITS_ENTITY_FOR_DAMAGE -> "They hit for %1 damage."
            Message.ENTITY_MISSES_ENTITY -> "They miss!"
            Message.ENTITY_SEARCHES_DEAD_ENTITY -> "%1 searches the corpse of %2."
            Message.DEAD_ENTITY_QUIPS_SOLO -> "The ghostly voice of %1 says, \"%2\""
            Message.ENTITY_SAYS_TO_ENTITY -> "%1 says to %2, \"%3\""
            Message.ENTITY_SAYS -> "%1 says, \"%2\""
            Message.ENTITY_ATTACKS_PLAYER -> "%1 swings at you with their %2."
            Message.DEAD_ENTITY_DECAYS -> "The body of %1 crumbles to dust."
            Message.ENTITY_MUMBLES -> "%1 mumbles something to themselves."
            Message.ENTITY_PUTS_AWAY_ITEM -> "%1 puts away their %2."
            Message.ENTITY_EQUIPS_ITEM -> "%1 equips %2."
            Message.ENTITY_REMOVES_ITEM -> "%1 removes their %2."
            Message.ENTITY_HEADS_OVER_TO_THE_CONNECTION -> "%1 heads over to the %2."
            Message.ENTITY_HEADS_THROUGH_THE_TOWN_GATES -> "%1 heads through the town gates."
            Message.ENTITY_HEADS_DIRECTION -> "%1 heads %2."
            Message.ENTITY_KNEELS -> "%1 kneels."
            Message.ENTITY_SITS -> "%1 sits down."
            Message.ENTITY_STANDS_UP -> "%1 stands up."
            Message.PLAYER_ALREADY_LYING_DOWN -> "You're already lying down."
            Message.PLAYER_LIES_DOWN -> "You lie down."
            Message.ENTITY_DROPS_ITEM -> "%1 drops %2."
            Message.PLAYER_CURRENT_GOLD ->  "You have %1 gold."
            Message.PLAYER_BANK_ACCOUNT_BALANCE ->  "Your balance is %1 gold."
            Message.PLAYER_NOT_CARRYING_ANYTHING -> "You aren't carrying anything."
            Message.ENTITY_EATS_FOOD_ON_GROUND -> "%1 takes a bite of the %2, which is on the ground."
            Message.ENTITY_DRINKS_DRINK_ON_GROUND -> "%1 takes a drink from the %2, which is on the ground."
            Message.ENTITY_DRINKS_DRINK_FROM_INVENTORY -> ""
            Message.ENTITY_EATS_FOOD_FROM_INVENTORY -> "%1 takes a bite of their %2."
            Message.FOOD_OR_DRINK_LAST_OF_IT -> "That was the last of it."

            else -> "error: invalid string"
        }

        for (i in 1..tokens.size) {
            s = s.replace("%$i", tokens[i - 1])
        }

        return s

//        return when (tokens.size) {
//            1 -> s.replace("%1", tokens[0])
//            2 -> s.replace("%1", tokens[0]).replace("%2", tokens[1])
//            3 -> s.replace("%1", tokens[0]).replace("%2", tokens[1]).replace("%3", tokens[2])
//            else -> s
//        }
    }

//    fun println(str: String) {
//        runBlocking {
//            if (ConnectionManager.webSocketSessions.size > 0) {
//                ConnectionManager.webSocketSessions[0].send(str)
//            }
//        }
//
//        kotlin.io.println(str)
//    }

    // fun println(message: Message, vararg tokens: String) = println(get(message, *tokens))

    fun biteString(bites: Int) = if (bites > 1) {
        "You have $bites bites left."
    } else {
        "You have one bite left."
    }

    fun quaffString(quaffs: Int) = if (quaffs > 1) {
        "You have $quaffs quaffs left."
    } else {
        "You have one quaff left."
    }
}
