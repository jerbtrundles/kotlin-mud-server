package engine.world

import engine.entity.core.EntityManager
import engine.entity.template.EntityMonsterTemplate
import engine.utility.Common
import engine.world.template.RegionTemplate

class Region(
    val id: String,
    val name: String,
    val subregions: List<Subregion>,
    monsterTemplates: List<EntityMonsterTemplate>,
    maxMonsters: Int,
    maxNpcs: Int,
    maxJanitors: Int,
    maxHealers: Int,
    maxWizards: Int,
    maxFarmers: Int
) {
    val entityManager = EntityManager(
        region = this,
        maxMonsters = maxMonsters,
        maxNpcs = maxNpcs,
        maxJanitors = maxJanitors,
        maxHealers = maxHealers,
        maxWizards = maxWizards,
        maxFarmers = maxFarmers,
        monsterTemplates = monsterTemplates
    )

    val randomRoom
        get() = allRooms.random()
    private val allRooms by lazy { subregions.flatMap { subregion -> subregion.rooms } }
    override fun toString() = "Region: $id - $name"
    val displayString = name

    fun toDebugString(): String {
        val sb = StringBuilder()
        sb.appendLine("world.Region ID: $id")
        sb.appendLine("world.Region name: $name")
        subregions.forEach { subregion ->
            sb.appendLine(subregion)
        }
        return sb.toString()
    }

    companion object {
        fun fromFilePath(c: Class<() -> Unit>, filePath: String) =
            Common.parseFromJson<RegionTemplate>(c, filePath).toRegion()
    }
}