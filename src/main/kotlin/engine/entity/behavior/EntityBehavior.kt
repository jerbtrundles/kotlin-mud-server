package engine.entity.behavior

import engine.entity.core.EntityBase
import kotlin.random.Random

class EntityBehavior(private val preferences: List<EntityPreference>) {
    companion object {
        val defaultNpc = EntityBehavior(
            listOf(
                // EntityPreference.defaultPreferenceQuipLikeABoss,
//                EntityPreference.defaultPreferenceFindAnyWeaponIfNoneEquipped,
//                EntityPreference.defaultPreferenceFindValuableItem,
//                EntityPreference.defaultPreferenceSearchDeadHostile,
//                EntityPreference.defaultPreferenceAttackLivingHostile,
//                EntityPreference.defaultPreferenceFindBetterWeapon,
                EntityPreference.defaultPreferenceFindBetterArmor,
                EntityPreference.defaultPreferenceEatFood,
                EntityPreference.defaultPreferenceDrink,
            )
        )

        val defaultMonster = EntityBehavior(
            listOf(
                // EntityPreference.defaultPreferenceQuipLikeABoss,
                EntityPreference.defaultPreferenceFindRandomWeaponIfNoneEquipped,
                // EntityPreference.defaultPreferenceAttackPlayer,
                EntityPreference.defaultPreferenceSearchDeadHostile,
                EntityPreference.defaultPreferenceAttackLivingHostile,
                EntityPreference.defaultPreferenceFindBetterWeapon,
                EntityPreference.defaultPreferenceFindBetterArmor
            )
        )

        val healer = EntityBehavior(
            listOf(
                EntityPreference.defaultPreferenceHealFriendly,
                EntityPreference.defaultPreferenceCastDamageSpellAtLivingHostile,
                EntityPreference.defaultPreferenceFindRandomWeaponIfNoneEquipped,
                EntityPreference.defaultPreferenceFindValuableItem,
                EntityPreference.defaultPreferenceSearchDeadHostile,
                EntityPreference.defaultPreferenceAttackLivingHostile,
                EntityPreference.defaultPreferenceFindBetterWeapon,
                EntityPreference.defaultPreferenceFindBetterArmor
            )
        )

        val wizard = EntityBehavior(
            listOf(
                EntityPreference.defaultPreferenceCastDamageSpellAtLivingHostile,
                EntityPreference.defaultPreferenceSearchDeadHostile,
                EntityPreference.defaultPreferenceAttackLivingHostile,
                EntityPreference.defaultPreferenceFindValuableItem,
                // TODO: wizards would only care about certain kinds of armor
                //  any class might care about only some types of armor
//                EntityPreference.defaultPreferenceFindAnyWeaponIfNoneEquipped,
//                EntityPreference.defaultPreferenceFindBetterWeapon,
//                EntityPreference.defaultPreferenceFindBetterArmor
            )
        )

        val janitor = EntityBehavior(
            listOf(
                EntityPreference.defaultPreferenceFindBetterWeapon,
                EntityPreference.defaultPreferenceFindBetterArmor,
                EntityPreference.defaultPreferenceFindValuableItem,
                EntityPreference.janitorPreferenceDestroyItemInRoom,
                EntityPreference.defaultPreferenceSearchDeadHostile
            )
        )

        val farmer = EntityBehavior(
            listOf(
                EntityPreference.defaultPreferenceAttackLivingHostile,
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
            EntityAction.QUIP_TO_RANDOM_ENTITY,
            EntityAction.IDLE_FLAVOR_ACTION,
            EntityAction.EAT_RANDOM_FOOD,
            EntityAction.DRINK_RANDOM_DRINK
        )

        fun randomIdleAction() =
            when (Random.nextInt(100)) {
                in 0..80 -> EntityAction.MOVE
                else -> idleActions.random()
            }
    }

    fun getNextAction(entity: EntityBase) =
        preferences.firstOrNull { preference ->
            preference.situations.all { situation -> entity.isInSituation(situation) }
        }?.action ?: EntityAction.IDLE
}