package engine.entity

class EntityFaction(
    val faction: EntityFactions,
    val relationships: Map<EntityFactions, Int>
) {
    fun isHostileTo(faction: EntityFaction) =
        relationships.getOrDefault(faction.faction, DEFAULT_FRIENDLY_RELATIONSHIP_VALUE) < 0

    val myHostileFactions
        get() = EntityFactions.entries.filter {
            relationships.getOrDefault(it, DEFAULT_FRIENDLY_RELATIONSHIP_VALUE) < 0
        }

    companion object {
        val factionNpc = EntityFaction(
            faction = EntityFactions.NPC,
            relationships = mapOf(EntityFactions.MONSTER to -100)
        )

        val factionMonster = EntityFaction(
            faction = EntityFactions.MONSTER,
            relationships = mapOf(EntityFactions.NPC to -100)
        )

        val factionPlayer = EntityFaction(
            faction = EntityFactions.PLAYER,
            relationships = mapOf(
                EntityFactions.NPC to 100,
                EntityFactions.MONSTER to -100
            )
        )

        private const val DEFAULT_FRIENDLY_RELATIONSHIP_VALUE = 100
    }
}