package engine.entity

import GameStats
import debug.Debug
import engine.Inventory
import engine.entity.behavior.EntityBehavior
import engine.entity.body.EntityBody
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
    namePrefix: String = "The ",
    arriveStringSuffix: String = "has arrived",
    inventory: Inventory = Inventory.defaultMonster(),
    body: EntityBody = EntityBody.critter()
) : EntityBase(
    faction = EntityFaction.factionMonster,
    name = monsterName,
    level = level,
    keywords = keywords,
    attributes = attributes,
    behavior = behavior,
    weapon = weapon,
    namePrefix = namePrefix,
    experience = experience,
    gold = gold,
    actionDelayMin = Debug.monsterDelayMin,
    actionDelayMax = Debug.monsterDelayMax,
    arriveStringSuffix = arriveStringSuffix,
    inventory = inventory,
    body = body
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
        val old = GameStats.monstersKilled[name] ?: 0
        GameStats.monstersKilled[name] = old + 1
        GameStats.totalMonstersKilled++
        GameStats.sendStatsToPlayers()
    }

    // TODO: monsters can speak in different ways; specify in monster config
    override fun say(what: String) = ""
}
