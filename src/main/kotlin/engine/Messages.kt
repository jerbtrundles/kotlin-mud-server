package engine

import javax.print.attribute.standard.MediaSize.Other

enum class Message {
    DEAD_ENTITY_DECAYS,
    DEAD_ENTITY_QUIPS_SOLO,
    ENTITY_ARRIVES,
    ENTITY_ATTACKS_PLAYER,
    ENTITY_DESTROYS_ITEM,
    ENTITY_DIES,
    ENTITY_DRINKS_DRINK_FROM_INVENTORY,
    ENTITY_DRINKS_DRINK_ON_GROUND,
    ENTITY_DROPS_ITEM,
    ENTITY_EATS_FOOD_FROM_INVENTORY,
    ENTITY_EATS_FOOD_ON_GROUND,
    ENTITY_EQUIPS_ITEM,
    ENTITY_HEADS_DIRECTION,
    ENTITY_HEADS_OVER_TO_THE_CONNECTION,
    ENTITY_HEADS_THROUGH_THE_TOWN_GATES,
    ENTITY_HITS_ENTITY_FOR_DAMAGE,
    ENTITY_KNEELS,
    ENTITY_MISSES_ENTITY,
    ENTITY_MUMBLES,
    ENTITY_PICKS_UP_ITEM,
    ENTITY_PUTS_AWAY_ITEM,
    ENTITY_REMOVES_ITEM,
    ENTITY_SAYS,
    ENTITY_SAYS_TO_ENTITY,
    ENTITY_SEARCHES_DEAD_ENTITY,
    ENTITY_SITS,
    ENTITY_SPEAKS_WITH_ENTITY,
    ENTITY_STANDS_UP,
    FOOD_OR_DRINK_LAST_OF_IT,
    GET_WHAT,
    ITEM_ALREADY_CLOSED,
    ITEM_ALREADY_OPEN,
    ITEM_IS_CLOSED,
    LOOK_CURRENT_ROOM,
    OTHER_ENTITY_DOES_BANKING,
    OTHER_ENTITY_DOES_COMMERCE,
    OTHER_ENTITY_FINDS_GOLD,
    OTHER_PLAYER_CLOSES_ITEM,
    OTHER_PLAYER_DIES,
    OTHER_PLAYER_DRINKS,
    OTHER_PLAYER_DRINKS_FINAL,
    OTHER_PLAYER_DROPS_ITEM,
    OTHER_PLAYER_EATS_FOOD,
    OTHER_PLAYER_EATS_FOOD_FINAL,
    OTHER_PLAYER_EQUIPS_ITEM_FROM_INVENTORY,
    OTHER_PLAYER_GETS_ITEM,
    OTHER_PLAYER_GETS_ITEM_FROM_CONTAINER,
    OTHER_PLAYER_HITS_FOR_DAMAGE,
    OTHER_PLAYER_KNEELS,
    OTHER_PLAYER_LIES_DOWN,
    OTHER_PLAYER_MISSES,
    OTHER_PLAYER_OPENS_ITEM,
    OTHER_PLAYER_PICKS_UP_AND_EQUIPS_ITEM,
    OTHER_PLAYER_PUTS_ITEM_IN_CONTAINER,
    OTHER_PLAYER_SEARCHES_DEAD_ENTITY,
    OTHER_PLAYER_SITS,
    OTHER_PLAYER_STANDS_UP,
    PLAYER_ALREADY_KNEELING,
    PLAYER_ALREADY_LYING_DOWN,
    PLAYER_ALREADY_SITTING,
    PLAYER_ALREADY_STANDING,
    PLAYER_ATTACKS_ENTITY_WITH_WEAPON,
    PLAYER_BANK_ACCOUNT_BALANCE,
    PLAYER_BUYS_ITEM,
    PLAYER_CAN_SELL_ITEM_HERE,
    PLAYER_CLOSES_ITEM,
    PLAYER_CURRENT_GOLD,
    PLAYER_DEPOSITS_GOLD,
    PLAYER_DIES,
    PLAYER_DRINKS,
    PLAYER_DRINKS_FINAL,
    PLAYER_DROPS_ITEM,
    PLAYER_EATS_FOOD,
    PLAYER_EATS_FOOD_FINAL,
    PLAYER_EQUIPS_ITEM_FROM_INVENTORY,
    PLAYER_FINDS_GOLD_ON_DEAD_ENTITY,
    PLAYER_GAINS_EXPERIENCE,
    PLAYER_GETS_ITEM,
    PLAYER_GETS_ITEM_FROM_CONTAINER,
    PLAYER_HAS_ARRIVED,
    PLAYER_HITS_FOR_DAMAGE,
    PLAYER_ITEM_ALREADY_EQUIPPED,
    PLAYER_KNEELS,
    PLAYER_LEAVES_GAME,
    PLAYER_LEAVES_ROOM,
    PLAYER_LIES_DOWN,
    PLAYER_MISSES,
    PLAYER_NOT_CARRYING_ANYTHING,
    PLAYER_NOT_ENOUGH_GOLD_TO_BUY_ITEM,
    PLAYER_NOT_ENOUGH_GOLD_TO_DEPOSIT,
    PLAYER_NOT_ENOUGH_GOLD_TO_WITHDRAW,
    PLAYER_NO_ARMOR_EQUIPPED,
    PLAYER_NO_WEAPON_EQUIPPED,
    PLAYER_OPENS_ITEM,
    PLAYER_PICKS_UP_AND_EQUIPS_ITEM,
    PLAYER_PUTS_ITEM_IN_CONTAINER,
    PLAYER_REMOVES_ITEM,
    PLAYER_ROOM_IS_NOT_BANK,
    PLAYER_ROOM_IS_NOT_SHOP,
    PLAYER_SEARCHES_DEAD_ENTITY,
    PLAYER_SELLS_ITEM_TO_MERCHANT,
    PLAYER_SHOW_EQUIPPED_ITEM,
    PLAYER_SITS,
    PLAYER_STANDS_UP,
    PLAYER_WITHDRAWS_GOLD,
    UNHANDLED_PLAYER_INPUT,
}

