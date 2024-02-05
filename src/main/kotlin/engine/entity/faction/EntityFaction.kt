package engine.entity.faction

class EntityFaction(
    val id: EntityFactions,
    val relationships: Map<EntityFactions, Int>
) {
    fun isHostileTo(otherId: EntityFactions) =
        relationships.getOrDefault(otherId, DEFAULT_FRIENDLY_RELATIONSHIP_VALUE) < 0
    fun isHostileTo(faction: EntityFaction) =
        relationships.getOrDefault(faction.id, DEFAULT_FRIENDLY_RELATIONSHIP_VALUE) < 0

    val myHostileFactions
        get() = EntityFactions.entries.filter {
            relationships.getOrDefault(it, DEFAULT_FRIENDLY_RELATIONSHIP_VALUE) < 0
        }

    companion object {
        val factionNpc = EntityFaction(
            id = EntityFactions.NPC,
            relationships = mapOf(EntityFactions.MONSTER to -100)
        )

        val factionMonster = EntityFaction(
            id = EntityFactions.MONSTER,
            relationships = mapOf(EntityFactions.NPC to -100)
        )

        val factionPlayer = EntityFaction(
            id = EntityFactions.PLAYER,
            relationships = mapOf(
                EntityFactions.NPC to 100,
                EntityFactions.MONSTER to -100
            )
        )

        private const val DEFAULT_FRIENDLY_RELATIONSHIP_VALUE = 100
    }
}