package engine.entity

import debug.Debug
import engine.Inventory
import engine.entity.behavior.EntityBehavior
import engine.item.ItemArmor
import engine.item.ItemWeapon
import engine.utility.withIndefiniteArticle

class EntityMonster(
    monsterName: String,
    level: Int,
    keywords: List<String>,
    attributes: EntityAttributes,
    experience: Int,
    gold: Int,
    behavior: EntityBehavior = EntityBehavior.defaultMonster,
    weapon: ItemWeapon? = null,
    armor: ItemArmor? = null,
    namePrefix: String = "The ",
    arriveStringSuffix: String = "has arrived",
    inventory: Inventory = Inventory.defaultMonster()
) : EntityBase(
    faction = EntityFaction.factionMonster,
    name = monsterName,
    level = level,
    keywords = keywords,
    attributes = attributes,
    behavior = behavior,
    weapon = weapon,
    armor = armor,
    namePrefix = namePrefix,
    experience = experience,
    gold = gold,
    actionDelayMin = Debug.monsterDelayMin,
    actionDelayMax = Debug.monsterDelayMax,
    arriveStringSuffix = arriveStringSuffix,
    inventory = inventory
) {
    override val canTravelBetweenRegions = false

    override val nameWithJob
        get() = name
    override val fullName
        get() = name
    override val randomName
        get() = name
    override val finalCleanupName = "the $name"
    override val deadConversationalName = "the spirit of $conversationalName"
    override val arriveName = name.withIndefiniteArticle(capitalized = true)
    override val deathName = name
    // "...says to $conversationalName, 'Hello!'"
    override val conversationalName
        get() = "the $name"


    override val nameForStory = "The $name"
    override fun calculateAttackPower() =
        attributes.strength + (weapon?.power ?: 0) - Debug.monsterAttackDebuff // debug monster attack debuff

    override fun processDeath() {
        GameStats.numMonstersKilled++
        GameStats.sendStatsToPlayers()
    }

    // TODO: monsters can speak in different ways; specify in monster config
    override fun say(what: String) = ""
}
