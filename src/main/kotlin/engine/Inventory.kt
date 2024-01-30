package engine

import debug.Debug
import engine.entity.body.EntityBodyPart
import engine.item.*
import engine.item.armor.ItemArmor
import engine.item.template.ItemTemplate
import engine.item.template.ItemTemplates
import engine.utility.Common
import engine.utility.Common.d100
import java.util.*

class Inventory(
    val items: MutableList<ItemBase> = Collections.synchronizedList(mutableListOf())
) {
    val armor: Sequence<ItemArmor>
        get() = items.asSequence().filterIsInstance<ItemArmor>()

    companion object {
        fun defaultMonster() =
            Inventory()

        fun defaultNpc() =
            Inventory()

        fun createWithRandomStuff(): Inventory =
            with(Inventory()) {
                repeat(5) {
                    randomChanceAddItemToList(ItemTemplates.junk)
                    randomChanceAddItemToList(ItemTemplates.food)
                    randomChanceAddItemToList(ItemTemplates.drinks)
                    randomChanceAddItemToList(ItemTemplates.weapons)
                    randomChanceAddItemToList(ItemTemplates.armorHumanoidChest)
                }

                return this
            }

        fun parseFromStringList(itemStrings: List<String>) =
            if (itemStrings.isNotEmpty()) {
                Inventory(
                    items = Collections.synchronizedList(
                        itemStrings.map { ItemTemplates.createItemFromString(it) }.toMutableList()
                    )
                )
            } else {
                Inventory()
            }
    }

    val valuableItems
        get() = items.filter { it.value > Debug.VALUABLE_ITEM_MINIMUM_VALUE }

    // region empty checks
    fun isEmpty() =
        items.isEmpty()

    fun isNotEmpty() =
        items.isNotEmpty()
    // endregion

    // region strings
    val itemsTextString
        get() = "ITEMS:${items.joinToString(separator = "\n") { it.name }}"

    override fun toString() = collectionString
    val collectionString
        get() = Common.collectionString(items.map { it.name })
    // endregion

    // region add/remove
    fun addInventory(other: Inventory) =
        items.addAll(other.items)

    fun addItem(item: ItemBase) =
        items.add(item)

    private fun randomChanceAddItemToList(templates: List<ItemTemplate>, percentChance: Int = 20) =
        d100(percentChance) {
            templates.randomOrNull()?.let { items.add(it.createItem()) }
        }

    fun removeItem(item: ItemBase) =
        items.remove(item)
    // endregion

    // region contains item
    fun containsValuableItem() =
        items.any { it.value >= Debug.VALUABLE_ITEM_MINIMUM_VALUE }

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
    fun getBestWeaponOrNull(minPower: Int = 0) =
        items.asSequence()
            .filterIsInstance<ItemWeapon>()
            .maxByOrNull { if (it.power > minPower) it.power else Int.MIN_VALUE }

    fun getAndRemoveBetterArmorOrNull(bodyPart: EntityBodyPart) =
        // filter by slot
        armor.filter { it.slot == bodyPart.slot }
            // get item with highest defense
            .maxByOrNull { it.defense }?.let { bestArmor ->
                // compare to equipped
                if (bestArmor.defense > (bodyPart.equippedItem?.defense ?: 0)) {
                    items.remove(bestArmor)
                    bestArmor
                } else {
                    null
                }
            }
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

    fun getItemWithKeywordOrNull(keyword: String) =
        items.firstOrNull {
            it.keywords.contains(keyword)
        }

    private inline fun <reified T : ItemBase> getTypedItemWithKeywordOrNull(keyword: String) =
        items.firstOrNull {
            it is T && it.keywords.contains(keyword)
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
        items.randomOrNull()?.let {
            items.remove(it)
            it
        }

    fun getAndRemoveRandomValuableItem() =
        valuableItems.randomOrNull()?.let {
            items.remove(it)
            it
        }

    private inline fun <reified T : ItemBase> getRandomTypedItemOrNull() =
        items.filterIsInstance<T>().randomOrNull()

    private inline fun <reified T : ItemBase> getAndRemoveRandomTypedItemOrNull(): T? =
        getRandomTypedItemOrNull<T>()?.let {
            items.remove(it)
            it
        }

    fun getAndRemoveRandomWeaponOrNull() =
        getAndRemoveRandomTypedItemOrNull<ItemWeapon>()

    fun getAndRemoveRandomArmorOrNull(bodyPart: EntityBodyPart) =
        armor.filter { it.slot == bodyPart.slot }
            .toList()
            .randomOrNull()
            ?.let { randomArmor ->
                items.remove(randomArmor)
                randomArmor
            }

    fun getAndRemoveRandomBetterWeaponOrNull(minRequiredPower: Int) =
        items.filterIsInstance<ItemWeapon>()
            .filter { it.power >= minRequiredPower }
            .randomOrNull()?.let {
                items.remove(it)
                it
            }
    // endregion

    fun containsBetterArmor(bodyPart: EntityBodyPart) =
        armor.filter { it.slot == bodyPart.slot }
            .maxByOrNull { it.defense }
            ?.let { bestArmor ->
                bestArmor.defense > (bodyPart.equippedItem?.defense ?: 0)
            } ?: false
}
