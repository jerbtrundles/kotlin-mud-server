package engine.magic

class Spell(
    val name: String,
    val effects: List<SpellEffect>,
    val cost: Int
) {
    companion object {
        val spellMinorCure = Spell(
            name = "minor cure",
            effects = listOf(
                SpellEffect(
                    type = SpellEffectType.RESTORE_HEALTH,
                    target = SpellTarget.TARGET,
                    strength = 5
                )
            ),
            cost = 5
            // "prefixedName's health is slightly restored."
        )
    }

    fun hasEffectType(effectType: SpellEffectType) = effects.any { it.type == effectType }
}