object Messages {
    fun get(message: Message) =
        when (message) {
            Message.DEAD_ENTITY_DECAYS -> "The body of %1 crumbles to dust."
            Message.DEAD_ENTITY_QUIPS_SOLO -> "The ghostly voice of %1 says, \"%2\""
            Message.ENTITY_ARRIVES -> "%1 %2."
            Message.ENTITY_ATTACKS_PLAYER -> "%1 swings at you with their %2."
            Message.ENTITY_DESTROYS_ITEM -> "%1 sweeps %2 into their dustpan."
            Message.ENTITY_DIES -> "%1 dies."
            Message.ENTITY_DRINKS_DRINK_FROM_INVENTORY -> "%1 takes a drink from their %2."
            Message.ENTITY_DRINKS_DRINK_ON_GROUND -> "%1 takes a drink from the %2, which is on the ground."
            Message.ENTITY_DROPS_ITEM -> "%1 drops %2."
            Message.ENTITY_EATS_FOOD_FROM_INVENTORY -> "%1 takes a bite of their %2."
            Message.ENTITY_EATS_FOOD_ON_GROUND -> "%1 takes a bite of the %2, which is on the ground."
            Message.ENTITY_EQUIPS_ITEM -> "%1 equips %2."
            Message.ENTITY_HEADS_DIRECTION -> "%1 heads %2."
            Message.ENTITY_HEADS_OVER_TO_THE_CONNECTION -> "%1 heads over to the %2."
            Message.ENTITY_HEADS_THROUGH_THE_TOWN_GATES -> "%1 heads through the town gates."
            Message.ENTITY_HITS_ENTITY_FOR_DAMAGE -> "They hit for %1 damage."
            Message.ENTITY_KNEELS -> "%1 kneels."
            Message.ENTITY_MISSES_ENTITY -> "They miss!"
            Message.ENTITY_MUMBLES -> "%1 mumbles something to themselves."
            Message.ENTITY_PICKS_UP_ITEM -> "%1 picks up %2."
            Message.ENTITY_PUTS_AWAY_ITEM -> "%1 puts away their %2."
            Message.ENTITY_REMOVES_ITEM -> "%1 removes their %2."
            Message.ENTITY_SAYS -> "%1 says, \"%2\""
            Message.ENTITY_SAYS_TO_ENTITY -> "%1 says to %2, \"%3\""
            Message.ENTITY_SEARCHES_DEAD_ENTITY -> "%1 searches the corpse of %2."
            Message.ENTITY_SITS -> "%1 sits down."
            Message.ENTITY_SPEAKS_WITH_ENTITY -> "%1 exchanges a few words with %2."
            Message.ENTITY_STANDS_UP -> "%1 stands up."
            Message.FOOD_OR_DRINK_LAST_OF_IT -> "That was the last of it."
            Message.GET_WHAT -> "Get what?"
            Message.ITEM_ALREADY_CLOSED -> "The %1 is already closed."
            Message.ITEM_ALREADY_OPEN -> "The %1 is already open."
            Message.ITEM_IS_CLOSED -> "The %1 is closed."
            Message.LOOK_CURRENT_ROOM -> "[%1 - %2]\n%3"
            Message.OTHER_ENTITY_DOES_BANKING -> "%1 chats briefly with the bank teller."
            Message.OTHER_ENTITY_DOES_COMMERCE -> "%1 chats briefly with the merchant."
            Message.OTHER_ENTITY_FINDS_GOLD -> "%1 finds some gold."
            Message.OTHER_PLAYER_CLOSES_ITEM -> "%1 closes %2."
            Message.OTHER_PLAYER_DIES -> "%1 has died."
            Message.OTHER_PLAYER_DROPS_ITEM -> "%1 drops %2."
            Message.OTHER_PLAYER_EATS_FOOD -> "%1 takes a bite of their %2."
            Message.OTHER_PLAYER_EATS_FOOD_FINAL -> "%1 takes a bite of their %2. That was the last of it."
            Message.OTHER_PLAYER_EQUIPS_ITEM_FROM_INVENTORY -> "%1 equips %2."
            Message.OTHER_PLAYER_GETS_ITEM -> "%1 picks up %2."
            Message.OTHER_PLAYER_GETS_ITEM_FROM_CONTAINER -> "%1 takes %1 from %2."
            Message.OTHER_PLAYER_HITS_FOR_DAMAGE -> "They hit for %1 damage."
            Message.OTHER_PLAYER_KNEELS -> "%1 kneels."
            Message.OTHER_PLAYER_LIES_DOWN -> "%1 lies down."
            Message.OTHER_PLAYER_MISSES -> "%1 misses!"
            Message.OTHER_PLAYER_OPENS_ITEM -> "%1 opens %2."
            Message.OTHER_PLAYER_PICKS_UP_AND_EQUIPS_ITEM -> "%1 picks up and equips the %2."
            Message.OTHER_PLAYER_PUTS_ITEM_IN_CONTAINER -> "%1 puts %2 into %3."
            Message.OTHER_PLAYER_SEARCHES_DEAD_ENTITY -> "%1 searches the %2."
            Message.OTHER_PLAYER_SITS -> "%1 sits down."
            Message.OTHER_PLAYER_STANDS_UP -> "%1 stands up."
            Message.PLAYER_ALREADY_KNEELING -> "You're already kneeling."
            Message.PLAYER_ALREADY_LYING_DOWN -> "You're already lying down."
            Message.PLAYER_ALREADY_SITTING -> "You're already sitting."
            Message.PLAYER_ALREADY_STANDING -> "You're already standing."
            Message.PLAYER_ATTACKS_ENTITY_WITH_WEAPON -> "You swing at the %1 with your %2."
            Message.PLAYER_BANK_ACCOUNT_BALANCE -> "Your balance is %1 gold."
            Message.PLAYER_BUYS_ITEM -> "You purchase %1 from the merchant for %2 gold."
            Message.PLAYER_CAN_SELL_ITEM_HERE -> "You can sell the %1 here for %2 gold."
            Message.PLAYER_CLOSES_ITEM -> "You close %1."
            Message.PLAYER_CURRENT_GOLD -> "You have %1 gold."
            Message.PLAYER_DEPOSITS_GOLD -> "You deposit %1 gold."
            Message.PLAYER_DIES -> "You have died."
            Message.PLAYER_DRINKS -> "You take a sip of your %1. %2"
            Message.PLAYER_DRINKS_FINAL -> "You take a sip of your %1. That was the last of it."
            Message.PLAYER_DROPS_ITEM -> "You drop %1."
            Message.PLAYER_EATS_FOOD -> "You take a bite of your %1. %2"
            Message.PLAYER_EATS_FOOD_FINAL -> "You take a bite of your %1. That was the last of it."
            Message.PLAYER_EQUIPS_ITEM_FROM_INVENTORY -> "You equip %1 from your inventory."
            Message.PLAYER_FINDS_GOLD_ON_DEAD_ENTITY -> "You find %1 gold on the %2."
            Message.PLAYER_GAINS_EXPERIENCE -> "You've gained %1 experience."
            Message.PLAYER_GETS_ITEM -> "You pick up %1."
            Message.PLAYER_GETS_ITEM_FROM_CONTAINER -> "You take %1 from %2."
            Message.PLAYER_HAS_ARRIVED -> "%1 has arrived."
            Message.PLAYER_HITS_FOR_DAMAGE -> "You hit for %1 damage."
            Message.PLAYER_ITEM_ALREADY_EQUIPPED -> "You already have %1 equipped."
            Message.PLAYER_KNEELS -> "You kneel."
            Message.PLAYER_LEAVES_GAME -> "%1 has left."
            Message.PLAYER_LEAVES_ROOM -> "%1 heads %2."
            Message.PLAYER_LIES_DOWN -> "You lie down."
            Message.PLAYER_MISSES -> "You miss!"
            Message.PLAYER_NOT_CARRYING_ANYTHING -> "You aren't carrying anything."
            Message.PLAYER_NOT_ENOUGH_GOLD_TO_BUY_ITEM -> "You don't have enough gold (%1) to buy the %2 (%3)."
            Message.PLAYER_NOT_ENOUGH_GOLD_TO_DEPOSIT -> "You don't have that much gold to deposit."
            Message.PLAYER_NOT_ENOUGH_GOLD_TO_WITHDRAW -> "You don't have that much gold (%1) in your account to withdraw."
            Message.PLAYER_NO_ARMOR_EQUIPPED -> "You don't have any armor equipped."
            Message.PLAYER_NO_WEAPON_EQUIPPED -> "You don't have a weapon equipped."
            Message.PLAYER_OPENS_ITEM -> "You open %1."
            Message.PLAYER_PICKS_UP_AND_EQUIPS_ITEM -> "You pick up and equip the %1."
            Message.PLAYER_PUTS_ITEM_IN_CONTAINER -> "You put %1 into %2."
            Message.PLAYER_REMOVES_ITEM -> "You remove the %1."
            Message.PLAYER_ROOM_IS_NOT_BANK -> "You don't see a bank teller anywhere around here."
            Message.PLAYER_ROOM_IS_NOT_SHOP -> "You don't see a merchant anywhere around here."
            Message.PLAYER_SEARCHES_DEAD_ENTITY -> "You search the %1."
            Message.PLAYER_SELLS_ITEM_TO_MERCHANT -> "You sell your %1 to the merchant and receive %2 gold."
            Message.PLAYER_SHOW_EQUIPPED_ITEM -> "You have %1 equipped."
            Message.PLAYER_SITS -> "You sit down."
            Message.PLAYER_STANDS_UP -> "You stand up."
            Message.PLAYER_WITHDRAWS_GOLD -> "You withdraw %1 gold."
            Message.UNHANDLED_PLAYER_INPUT -> "I don't know, boss. Try something else."
            else -> throw(Exception("error: invalid string"))
        }

    fun get(message: Message, vararg tokens: String) =
        replaceTokens(get(message), *tokens)

    private fun replaceTokens(str: String, vararg tokens: String): String {
        var s = str

        for (i in 1..tokens.size) {
            s = s.replace("%$i", tokens[i - 1])
        }

        return s
    }

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
