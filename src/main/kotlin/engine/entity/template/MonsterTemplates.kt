package engine.entity.template

import debug.Debug
import engine.utility.Common

object MonsterTemplates {
    var monsterTemplates = listOf<EntityMonsterTemplate>()

    fun load(c: Class<() -> Unit>) {
        Debug.println("Loading monsters...")
        monsterTemplates = Common.parseArrayFromJson<EntityMonsterTemplate>(c, "/monsters.json")
        Debug.println("Done loading monsters. ${monsterTemplates.size} types of enemies are out to get us!")
    }

    fun get(monsterName: String) =
        monsterTemplates.first { it.name == monsterName }
}