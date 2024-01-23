package debug

enum class DebugMessageType {
    ENTITY_PASSIVE_EFFECT,
    ENTITY_CHECK_FOR_MAGIC_EFFECT,
    ENTITY_CAST_SPELL,
    DEFAULT,
    ENTITY_ADD_TO_ROOM,
    ENTITY_SEARCH,
    ENTITY_ATTACK,
    ENTITY_GET_VALUABLE_ITEM,
    ENTITY_FIND_WEAPON,
    ENTITY_FIND_ARMOR;

    companion object {
        fun enabled(messageType: DebugMessageType) =
            when (messageType) {
                DEFAULT -> true
                ENTITY_PASSIVE_EFFECT -> false
                ENTITY_CHECK_FOR_MAGIC_EFFECT -> false
                ENTITY_CAST_SPELL -> true
                ENTITY_ADD_TO_ROOM -> false
                ENTITY_ATTACK -> true
                ENTITY_SEARCH -> false
                ENTITY_FIND_WEAPON -> false
                ENTITY_FIND_ARMOR -> false
                ENTITY_GET_VALUABLE_ITEM -> false
                else -> false
            }
    }
}