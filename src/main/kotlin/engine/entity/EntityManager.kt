package engine.entity

import engine.Inventory
import engine.utility.Common
import engine.entity.behavior.EntityBehavior
import engine.game.Game
import engine.item.ItemWeapon
import engine.item.template.ItemTemplates
import engine.magic.Spell
import engine.world.Region
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext
import kotlin.random.Random

class EntityManager(
    val region: Region,
    val maxMonsters: Int,
    val maxNpcs: Int,
    val maxJanitors: Int,
    val maxHealers: Int,
    val maxWizards: Int,
    val maxFarmers: Int,
    val monsterTemplates: List<EntityMonsterTemplate>
) {
    companion object {
        private var allNpcNames = listOf<String>()
        private var allNpcJobs = listOf<String>()

        fun load(c: Class<() -> Unit>) {
            allNpcNames = Common.parseArrayFromJson(c, "/names.json")
            allNpcJobs = Common.parseArrayFromJson(c, "/jobs.json")
        }
    }

    val allMonsters = mutableListOf<EntityBase>()
    val allNpcs = mutableListOf<EntityBase>()
    val allJanitors = mutableListOf<EntityBase>()
    val allHealers = mutableListOf<EntityBase>()
    val allWizards = mutableListOf<EntityBase>()
    val allFarmers = mutableListOf<EntityBase>()

    suspend fun start(context: CoroutineContext) =
        withContext(context) {
            while (Game.running) {
                addMonsters(this)
                Game.delay(2000)
                addNpcs(this)
                Game.delay(2000)
                addJanitors(this)
                Game.delay(2000)
                addHealers(this)
                Game.delay(2000)
                addWizards(this)
                Game.delay(2000)
                addFarmers(this)
                Game.delay(2000)
                launch { removeSearchedEntities(allMonsters) }
                Game.delay(2000)
                launch { removeSearchedEntities(allNpcs) }
                Game.delay(2000)
                launch { removeSearchedEntities(allJanitors) }
                Game.delay(2000)
            }
        }

    // region create entities
    private fun createRandomNpc() =
        when (Random.nextInt(100)) {
            in 0..1 -> createBerserker()
            else -> createDefaultNpc()
        }

    private fun createDefaultNpc() =
        EntityFriendlyNpc(
            name = allNpcNames.random(),
            level = 1,
            job = allNpcJobs.random(),
            behavior = EntityBehavior.defaultNpc,
            inventory = Inventory.defaultNpc()
        )

    private fun createHealer() =
        EntityFriendlyNpc(
            name = allNpcNames.random(),
            level = 1,
            job = "healer",
            behavior = EntityBehavior.healer,
            spells = mutableListOf(
                "minor heal"
            )
        )

    private fun createWizard() =
        EntityFriendlyNpc(
            name = allNpcNames.random(),
            level = 1,
            job = "wizard",
            behavior = EntityBehavior.wizard,
            spells = mutableListOf(
                "minor fire",
                "minor ice",
                "minor bolt"
            )
        )

    private fun createJanitor() =
        EntityFriendlyNpc(
            name = allNpcNames.random(),
            level = 1,
            job = "janitor",
            behavior = EntityBehavior.janitor,
            arriveStringSuffix = "arrives, broom in hand",
            delayMin = 500,
            delayMax = 1000,
            weapon = ItemTemplates.createItemFromString("broom") as ItemWeapon
        )

    private fun createFarmer() =
        EntityFriendlyNpc(
            name = allNpcNames.random(),
            level = 1,
            job = "farmer",
            behavior = EntityBehavior.farmer,
            delayMin = 500,
            delayMax = 1000,
            canTravelBetweenRegions = false,
            weapon = ItemTemplates.createItemFromString("pitchfork") as ItemWeapon
        )

    private fun createBerserker() =
        EntityFriendlyNpc(
            name = allNpcNames.random(),
            level = 1,
            job = "berserker",
            behavior = EntityBehavior.berserker,
            arriveStringSuffix = "storms in",
            attributes = EntityAttributes.defaultBerserker
        )

    private fun createRandomMonster() = monsterTemplates.random().create()
    // endregion

    // region remove entities
    private fun removeSearchedEntities(allEntities: MutableList<EntityBase>) =
        // remove searched dead
        allEntities.removeAll(
            allEntities.filter { !it.hasNotBeenSearched }
        )
    // endregion

    // region add entities
    private suspend fun addMonsters(scope: CoroutineScope) =
        addEntities(allMonsters, maxMonsters, scope, ::createRandomMonster)


    private suspend fun addNpcs(scope: CoroutineScope) =
        addEntities(allNpcs, maxNpcs, scope, ::createRandomNpc)

    private suspend fun addJanitors(scope: CoroutineScope) =
        addEntities(allJanitors, maxJanitors, scope, ::createJanitor)

    private suspend fun addHealers(scope: CoroutineScope) =
        addEntities(allHealers, maxHealers, scope, ::createHealer)
    private suspend fun addWizards(scope: CoroutineScope) =
        addEntities(allWizards, maxWizards, scope, ::createWizard)
    private suspend fun addFarmers(scope: CoroutineScope) =
        addEntities(allFarmers, maxFarmers, scope, ::createFarmer)

    private suspend fun addEntities(
        allEntities: MutableList<EntityBase>,
        max: Int,
        scope: CoroutineScope,
        createEntity: () -> EntityBase
    ) = repeat(max - allEntities.size) {
        with(createEntity()) {
            allEntities.add(this)
            scope.launch {
                goLiveYourLifeAndBeFree(initialRoom = region.randomRoom)
            }
            scope.launch {
                naturalHealthRestoration()
            }
            scope.launch {
                naturalMagicRestoration()
            }
        }
    }
    // endregion
}