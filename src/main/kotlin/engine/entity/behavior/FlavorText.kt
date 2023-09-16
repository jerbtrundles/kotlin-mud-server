package engine.entity.behavior

object FlavorText {
    private object Strings {
        val getItemStrings = arrayOf(
            "Well look at this.",
            "This looks useful.",
            "I'm taking this.",
            "This is mine now.",
            "Let's see how this works."
        )

        val getValuableItemStrings = arrayOf(
            "Whoa! Look at this!",
            "This looks nice!",
        )

        // region quips
        val quipsFriendly = arrayOf(
            "Nice weather today, isn't it?",
            "My feet ache somethin' awful.",
            "I'm a little sick. Don't get too close!",
            "I found a lucky coin on the ground the other day.",
            "Mrrrrrrr...."
        )
        val deadQuipsFriendlyCommon = arrayOf(
            "Welp.",
            "Well this sucks.",
            "Being dead isn't as a bad as I figured it'd be.",
            "Boo!"
        )
        val deadToLivingFriendlyQuips = arrayOf(
            "Must be nice to still be alive.",
            "Others will avenge me!",
            "Hey, there. Could I get some assistance?",
            *deadQuipsFriendlyCommon
        )
        val deadToDeadFriendlyQuips = arrayOf(
            "Well look at us.",
            "They got you too, huh?",
            "We'll be avenged! Just you wait!",
            *deadQuipsFriendlyCommon
        )
        val livingToLivingFriendlyQuips = arrayOf(
            *quipsFriendly,
        )
        val livingToDeadFriendlyQuips = arrayOf(
            "Whoa, you're dead. What happened?",
            "I hope someone comes along to help you soon!"
        )
        val livingToDeadHostileQuips = arrayOf(
            "Ha!",
            "Victory is mine today, fiend!"
        )
        val livingToLivingHostileQuips = arrayOf(
            "Have at you, fiend!",
            "I'm going to make you suffer!",
            "You'll regret crossing me!",
            "You aren't welcome here!",
            "Away from me, you filthy creature!"
        )
        val deadToLivingHostileQuips = arrayOf(
            "You won't live much longer!",
            "You won't get away with this!",
            "I'm going to haunt you for the rest of your days!",
            "I'll have my revenge soon!",
            "I yield! Oh, too late.",
        )
        val deadToDeadHostileQuips = arrayOf(
            "Well look at us.",
            "My people will destroy your kind!",
            "I might be dead, but so are you!"
        )
        // endregion

        val idleActionStrings = arrayOf(
            "capitalizedConversationalName gazes up at the sky.",
            "capitalizedConversationalName shuffles their feet.",
            "capitalizedConversationalName glances around.",
            "capitalizedConversationalName says \"quip\"",
            "capitalizedConversationalName rummages around in their pockets, looking for something."
        )

        fun randomIdleActionString() = idleActionStrings.random()
            .replace("quip", quipsFriendly.random())
    }

    fun get(type: EntityAction) =
        when (type) {
            EntityAction.GET_ANY_ITEM -> Strings.getItemStrings.random()
            EntityAction.GET_VALUABLE_ITEM -> Strings.getValuableItemStrings.random()
            EntityAction.SPEAK_WITH_RANDOM_LIVING_ENTITY -> Strings.quipsFriendly.random()
            EntityAction.LIVING_ENTITY_SAYS_TO_LIVING_FRIENDLY_ENTITY -> Strings.livingToLivingFriendlyQuips.random()
            EntityAction.LIVING_ENTITY_SAYS_TO_DEAD_FRIENDLY_ENTITY -> Strings.livingToDeadFriendlyQuips.random()
            EntityAction.DEAD_ENTITY_SAYS_TO_LIVING_FRIENDLY_ENTITY -> Strings.deadToLivingFriendlyQuips.random()
            EntityAction.DEAD_ENTITY_SAYS_TO_DEAD_FRIENDLY_ENTITY -> Strings.deadToDeadFriendlyQuips.random()
            EntityAction.LIVING_ENTITY_SAYS_TO_LIVING_HOSTILE_ENTITY -> Strings.livingToLivingHostileQuips.random()
            EntityAction.LIVING_ENTITY_SAYS_TO_DEAD_HOSTILE_ENTITY -> Strings.livingToDeadHostileQuips.random()
            EntityAction.DEAD_ENTITY_SAYS_TO_LIVING_HOSTILE_ENTITY -> Strings.deadToLivingHostileQuips.random()
            EntityAction.DEAD_ENTITY_SAYS_TO_DEAD_HOSTILE_ENTITY -> Strings.deadToDeadHostileQuips.random()
            EntityAction.IDLE_FLAVOR_ACTION -> Strings.randomIdleActionString()
            EntityAction.LIVING_QUIP_SOLO -> Strings.quipsFriendly.random()
            EntityAction.DEAD_QUIP_SOLO -> Strings.deadQuipsFriendlyCommon.random()
            else -> ""
        }
}
