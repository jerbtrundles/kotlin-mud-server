package engine.quest

import engine.entity.core.EntityBase
import engine.item.ItemBase

// questgiver: kill # <enemy/enemies>
class KillQuest(
    id: String,
    name: String,
    description: String,
    rewards: List<ItemBase>,
    questGiver: EntityBase,
    // KillQuest
    val targetEnemies: Map<EntityBase, Int>
) : Quest(
    id = id,
    name = name,
    description = description,
    rewards = rewards,
    questGiver = questGiver
) {
    private val killedEnemies: MutableMap<EntityBase, Int> = mutableMapOf()

    fun recordKill(enemy: EntityBase) {
        if (targetEnemies.contains(enemy)) {
            killedEnemies[enemy] = killedEnemies.getOrDefault(enemy, 0) + 1
        } else {
            // killed enemy not part of quest
        }
    }

    override fun isCompleted() =
        targetEnemies.all { (enemy, enemyCount) ->
            killedEnemies.getOrDefault(enemy, 0) >= enemyCount
        }
}
