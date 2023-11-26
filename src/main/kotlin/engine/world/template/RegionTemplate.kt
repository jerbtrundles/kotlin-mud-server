package engine.world.template

import com.beust.klaxon.Json
import engine.entity.EntityManager
import engine.entity.EntityMonsterTemplate
import engine.entity.MonsterTemplates
import engine.world.Region

class RegionTemplate(
    @Json(name = "region-name")
    val name: String,
    @Json(name = "subregions")
    val subregionTemplates: List<SubregionTemplate>,
    @Json(name = "monsters")
    val monsters: List<String> = listOf(),
    @Json(name = "max-monsters")
    val maxMonsters: Int = 0,
    @Json(name = "max-janitors")
    val maxJanitors: Int = 0,
    @Json(name = "max-npcs")
    val maxNpcs: Int = 0,
    @Json(name = "max-healers")
    val maxHealers: Int = 0,
    @Json(name = "max-wizards")
    val maxWizards: Int = 0,
    @Json(name = "max-farmers")
    val maxFarmers: Int = 0
) {
    val id = nextInt()

    companion object {
        private var theInt = 0
        private fun nextInt() = theInt++
    }
    fun toRegion() =
        Region(
            id = id,
            name = name,
            subregions = subregionTemplates.mapIndexed { i, subregionTemplate -> subregionTemplate.toSubregion(id = i) },
            monsterTemplates = monsters.map { MonsterTemplates.get(it) },
            maxMonsters = maxMonsters,
            maxJanitors = maxJanitors,
            maxNpcs = maxNpcs,
            maxHealers = maxHealers,
            maxWizards = maxWizards,
            maxFarmers = maxFarmers
        )
}