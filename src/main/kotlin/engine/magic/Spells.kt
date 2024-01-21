package engine.magic

import debug.Debug
import engine.utility.Common

object Spells {
//    "name": "minor fire",
//    "cost": 5,
//    "effects": [
//    "FIRE_DAMAGE 5-10"
//    ],
//    "attack-strings": [
//    "Flames leap from your fingers, burning the !target for !damage damage."
//    ]

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