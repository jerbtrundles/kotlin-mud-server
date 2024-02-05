package engine.entity.behavior

class EntityPreference(
    val situations: List<EntitySituation>,
    val action: EntityAction
) {
    constructor(situation: EntitySituation, action: EntityAction) : this(listOf(situation), action)

    companion object {
        // region default
        val defaultPreferenceAttackPlayer = EntityPreference(
            EntitySituation.ROOM_CONTAINS_LIVING_PLAYER, EntityAction.ATTACK_PLAYER
        )
        val defaultPreferenceBeAlone = EntityPreference(
            EntitySituation.NOT_ALONE, EntityAction.MOVE
        )
        val defaultPreferenceSit = EntityPreference(
            EntitySituation.NOT_SITTING, EntityAction.SIT
        )
        val defaultPreferenceQuipLikeABoss = EntityPreference(
            EntitySituation.NOT_ALONE, EntityAction.QUIP_TO_RANDOM_ENTITY
        )
        val defaultPreferenceFindRandomWeaponIfNoneEquipped = EntityPreference(
            listOf(
                EntitySituation.NO_EQUIPPED_WEAPON,
                EntitySituation.INVENTORY_OR_CURRENT_ROOM_CONTAINS_WEAPON
            ),
            EntityAction.FIND_AND_EQUIP_RANDOM_WEAPON
        )
        val defaultPreferenceHealFriendly = EntityPreference(
            listOf(
                EntitySituation.INJURED_FRIENDLY_IN_ROOM,
                EntitySituation.CAN_CAST_HEALING_SPELL
            ),
            EntityAction.HEAL_OTHER
        )
        val defaultPreferenceCastDamageSpellAtLivingHostile = EntityPreference(
            listOf(
                EntitySituation.ANY_LIVING_HOSTILES,
                EntitySituation.CAN_CAST_DAMAGE_SPELL
            ),
            EntityAction.CAST_DAMAGE_SPELL_AT_LIVING_HOSTILE
        )
        val defaultPreferenceCastFireAtLivingHostile = EntityPreference(
            listOf(
                EntitySituation.ANY_LIVING_HOSTILES,
                EntitySituation.CAN_CAST_FIRE_DAMAGE_SPELL
            ),
            EntityAction.CAST_FIRE_AT_LIVING_HOSTILE
        )
        val defaultPreferenceSearchDeadHostile = EntityPreference(
            EntitySituation.ANY_UNSEARCHED_DEAD_HOSTILES, EntityAction.SEARCH_RANDOM_UNSEARCHED_DEAD_HOSTILE
        )
        val defaultPreferenceAttackLivingHostile = EntityPreference(
            EntitySituation.ANY_LIVING_HOSTILES, EntityAction.ATTACK_RANDOM_LIVING_HOSTILE
        )
        val defaultPreferenceFindBetterWeapon = EntityPreference(
            EntitySituation.FOUND_BETTER_WEAPON, EntityAction.GET_RANDOM_BETTER_WEAPON
        )
        val defaultPreferenceFindBetterArmor = EntityPreference(
            EntitySituation.FOUND_BETTER_ARMOR, EntityAction.GET_RANDOM_BETTER_ARMOR
        )
        val defaultPreferenceFindValuableItem = EntityPreference(
            EntitySituation.FOUND_VALUABLE_ITEM, EntityAction.GET_VALUABLE_ITEM
        )
        val defaultPreferenceEatFood = EntityPreference(
            EntitySituation.FOUND_FOOD, EntityAction.EAT_RANDOM_FOOD
        )
        val defaultPreferenceDrink = EntityPreference(
            EntitySituation.FOUND_DRINK, EntityAction.DRINK_RANDOM_DRINK
        )
        // endregion

        // region janitor
        val janitorPreferenceGetItem = EntityPreference(
            EntitySituation.FOUND_ANY_ITEM, EntityAction.GET_ANY_ITEM
        )
        val janitorPreferenceDestroyItemInRoom = EntityPreference(
            EntitySituation.FOUND_ANY_ITEM, EntityAction.DESTROY_ANY_ITEM
        )
        // region berserker
        val berserkerPreferenceAlwaysMoving = EntityPreference(
            EntitySituation.ANY, EntityAction.MOVE
        )
        // endregion
    }
}

// move until alone, check for weapon and armor, if nothing better found, just sit
// if in same room as player, shriek and then run away!

// standing up takes time, so it'd be a complex action, STAND then MOVE
// requested action isn't always what's done, but what's always done is one action
// e.g. MOVE won't move if you're sitting, but it'll route to STAND
//  next situation assessment, still not alone, so MOVE again, but takes
//  two rounds of evaluation

