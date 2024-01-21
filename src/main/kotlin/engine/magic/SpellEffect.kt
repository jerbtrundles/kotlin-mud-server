package engine.magic
data class SpellEffect(
    val type: SpellEffectType,
    val target: SpellTarget,
    val strength: Int
) {
    companion object {
        fun fromJson(jsonString: String) =
            with(jsonString.split(" ")) {
                SpellEffect(
                    type = SpellEffectType.valueOf(this[0]),
                    target = SpellTarget.valueOf(this[1]),
                    strength = this[2].toInt()
                )
            }
    }
}

