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
    ENTITY_FIND_ARMOR,
    ECHO_MESSAGE;

    companion object {
        fun enabled(messageType: DebugMessageType) =
            when (messageType) {
                DEFAULT -> true
                ENTITY_PASSIVE_EFFECT -> false
                ENTITY_CHECK_FOR_MAGIC_EFFECT -> false
                ENTITY_CAST_SPELL -> false
                ENTITY_ADD_TO_ROOM -> false
                ENTITY_ATTACK -> false
                ENTITY_SEARCH -> false
                ENTITY_FIND_WEAPON -> true
                ENTITY_FIND_ARMOR -> true
                ENTITY_GET_VALUABLE_ITEM -> false
                ECHO_MESSAGE -> false
                else -> false
            }
    }
}