//        val behaviorFindEmptyRoomAndHeal = EntityBehaviorTemplate(
//            prereqs = listOf(
//                Situation.INJURED_MAJOR,
//                Situation.ALONE,
//                Situation.SITTING
//            ),
//            // need to verify that action is even possible
//            primaryAction = Action.HEAL_SELF, // consider way to specify amount of healing (e.g. 80%)
//            postreqs = listOf(
//                Situation.STANDING
//            )
//        )
//        Situation.SELF_INJURED_MAJOR to ComplexAction(
//            Action.FIND_EMPTY_ROOM,
//            Action.SIT,
//            Action.HEAL_SELF,
//            Action.STAND
//        )

//        val defaultNpc = EntityBehaviorTemplate(
//            mapOf(
//                behaviorFindEmptyRoomAndHeal
//            )
//        )

// a behavior has a series of sub-behaviors
// a sub-behavior is something like "if injured, find empty room, sit, heal, and stand back up"
// requirements that are periodically checked:
// - still injured?
// - alone?
// - sitting?
// if these are met, next action is to heal with whatever you have
// loop until prereqs are met
// sub-behavior also has post-reqs that need to be met before the sub-behavior has completed
// e.g. stand
// sub-behavior: if healthy, look for shiny things, completed when a shiny thing is found and picked up
// if thirsty, travel to the tavern, order a drink, and drink it
// if fine/normal, wander around
// -
// an action has goals that can be met at any time
// an action can be interrupted by certain other actions

// need to be able to abort current behavior when something more pressing happens
// e.g. when finding empty room and healing, if no longer alone, start over (find another empty room)
// entity has a current behavior and a current action
// need to be able to set next
// actions in a complexaction need to be ordered

// monster
// if there's a desired item on the ground, and the monster wants it, they'll pick it up
// monsters have wants, some like certain materials?
// goblins will highly prioritize taking silver objects that are lying around
// goblins might otherwise dart from room to room looking for interesting things to pick up
// goblins might stop and check the area for safety before picking something up
// might not, though; might be reckless
// if there's

// monster behavior: default
// if at least moderately injured
//    if no one is around, rest and heal
//    else run
// else if hostile is in the room, consider attacking
// else if better weapon or armor is on the ground, consider picking it up and equipping it
// else consider moving around
// else idle emote

// monster behavior: berserker
// doesn't care about being injured
// if hostile is in the room, attack
// doesn't care about weapons or armor
// else consider moving around
// else idle emote

// monster behavior: fearful
// if hostile is in the room, usually run, sometimes attack and run, sometimes attack
// else if better weapon or armor...
// else move/idle

// monster behavior: buddy
// if friendly is in the room, usually stay, occasionally move
// if hostile is in the room
//    if friendly is in the room, usually attack, sometimes run
//    else run
// else prioritize moving over finding better items and/or idling

// monster behavior: pack
// same as buddy but try to group at least 3

// todo: move as pack?
//  e.g. goblin announces to other goblins, move east
//  other goblins then get a planned next move
//  - give entity a nextmove field, can set, but if empty, move randomly; reset value when moving
// todo: if hostile moves, go in that direction (how?)

// situations
// SELF_INJURED_MINOR
// SELF_INJURED_MODERATE
// SELF_INJURED_MAJOR
// ALONE
// NO_NPCS
// SINGLE_NPC
// MULTIPLE_NPCS
// NO_HOSTILES
// SINGLE_HOSTILE
// MULTIPLE_HOSTILES
// FOUND_GOOD_WEAPON
// FOUND_GOOD_ARMOR
// FOUND_GOOD_ITEM (list of favorite items?)
// WITH_OTHER_MONSTER
// WITH_OTHER_MONSTER_SAME_TYPE
// WITH_PACK
// WITH_PACK_SAME_TYPE
// NORMAL
// entity is injured, so this triggers the INJURED scenario

// actions, responses to situations
// a monster will have a prioritized list of situations that it cares about
// each situation will have a prioritized list of responses along with their likelihoods
// each tick, for each situation, if situation exists, choose response with some randomness
// if no situation exists, provide final response

// if i'm injured, i find a way to heal
// SELF_INJURED_MINOR 50 -> 50% chance of triggering, pass injury check
// if i see a good weapon, i pick it up
// FOUND_GOOD_WEAPON 50 -> 50% chance of triggering, pass check for good weapon
// if i'm injured, and if i pass a care check (injured condition is true, 50% chance to pass check), then
// do the complex action of finding an empty room and then sitting and healing and standing

// i keep a list of situations that i care about, ordered or prioritized in some way
// occasionally i check if i'm in any of my situations
// if i am, then the highest priority one determines my next action
// do action until fulfilled, then back to normal/idle behavior