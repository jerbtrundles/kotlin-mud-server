package engine.item.template

import debug.Debug
import engine.utility.Common

object ItemTemplates {
    var junk = listOf<ItemTemplateJunk>()
    var drinks = listOf<ItemTemplateDrink>()
    var food = listOf<ItemTemplateFood>()
    var containers = listOf<ItemTemplateContainer>()
    var weapons = listOf<ItemTemplateWeapon>()
    var armor = listOf<ItemTemplateArmor>()

    fun load(c: Class<() -> Unit>) {
        loadJunk(c)
        loadDrinks(c)
        loadFood(c)
        loadContainers(c)
        loadWeapons(c)
        loadArmor(c)
    }

    private fun loadArmor(c: Class<() -> Unit>) {
        Debug.println("Loading armor...")
        armor = Common.parseArrayFromJson(c, "/items-armor.json")
        Debug.println("Done loading armor. We can defend ourselves with ${armor.size} different options.")
    }

    private fun loadWeapons(c: Class<() -> Unit>) {
        Debug.println("Loading weapons...")
        weapons = Common.parseArrayFromJson(c, "/items-weapon.json")
        Debug.println("Done loading weapons. We can kill enemies in ${weapons.size} different ways.")
    }

    private fun loadContainers(c: Class<() -> Unit>) {
        Debug.println("Loading containers...")
        containers = Common.parseArrayFromJson(c, "/items-container.json")
        Debug.println("Done loading containers. We can hold things in ${containers.size} types of containers.")
    }

    private fun loadFood(c: Class<() -> Unit>) {
        Debug.println("Loading food...")
        food = Common.parseArrayFromJson(c, "/items-food.json")
        Debug.println("Done loading food. We gots ${food.size} types of things to eat.")
    }

    private fun loadJunk(c: Class<() -> Unit>) {
        Debug.println("Loading junk...")
        junk = Common.parseArrayFromJson(c, "/items-junk.json")
        Debug.println("Done loading junk. Size of junk is ${junk.size}.")
    }

    private fun loadDrinks(c: Class<() -> Unit>) {
        Debug.println("Loading drinks...")
        drinks = Common.parseArrayFromJson(c, "/items-drink.json")
        Debug.println("Done loading drinks. We have ${drinks.size} drinks.")
    }

    fun find(itemString: String): ItemTemplate {
        return weapons.firstOrNull { template -> template.matches(itemString) }
            ?: armor.firstOrNull { template -> template.matches(itemString) }
            ?: food.firstOrNull { template -> template.matches(itemString) }
            ?: drinks.firstOrNull { template -> template.matches(itemString) }
            ?: junk.firstOrNull { template -> template.matches(itemString) }
            ?: containers.firstOrNull { template -> template.matches(itemString) }
            ?: throw Exception("No item template match for keyword: $itemString. This should never happen.")
    }
}