package engine.magic

class Spell(
    val name: String,
    val cost: Int,
    val effects: List<SpellEffect>
) {
    fun hasEffectType(effectType: SpellEffectType) =
        effects.any { it.type == effectType }
    fun isDamageSpell() =
        effects.any {
            it.type == SpellEffectType.ICE_DAMAGE
                    || it.type == SpellEffectType.FIRE_DAMAGE
                    || it.type == SpellEffectType.LIGHTNING_DAMAGE
                    || it.type == SpellEffectType.NON_ELEMENTAL_DAMAGE
        }
}
