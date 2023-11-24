package engine.magic
data class SpellEffect(
    val type: SpellEffectType,
    val target: SpellTarget,
    val strength: Int
)

