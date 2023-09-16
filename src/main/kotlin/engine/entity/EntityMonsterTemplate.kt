package engine.entity

import engine.item.template.ItemTemplates

class EntityMonsterTemplate(
    val name: String,
    val level: Int,
    val attributes: EntityAttributes,
    val keywords: List<String>,
    val experience: Int,
    val gold: Int
) {
    fun create() = EntityMonster(
        level = this.level,
        monsterName = this.name,
        experience = this.experience,
        gold = this.gold,
        attributes = EntityAttributes(
            strength = this.attributes.strength,
            intelligence = this.attributes.intelligence,
            vitality = this.attributes.vitality,
            speed = this.attributes.speed,
            baseDefense = this.attributes.baseDefense,
            maximumHealth = this.attributes.maximumHealth,
            maximumMagic = this.attributes.maximumMagic
        ),
        keywords = this.keywords,
        armor = ItemTemplates.armor.getOrNull(0)?.createItem(),
        weapon = ItemTemplates.weapons.getOrNull(0)?.createItem()
    )
}
