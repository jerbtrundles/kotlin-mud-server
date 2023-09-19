package engine.entity

import debug.Debug
import engine.Inventory
import engine.entity.behavior.EntityBehavior

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
    inventory: Inventory = Inventory.defaultNpc()
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
    delayMin = delayMin,
    delayMax = delayMax,
    arriveStringSuffix = arriveStringSuffix,
    inventory = inventory
) {
    override val deadConversationalName
        get() = "the spirit of $randomName"
    override val nameWithJob
        get() = "$name the $job"
    override val fullName
        get() = nameWithJob
    override val randomName
        get() = arrayOf(nameWithJob, name).random()

    // "...says to $conversationalName, 'Hello!'"
    override val conversationalName
        get() = randomName

    // "The body of $finalCleanupName crumbles to dust."
    override val finalCleanupName get() = nameWithJob

    // "$arriveName has arrived."
    override val arriveName: String
        get() = fullName

    // "$stringPrefix$deathName dies."
    override val deathName = nameWithJob

    // "..., a goblin (kneeling), ..."
    override val nameForCollectionString
        get() = when {
            isDead -> "$randomName (dead)"
            posture == EntityPosture.KNEELING -> "$randomName (kneeling)"
            posture == EntityPosture.SITTING -> "$randomName (sitting)"
            else -> name
        }

    override fun calculateAttackPower() =
        attributes.strength + (weapon?.power ?: 0) + Debug.npcAttackBuff
}
