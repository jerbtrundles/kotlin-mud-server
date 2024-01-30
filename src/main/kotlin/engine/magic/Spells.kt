package engine.magic

import debug.Debug
import engine.utility.Common

object Spells {
    private lateinit var spellMap: Map<String, Spell>

    operator fun get(spellName: String) = spellMap[spellName]!!

    fun load(c: Class<() -> Unit>) {
        Debug.println("Loading spells...")
        val spellsJson = Common.parseArrayFromJson<SpellJson>(c, "/spells.json")
        spellMap = spellsJson
            .map { it.toSpell() }
            .associateBy({ it.name }, { it })
        Debug.println("Done loading spells. We gots magics of ${spellMap.size} kinds, huzzah!")
    }

    private class SpellJson(
        val name: String,
        val cost: Int,
        val effects: List<String>
    ) {
        fun toSpell(): Spell {
            return Spell(
                name = name,
                cost = cost,
                effects = effects.map { SpellEffect.fromJson(it) }
            )
        }
    }
}
