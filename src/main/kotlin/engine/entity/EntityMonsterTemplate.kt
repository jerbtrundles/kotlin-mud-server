package engine.entity

import com.beust.klaxon.Json
import engine.Inventory
import engine.item.template.ItemTemplates

class EntityMonsterTemplate(
    @Json(name = "name")
    val name: String,
    @Json(name = "keywords")
    val keywords: List<String>,
    @Json(name = "level")
    val level: Int = 1,
    @Json(name = "attributes")
    val attributes: EntityAttributes = EntityAttributes.defaultCritter,
    @Json(name = "experience")
    val experience: Int = 0,
    @Json(name = "gold")
    val gold: Int = 0,
    @Json(name = "items")
    val itemsString: List<String> = emptyList()
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
        inventory = Inventory.parseFromStringList(itemsString),
        keywords = this.keywords,
        weapon = null, // ItemTemplates.weapons.getOrNull(0)?.createItem()
    )
}
