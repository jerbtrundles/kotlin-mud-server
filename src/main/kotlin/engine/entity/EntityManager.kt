package engine.entity

import debug.Debug
import engine.Inventory
import engine.utility.Common
import engine.entity.behavior.EntityBehavior
import engine.game.Game
import kotlinx.coroutines.launch
import engine.world.World
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import kotlin.coroutines.coroutineContext
import kotlin.random.Random

object EntityManager {
    private var allNpcNames = listOf<String>()
    private var allNpcJobs = listOf<String>()

    fun load(c: Class<() -> Unit>) {
        allNpcNames = Common.parseArrayFromJson(c, "/names.json")
        allNpcJobs = Common.parseArrayFromJson(c, "/jobs.json")
    }

    suspend fun start() {
        val allMonsters = mutableListOf<EntityBase>()
        val allNpcs = mutableListOf<EntityBase>()
        val allJanitors = mutableListOf<EntityBase>()

        withContext(coroutineContext) {
            while (Game.running) {
                addMonsters(allMonsters, this)
                Game.delay(2000)
                addNpcs(allNpcs, this)
                Game.delay(2000)
                addJanitors(allJanitors, this)
                Game.delay(2000)
                launch { removeSearchedEntities(allMonsters) }
                Game.delay(2000)
                launch { removeSearchedEntities(allNpcs) }
                Game.delay(2000)
                launch { removeSearchedEntities(allJanitors) }
                Game.delay(2000)
            }
        }
    }

    // region create entities
    private fun createRandomNpc() =
        when (Random.nextInt(100)) {
            in 0..1 -> createBerserker()
            else -> createDefaultNpc()
        }

    private fun createDefaultNpc() = EntityFriendlyNpc(
        name = allNpcNames.random(),
        level = 1,
        job = allNpcJobs.random(),
        behavior = EntityBehavior.defaultNpc,
        inventory = Inventory.createWithRandomStuff()
    )

    private fun createJanitor() = EntityFriendlyNpc(
        name = allNpcNames.random(),
        level = 1,
        job = "janitor",
        behavior = EntityBehavior.janitor,
        arriveStringSuffix = "arrives, broom in hand",
        delayMin = 500,
        delayMax = 1000
    )

    private fun createBerserker() = EntityFriendlyNpc(
        name = allNpcNames.random(),
        level = 1,
        job = "berserker",
        behavior = EntityBehavior.berserker,
        arriveStringSuffix = "storms in",
        attributes = EntityAttributes.defaultBerserker
    )

    private fun createRandomMonster() = EntityTemplates
        .monsterTemplates
        .random()
        .create(inventory = Inventory.createWithRandomStuff())
    // endregion

    // region remove entities
    private fun removeSearchedEntities(allEntities: MutableList<EntityBase>) {
        // remove searched dead
        allEntities.removeAll(
            allEntities.filter { !it.hasNotBeenSearched }
        )
    }
    // endregion

    // region add entities
    suspend fun addMonsters(allMonsters: MutableList<EntityBase>, scope: CoroutineScope) =
        addEntities(allMonsters, Debug.maxMonsters, scope, ::createRandomMonster)

    suspend fun addNpcs(allNpcs: MutableList<EntityBase>, scope: CoroutineScope) =
        addEntities(allNpcs, Debug.maxNpcs, scope, ::createRandomNpc)

    suspend fun addJanitors(allJanitors: MutableList<EntityBase>, scope: CoroutineScope) =
        addEntities(allJanitors, Debug.maxJanitors, scope, ::createJanitor)

    suspend fun addEntities(
        allEntities: MutableList<EntityBase>,
        max: Int,
        scope: CoroutineScope,
        createEntity: () -> EntityBase
    ) {
        while (allEntities.size < max) {
            val entity = createEntity()
            allEntities.add(entity)
            scope.launch {
                entity.goLiveYourLifeAndBeFree(initialRoom = World.getRandomRoom())
            }
        }
    }
    // endregion
}