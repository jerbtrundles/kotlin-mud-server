package engine.utility

import engine.Message
import engine.Messages

const val vowels: String = "aeiou"
fun isVowel(c: Char) = vowels.contains(c)

private val indefiniteArticleExceptions = arrayOf(
    "unicorn"
)

fun String.withIndefiniteArticle(capitalized: Boolean = false): String {
    // some words start with vowels but would still be "a <exception>"
    //  e.g. hard y sounds, like "a unicorn"

    return if (isVowel(this[0]) && !indefiniteArticleExceptions.any { this.lowercase().startsWith(it) }) {
        if (capitalized) {
            "An $this"
        } else {
            "an $this"
        }
    } else {
        if (capitalized) {
            "A $this"
        } else {
            "a $this"
        }
    }
}

fun StringBuilder.appendLine(message: Message, vararg tokens: String) =
    appendLine(Messages.get(message, *tokens))