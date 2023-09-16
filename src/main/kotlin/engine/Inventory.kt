package engine

import debug.Debug
import engine.item.ItemArmor
import engine.item.ItemBase
import engine.item.ItemWeapon
import engine.utility.Common
import java.util.*

class Inventory(
    val items: MutableList<ItemBase> = Collections.synchronizedList(mutableListOf())
) {
    fun getItemByKeyword(keyword: String) = items.firstOrNull { item ->
        item.keywords.contains(keyword)
    }

    inline fun <reified T> getTypedItemByKeyword(keyword: String) =
        items.firstOrNull { item ->
            item is T && item.keywords.contains(keyword)
        } as? T

    override fun toString() = collectionString
    val collectionString
        get() = Common.collectionString(items.map { item -> item.name })

    fun getAndRemoveRandomItem(): ItemBase? =
        items.randomOrNull()?.let { item ->
            items.remove(item)
            item
        }

    fun getAndRemoveRandomValuableItem() =
        items.filter { it.value > Debug.valuableItemMinimumValue }.randomOrNull()?.let {
            items.remove(it)
            it
        }

    inline fun <reified T> getRandomTypedItemOrNull() = items.filterIsInstance<T>().randomOrNull()
    inline fun <reified T> getAndRemoveRandomTypedItemOrNull(): T? =
        getRandomTypedItemOrNull<T>()?.let {
            items.remove(it as ItemBase)
            it
        }

    fun getAndRemoveRandomWeaponOrNull() = getAndRemoveRandomTypedItemOrNull<ItemWeapon>()
    fun getAndRemoveRandomArmorOrNull() = getAndRemoveRandomTypedItemOrNull<ItemArmor>()
    fun getAndRemoveRandomBetterWeaponOrNull(minRequiredPower: Int) =
        items.filterIsInstance(ItemWeapon::class.java)
            .filter { it.power >= minRequiredPower }
            .randomOrNull()?.let {
                items.remove(it)
                it
            }

    fun getAndRemoveRandomBetterArmorOrNull(minRequiredDefense: Int) =
        items.filterIsInstance(ItemArmor::class.java)
            .filter { it.defense >= minRequiredDefense }
            .randomOrNull()?.let {
                items.remove(it)
                it
            }

    fun getAndRemoveBestWeaponOrNull(minPower: Int = 0) =
       getBestWeaponOrNull(minPower)?.let {
           items.remove(it)
           it
       }

    fun getBestWeaponOrNull(minPower: Int = 0) = items.asSequence()
        .filterIsInstance<ItemWeapon>()
        .maxByOrNull { if (it.power > minPower) it.power else Int.MIN_VALUE }

    fun getBestArmorOrNull(minDefense: Int = 0) = items.asSequence()
        .filterIsInstance<ItemArmor>()
        .maxByOrNull { if (it.defense > minDefense) it.defense else Int.MIN_VALUE }

    fun getAndRemoveBestArmorOrNull(minDefense: Int = 0): ItemArmor? {
        val bestArmor = items.asSequence()
            .filterIsInstance<ItemArmor>()
            .maxByOrNull { if (it.defense > minDefense) it.defense else Int.MIN_VALUE }

        bestArmor?.let { items.remove(it) }

        return bestArmor
    }
}