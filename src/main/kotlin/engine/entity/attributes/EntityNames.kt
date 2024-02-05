package engine.entity.attributes

import engine.utility.withIndefiniteArticle

class EntityNames(
    val primary: String,
    val withJob: String,
    val full: String,
    val story: String,
    // "$arrive has arrived."
    val arrive: String,
    // "The body of $finalCleanup crumbles to dust."
    val finalCleanup: String,
    // "$prefix$death dies."
    val death: String,
    // "$fullName (dead)"
    val dead: String,
    // "$fullName (sitting)"
    val prefix: String,
    // "... [walks/struts/ambles/lumbers/etc. in]"
    val arriveSuffix: String,
    // "... asks/looks at/says to/etc. [the goblin / Bert the tailor]"
    val conversational: String,
    // "the spirit of $conversational
    val deadConversational: String,
    val capitalizedPrefixedDeath: String,
) {
    fun random() = arrayOf(primary, withJob, full).random()
    fun prefixedRandom() = "$prefix ${random()}"
    fun capitalizedPrefixedRandom() = prefixedRandom().replaceFirstChar { it.uppercase() }

    val prefixedFull = "$prefix$full"
    val capitalizedPrefixedFull = prefixedFull.replaceFirstChar { it.uppercase() }

    val capitalizedConversational = conversational.replaceFirstChar { it.uppercase() }
    //  "$fullName (kneeling)"
    val kneeling = "$full (kneeling)"
    val sitting = "$full (sitting)"
    val lyingDown = "$full (lying down)"


    companion object {
        fun monster(name: String, arriveSuffix: String) =
            EntityNames(
                primary = name,
                withJob = name,
                full = name,
                story = "The $name",
                arrive = name.withIndefiniteArticle(capitalized = true),
                finalCleanup = "",
                death = name,
                dead = "$name (dead)",
                prefix = "The ",
                arriveSuffix = arriveSuffix,
                conversational = "the $name",
                deadConversational = "the spirit of ",
                capitalizedPrefixedDeath = "The $name"
            )

        fun friendlyNpc(name: String, entityClass: String, arriveSuffix: String) =
            EntityNames(
                primary = name,
                withJob = "$name the $entityClass",
                full = "$name the $entityClass",
                story = "$name the $entityClass",
                arrive = "$name the $entityClass",
                finalCleanup = "$name the $entityClass",
                death = "$name the $entityClass",
                dead = "$name the $entityClass (dead)",
                prefix = "",
                arriveSuffix = arriveSuffix,
                conversational = name,
                deadConversational = "the spirit of $name the $entityClass",
                capitalizedPrefixedDeath = "$name the $entityClass"
            )
    }
}


//}