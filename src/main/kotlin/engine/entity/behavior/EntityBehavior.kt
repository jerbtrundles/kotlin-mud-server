package engine.entity.behavior

import engine.entity.EntityBase
import kotlin.random.Random

class EntityBehavior(private val preferences: List<EntityPreference>) {
    companion object {
        val defaultNpc = EntityBehavior(
            listOf(
                // EntityPreference.defaultPreferenceQuipLikeABoss,
                EntityPreference.defaultPreferenceFindAnyWeaponIfNoneEquipped,
                EntityPreference.defaultPreferenceFoundValuableItem,
                EntityPreference.defaultPreferenceSearchDeadHostile,
                EntityPreference.defaultPreferenceAttackLivingHostile,
                EntityPreference.defaultPreferenceFindBetterWeapon,
                EntityPreference.defaultPreferenceFindBetterArmor
            )
        )

        val defaultMonster = EntityBehavior(
            listOf(
                EntityPreference.defaultPreferenceEatFood,
                EntityPreference.defaultPreferenceDrink,
                // EntityPreference.defaultPreferenceQuipLikeABoss,
                EntityPreference.defaultPreferenceFindAnyWeaponIfNoneEquipped,
                // EntityPreference.defaultPreferenceAttackPlayer,
                EntityPreference.defaultPreferenceSearchDeadHostile,
                EntityPreference.defaultPreferenceAttackLivingHostile,
                EntityPreference.defaultPreferenceFindBetterWeapon,
                EntityPreference.defaultPreferenceFindBetterArmor
            )
        )

        val janitor = EntityBehavior(
            listOf(
                EntityPreference.defaultPreferenceFindBetterWeapon,
                EntityPreference.defaultPreferenceFindBetterArmor,
                EntityPreference.janitorPreferenceDestroyItemInRoom,
                EntityPreference.defaultPreferenceSearchDeadHostile
            )
        )

        // berserker runs around with no weapons or armor looking for things to attack
        val berserker = EntityBehavior(
            listOf(
                EntityPreference.defaultPreferenceAttackLivingHostile,
                EntityPreference.berserkerPreferenceAlwaysMoving,
            )
        )

        private val idleActions = listOf(
            EntityAction.GET_RANDOM_ITEM,
//            EntityAction.SIT,
//            EntityAction.KNEEL,
            EntityAction.QUIP_TO_RANDOM_ENTITY,
            EntityAction.IDLE_FLAVOR_ACTION,
            EntityAction.EAT_RANDOM_FOOD,
            EntityAction.DRINK_RANDOM_DRINK
        )

        fun randomIdleAction() = when (Random.nextInt(100)) {
            in 0..80 -> EntityAction.MOVE
            else -> idleActions.random()
        }
    }

    fun getNextAction(entity: EntityBase) =
        preferences.firstOrNull { preference ->
            preference.situations.all { situation -> entity.isInSituation(situation) }
        }?.action ?: EntityAction.IDLE
}