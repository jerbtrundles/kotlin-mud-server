package engine.utility

import com.beust.klaxon.Klaxon
import kotlin.random.Random

object Common {
    fun collectionString(
        itemStrings: List<String>,
        includeIndefiniteArticles: Boolean = true
    ): String {
        return when (itemStrings.size) {
            0 -> ""
            // one item: "a pigeon"
            1 -> collectionStringOneItem(itemStrings, includeIndefiniteArticles)
            // two items: "an apple and an orange"
            2 -> collectionStringTwoItems(itemStrings, includeIndefiniteArticles)
            // more than two items; e.g. "a cat, a hat, a banana, and an iguana"
            else -> collectionStringMoreThanTwoItems(itemStrings, includeIndefiniteArticles)
        }
    }

    private fun collectionStringOneItem(itemStrings: List<String>, includeIndefiniteArticles: Boolean) =
        // one item: "a pigeon"
        if (includeIndefiniteArticles) {
            itemStrings.first().withIndefiniteArticle()
        } else {
            itemStrings.first()
        }

    private fun collectionStringTwoItems(itemStrings: List<String>, includeIndefiniteArticles: Boolean) =
        // two items: "an apple and an orange"
        if (includeIndefiniteArticles) {
            itemStrings.joinToString(" and ") { itemString -> itemString.withIndefiniteArticle() }
        } else {
            itemStrings.joinToString(" and ")
        }

    private fun collectionStringMoreThanTwoItems(
        itemStrings: List<String>,
        includeIndefiniteArticles: Boolean
    ): String {
        // more than two items; e.g. "a cat, a hat, a banana, and an iguana"
        // start with simple joined string: "a cat, a bagel, an apple, an orange, a hat"
        val mergedItemsString = if (includeIndefiniteArticles) {
            itemStrings.joinToString(", ") { itemString -> itemString.withIndefiniteArticle() }
        } else {
            itemStrings.joinToString(", ")
        }

        // insert "and " before last item
        return mergedItemsString.substringBeforeLast(", ") + ", and " + mergedItemsString.substringAfterLast(
            ", "
        )
    }

    inline fun <reified T> parseArrayFromJson(c: Class<() -> Unit>, fileName: String): List<T> =
        Klaxon().parseArray(loadJson(c, fileName))!!

    inline fun <reified T> parseFromJson(c: Class<() -> Unit>, fileName: String): T =
        Klaxon().parse(loadJson(c, fileName))!!

    fun loadJson(c: Class<() -> Unit>, fileName: String) =
        c.getResourceAsStream(fileName)?.bufferedReader()?.readText()!!

    fun d100(percentChance: Int, doThis: () -> Unit) {
        if (Random.nextInt(from = 0, until = 100) < percentChance) {
            doThis()
        }
    }

    fun <T> listsHaveSameItems(list1: List<T>, list2: List<T>) =
        list1.size == list2.size
                && list1.toSet() == list2.toSet()
}
