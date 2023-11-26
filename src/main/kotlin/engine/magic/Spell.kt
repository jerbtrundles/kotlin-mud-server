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
            cost = 2
            // "prefixedName's health is slightly restored."
        )

        val spellMinorFire = Spell(
            name = "minor fire",
            effects = listOf(
                SpellEffect(
                    type = SpellEffectType.FIRE_DAMAGE,
                    target = SpellTarget.TARGET,
                    strength = 5
                )
            ),
            cost = 2
            // "A ball of flame hurtles toward prefixedName, dealing x damage."
        )
    }

    fun hasEffectType(effectType: SpellEffectType) = effects.any { it.type == effectType }
}

