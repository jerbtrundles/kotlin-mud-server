package engine.item.template

import debug.Debug
import engine.utility.Common

object ItemTemplates {
    var junk = listOf<ItemTemplateJunk>()
    var drinks = listOf<ItemTemplateDrink>()
    var food = listOf<ItemTemplateFood>()
    var containers = listOf<ItemTemplateContainer>()
    var weapons = listOf<ItemTemplateWeapon>()
    var armorHumanoidHead = listOf<ItemTemplateArmor>()
    var armorHumanoidChest = listOf<ItemTemplateArmor>()
    var armorHumanoidArms = listOf<ItemTemplateArmor>()
    var armorHumanoidLegs = listOf<ItemTemplateArmor>()
    var armorHumanoidHands = listOf<ItemTemplateArmor>()
    var armorHumanoidFeet = listOf<ItemTemplateArmor>()

    var armor: List<ItemTemplateArmor> = listOf()
        get() = armorHumanoidFeet + armorHumanoidArms + armorHumanoidHead +
                armorHumanoidChest + armorHumanoidHands + armorHumanoidLegs

    var gems = listOf<ItemTemplateGem>()

    fun load(c: Class<() -> Unit>) {
        loadJunk(c)
        loadDrinks(c)
        loadFood(c)
        loadContainers(c)
        loadWeapons(c)
        loadArmor(c)
        loadGems(c)
    }

    private fun loadArmor(c: Class<() -> Unit>) {
        loadArmorHumanoidChest(c)
        loadArmorHumanoidHead(c)
        loadArmorHumanoidFeet(c)
        loadArmorHumanoidHands(c)
        loadArmorHumanoidArms(c)
        loadArmorHumanoidLegs(c)
    }

    private fun loadArmorHumanoidChest(c: Class<() -> Unit>) {
        Debug.println("Loading humanoid chest armor...")
        armorHumanoidChest = Common.parseArrayFromJson(c, "/armor/items-armor-humanoid-chest.json")
        Debug.println("Done loading humanoid chest armor. ${armorHumanoidChest.size}")
    }

    private fun loadArmorHumanoidHead(c: Class<() -> Unit>) {
        Debug.println("Loading humanoid head armor...")
        armorHumanoidHead = Common.parseArrayFromJson(c, "/armor/items-armor-humanoid-head.json")
        Debug.println("Done loading humanoid head armor. ${armorHumanoidHead.size}")
    }

    private fun loadArmorHumanoidArms(c: Class<() -> Unit>) {
        Debug.println("Loading humanoid arms armor...")
        armorHumanoidArms = Common.parseArrayFromJson(c, "/armor/items-armor-humanoid-arms.json")
        Debug.println("Done loading humanoid arms armor. ${armorHumanoidArms.size}")
    }

    private fun loadArmorHumanoidLegs(c: Class<() -> Unit>) {
        Debug.println("Loading humanoid legs armor...")
        armorHumanoidLegs = Common.parseArrayFromJson(c, "/armor/items-armor-humanoid-legs.json")
        Debug.println("Done loading humanoid legs armor. ${armorHumanoidLegs.size}")
    }

    private fun loadArmorHumanoidHands(c: Class<() -> Unit>) {
        Debug.println("Loading humanoid hands armor...")
        armorHumanoidHands = Common.parseArrayFromJson(c, "/armor/items-armor-humanoid-hands.json")
        Debug.println("Done loading humanoid hands armor. ${armorHumanoidHands.size}")
    }

    private fun loadArmorHumanoidFeet(c: Class<() -> Unit>) {
        Debug.println("Loading humanoid feet armor...")
        armorHumanoidFeet = Common.parseArrayFromJson(c, "/armor/items-armor-humanoid-feet.json")
        Debug.println("Done loading humanoid feet armor. ${armorHumanoidFeet.size}")
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

    private fun loadGems(c: Class<() -> Unit>) {
        Debug.println("Loading gems...")
        gems = Common.parseArrayFromJson(c, "/items-gem.json")
        Debug.println("Done loading gems. We have ${gems.size} shiny and valuable rocks.")
    }

    fun find(itemString: String): ItemTemplate {
        return weapons.firstOrNull { template -> template.matches(itemString) }
            ?: armorHumanoidChest.firstOrNull { template -> template.matches(itemString) }
            ?: armorHumanoidHead.firstOrNull { template -> template.matches(itemString) }
            ?: armorHumanoidArms.firstOrNull { template -> template.matches(itemString) }
            ?: armorHumanoidLegs.firstOrNull { template -> template.matches(itemString) }
            ?: armorHumanoidHands.firstOrNull { template -> template.matches(itemString) }
            ?: armorHumanoidFeet.firstOrNull { template -> template.matches(itemString) }
            ?: food.firstOrNull { template -> template.matches(itemString) }
            ?: drinks.firstOrNull { template -> template.matches(itemString) }
            ?: junk.firstOrNull { template -> template.matches(itemString) }
            ?: containers.firstOrNull { template -> template.matches(itemString) }
            ?: gems.firstOrNull { template -> template.matches(itemString) }
            ?: throw Exception("No item template match for keyword: $itemString. This should never happen.")
    }

    fun createItemFromString(itemString: String) =
        find(itemString).createItem()
}