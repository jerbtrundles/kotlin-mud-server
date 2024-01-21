package engine.magic

class Spell(
    val name: String,
    val cost: Int,
    val effects: List<SpellEffect>
) {
    fun hasEffectType(effectType: SpellEffectType) = effects.any { it.type == effectType }
}

