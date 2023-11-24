package engine.entity

import debug.Debug
import engine.Inventory
import engine.Message
import engine.Messages
import engine.entity.behavior.EntityBehavior
import engine.item.ItemArmor
import engine.item.ItemWeapon
import engine.magic.Spell

class EntityFriendlyNpc(
    name: String,
    level: Int,
    job: String,
    behavior: EntityBehavior,
    experience: Int = 0,
    gold: Int = 0,
    arriveStringSuffix: String = "walks in",
    delayMin: Int = Debug.npcDelayMin,
    delayMax: Int = Debug.npcDelayMax,
    attributes: EntityAttributes = EntityAttributes.defaultNpc,
    inventory: Inventory = Inventory.defaultNpc(),
    weapon: ItemWeapon? = null,
    armor: ItemArmor? = null,
    spells: MutableMap<String, Spell> = mutableMapOf()
) : EntityBase(
    faction = EntityFaction.factionNpc,
    name = name,
    level = level,
    job = job,
    attributes = attributes,
    keywords = listOf(name),
    experience = experience,
    gold = gold,
    behavior = behavior,
    actionDelayMin = delayMin,
    actionDelayMax = delayMax,
    arriveStringSuffix = arriveStringSuffix,
    inventory = inventory,
    weapon = weapon,
    armor = armor,
    spells = spells
) {
    override val canTravelBetweenRegions = true

    // region names
    override val nameWithJob = "$name the $job"
    override val fullName = nameWithJob

    // "$arriveName has arrived."
    override val arriveName = fullName

    // "The body of $finalCleanupName crumbles to dust."
    override val finalCleanupName = nameWithJob

    // "$stringPrefix$deathName dies."
    override val deathName = nameWithJob

    override val randomName
        get() = arrayOf(nameWithJob, name).random()

    // "...says to $conversationalName, 'Hello!'"
    override val conversationalName
        get() = randomName
    override val deadConversationalName
        get() = "the spirit of $randomName"
    // endregion

    override fun calculateAttackPower() =
        attributes.strength + (weapon?.power ?: 0) + Debug.npcAttackBuff
    override fun processDeath() {
        GameStats.numNpcsKilled++
        GameStats.sendStatsToPlayers()
    }

    override fun say(what: String) = Messages.get(Message.ENTITY_SAYS, prefixedRandomName, what)
}
