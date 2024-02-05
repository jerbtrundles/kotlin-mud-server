package engine.entity.core

import GameStats
import debug.Debug
import engine.Inventory
import engine.entity.attributes.EntityAttributes
import engine.entity.attributes.EntityClass
import engine.entity.attributes.EntityNames
import engine.entity.behavior.EntityBehavior
import engine.entity.body.EntityBody
import engine.entity.faction.EntityFaction
import engine.item.ItemWeapon

class EntityMonster(
    monsterName: String,
    level: Int,
    keywords: List<String>,
    attributes: EntityAttributes,
    experience: Int,
    gold: Int,
    behavior: EntityBehavior = EntityBehavior.defaultMonster,
    weapon: ItemWeapon? = null,
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
    experience = experience,
    gold = gold,
    entityClass = EntityClass.MONSTER,
    actionDelayMin = Debug.monsterDelayMin,
    actionDelayMax = Debug.monsterDelayMax,
    inventory = inventory,
    body = body
) {
    override val names = EntityNames.monster(name, arriveStringSuffix)
    override val canTravelBetweenRegions = false

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
