package engine.player

import engine.entity.core.EntityBase
import engine.item.ItemBase
import engine.item.template.ItemTemplates
import engine.quest.*
import engine.world.Room
import engine.world.World
import engine.world.WorldCoordinates

class PlayerQuestManager {
    val activeQuests: MutableList<Quest> = mutableListOf()
    val completedQuests: MutableList<Quest> = mutableListOf()

    private val killQuests
        get() = activeQuests.filterIsInstance<KillQuest>()
    private val exploreQuests
        get() = activeQuests.filterIsInstance<ExploreQuest>()

    private val fetchQuests
        get() = activeQuests.filterIsInstance<FetchQuest>()

    private val deliveryQuests
        get() = activeQuests.filterIsInstance<DeliveryQuest>()

    fun completeQuest(quest: Quest) {
        // message completion

        activeQuests.remove(quest)
        completedQuests.add(quest)
    }

    fun processKill(entity: EntityBase) {
        killQuests.forEach { killQuest ->
            killQuest.recordKill(entity)
            if (killQuest.isCompleted()) {
                completeQuest(killQuest)
            }
        }
    }


    fun processLocation(coordinates: WorldCoordinates) {
        exploreQuests.forEach { exploreQuest ->
            exploreQuest.visitLocation(coordinates)
            if (exploreQuest.isCompleted()) {
                completeQuest(exploreQuest)
            }
        }
    }

    // player picks up an item (i.e. places it in inventory)
    //  
    fun processFetchItem(item: ItemBase) {
        fetchQuests.firstOrNull { fetchQuest ->
            fetchQuest.needs(item)
        }
    }

    fun checkDeliveryPotential(item: ItemBase, room: Room) {
        deliveryQuests.forEach { deliveryQuest ->
            deliveryQuest.attemptDelivery(item, room)
            if(deliveryQuest.isCompleted()) {
                completeQuest(deliveryQuest)
            }
        }
    }
}
