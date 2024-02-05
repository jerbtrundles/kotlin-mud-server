package engine.entity.core

import debug.Debug
import engine.Inventory
import engine.Message
import engine.Messages
import engine.entity.attributes.EntityAttributes
import engine.entity.attributes.EntityClass
import engine.entity.attributes.EntityNames
import engine.entity.behavior.EntityBehavior
import engine.entity.body.EntityBody
import engine.entity.faction.EntityFaction
import engine.item.ItemWeapon

class EntityFriendlyNpc(
    name: String,
    level: Int,
    entityClass: EntityClass,
    behavior: EntityBehavior,
    experience: Int = 0,
    gold: Int = 0,
    body: EntityBody = EntityBody.humanoid(),
    arriveSuffix: String = "walks in",
    delayMin: Int = Debug.npcDelayMin,
    delayMax: Int = Debug.npcDelayMax,
    attributes: EntityAttributes = EntityAttributes.defaultNpc,
    inventory: Inventory = Inventory.defaultNpc(),
    weapon: ItemWeapon? = null,
    spells: MutableList<String> = mutableListOf(),
    override val canTravelBetweenRegions: Boolean = true
) : EntityBase(
    faction = EntityFaction.factionNpc,
    name = name,
    level = level,
    entityClass = entityClass,
    attributes = attributes,
    keywords = listOf(name),
    experience = experience,
    gold = gold,
    behavior = behavior,
    actionDelayMin = delayMin,
    actionDelayMax = delayMax,
    inventory = inventory,
    weapon = weapon,
    spells = spells,
    body = body
) {
    override val names = EntityNames.friendlyNpc(name, entityClass.toString().lowercase(), arriveSuffix)
    override fun calculateAttackPower() =
        attributes.strength + (weapon?.power ?: 0) + Debug.npcAttackBuff
    override fun processDeath() {
        GameStats.numNpcsKilled++
        GameStats.sendStatsToPlayers()
    }

    override fun say(what: String) = Messages.get(Message.ENTITY_SAYS, names.prefixedRandom(), what)
}
