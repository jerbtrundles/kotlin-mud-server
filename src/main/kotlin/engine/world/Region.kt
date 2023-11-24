package engine.world

import engine.entity.EntityManager
import engine.entity.EntityMonsterTemplate

class Region(
    val id: Int,
    val name: String,
    val subregions: List<Subregion>,
    monsterTemplates: List<EntityMonsterTemplate>,
    maxMonsters: Int,
    maxNpcs: Int,
    maxJanitors: Int,
    maxHealers: Int
) {
    val entityManager = EntityManager(
        region = this,
        maxMonsters = maxMonsters,
        maxNpcs = maxNpcs,
        maxJanitors = maxJanitors,
        maxHealers = maxHealers,
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
}