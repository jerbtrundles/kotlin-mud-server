package engine.quest

import engine.entity.core.EntityBase
import engine.item.ItemBase
import kotlin.random.Random

class KillQuestTemplate(
    // QuestTemplate
    override val description: String,
    override val id: String,
    override val name: String,
    override val rewards: List<ItemBase>,
    // KillQuestTemplate
    val targetEnemies: Map<EntityBase, IntRange>
) : QuestTemplate {
    // TODO: allow variance with targetEnemy, requiredKills?
    //  targetEnemy as a list?
    override fun toQuest(questGiver: EntityBase) =
        KillQuest(
            id = id,
            name = name,
            description = description,
            rewards = rewards,
            questGiver = questGiver,
            targetEnemies = transformToRandomMap(targetEnemies)
        )

    private fun transformToRandomMap(targetEnemies: Map<EntityBase, IntRange>): Map<EntityBase, Int> {
        return targetEnemies.mapValues { (_, range) ->
            Random.nextInt(range.first, range.last)
        }
    }
}