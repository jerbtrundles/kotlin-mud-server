package engine

import debug.Debug
import engine.item.*
import engine.item.template.ItemTemplate
import engine.item.template.ItemTemplates
import engine.utility.Common
import engine.utility.Common.d100
import java.util.*

class Inventory(
    val items: MutableList<ItemBase> = Collections.synchronizedList(mutableListOf())
) {
    companion object {
        fun defaultMonster() = Inventory()
        fun defaultNpc() = Inventory()
        fun createWithRandomStuff(): Inventory {
            with(Inventory()) {
                repeat(5) {
                    randomChanceAddItemToList(ItemTemplates.junk)
                    randomChanceAddItemToList(ItemTemplates.food)
                    randomChanceAddItemToList(ItemTemplates.drinks)
                    randomChanceAddItemToList(ItemTemplates.weapons)
                    randomChanceAddItemToList(ItemTemplates.armor)
                }

                return this
            }
        }
    }

    // region empty checks
    fun isEmpty() = items.isEmpty()
    fun isNotEmpty() = items.isNotEmpty()
    // endregion

    // region strings
    val itemsTextString
        get() = "ITEMS:${items.joinToString(separator = "\n") { it.name }}"

    override fun toString() = collectionString
    val collectionString
        get() = Common.collectionString(items.map { item -> item.name })
    // endregion

    // region add/remove
    fun addInventory(inventory: Inventory) = items.addAll(inventory.items)
    fun addItem(item: ItemBase) = items.add(item)
    private fun randomChanceAddItemToList(templates: List<ItemTemplate>, percentChance: Int = 20) =
        d100(percentChance) {
            templates.randomOrNull()?.let { items.add(it.createItem()) }
        }

    fun removeItem(item: ItemBase) = items.remove(item)
    // endregion

    // region contains item
    val containsValuableItem
        get() = items.any { it.value > Debug.valuableItemMinimumValue }

    fun containsWeapon() =
        items.any { it is ItemWeapon }

    fun containsArmor() =
        items.any { it is ItemArmor }

    fun containsFood() =
        items.any { it is ItemFood }
    fun containsDrink() =
        items.any { it is ItemDrink }
    fun containsContainer() =
        items.any { it is ItemContainer }

    fun containsJunk() =
        items.any { it is ItemJunk }
    // endregion

    // region get best
    fun getBestWeaponOrNull(minPower: Int = 0) = items.asSequence()
        .filterIsInstance<ItemWeapon>()
        .maxByOrNull { if (it.power > minPower) it.power else Int.MIN_VALUE }

    fun getBestArmorOrNull(minDefense: Int = 0) = items.asSequence()
        .filterIsInstance<ItemArmor>()
        .maxByOrNull { if (it.defense > minDefense) it.defense else Int.MIN_VALUE }
    // endregion

    // region get random
    fun getRandomFoodOrNull() =
        getRandomTypedItemOrNull<ItemFood>()

    fun getRandomDrinkOrNull() =
        getRandomTypedItemOrNull<ItemDrink>()
    // endregion

    // region get with keyword
    fun getContainerWithKeywordOrNull(keyword: String) =
        getTypedItemWithKeywordOrNull<ItemContainer>(keyword)

    fun getFoodWithKeywordOrNull(keyword: String) =
        getTypedItemWithKeywordOrNull<ItemFood>(keyword)

    fun getDrinkWithKeywordOrNull(keyword: String) =
        getTypedItemWithKeywordOrNull<ItemDrink>(keyword)

    fun getItemWithKeywordOrNull(keyword: String) = items.firstOrNull { item ->
        item.keywords.contains(keyword)
    }

    inline fun <reified T> getTypedItemWithKeywordOrNull(keyword: String) =
        items.firstOrNull { item ->
            item is T && item.keywords.contains(keyword)
        } as? T
    // endregion

    // region get and remove
    fun getAndRemoveItemWithKeywordOrNull(keyword: String) =
        getItemWithKeywordOrNull(keyword)?.let {
            removeItem(it)
            it
        }

    fun getAndRemoveWeaponWithKeywordOrNull(keyword: String) =
        getTypedItemWithKeywordOrNull<ItemWeapon>(keyword)?.let {
            removeItem(it)
            it
        }

    fun getAndRemoveArmorWithKeywordOrNull(keyword: String) =
        getTypedItemWithKeywordOrNull<ItemArmor>(keyword)?.let {
            removeItem(it)
            it
        }

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

    fun getAndRemoveBestArmorOrNull(minDefense: Int = 0): ItemArmor? {
        val bestArmor = items.asSequence()
            .filterIsInstance<ItemArmor>()
            .maxByOrNull { if (it.defense > minDefense) it.defense else Int.MIN_VALUE }

        bestArmor?.let { items.remove(it) }

        return bestArmor
    }
    // endregion
}
