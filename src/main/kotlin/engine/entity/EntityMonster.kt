package engine.entity

import debug.Debug
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
    stringPrefix: String = "The ",
    arriveStringSuffix: String = "has arrived"
) : EntityBase(
    faction = EntityFaction.factionMonster,
    name = monsterName,
    level = level,
    keywords = keywords,
    attributes = attributes,
    behavior = behavior,
    weapon = weapon,
    armor = armor,
    stringPrefix = stringPrefix,
    experience = experience,
    gold = gold,
    delayMin = Debug.monsterDelayMin,
    delayMax = Debug.monsterDelayMax,
    arriveStringSuffix = arriveStringSuffix
) {
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
    override val nameForCollectionString
        get() = when {
            isDead && !hasNotBeenSearched -> "dead $name (searched)"
            isDead -> "dead $name"
            posture == EntityPosture.KNEELING -> "$name (kneeling)"
            posture == EntityPosture.SITTING -> "$name (sitting)"
            else -> name
        }
    // "...says to $conversationalName, 'Hello!'"
    override val conversationalName
        get() = "the $name"


    override val nameForStory = "The $name"
    override fun calculateAttackPower() =
        attributes.strength + (weapon?.power ?: 0) - Debug.monsterAttackDebuff // debug monster attack debuff
}