package engine.entity.core

import engine.Inventory
import engine.entity.attributes.EntityAttributes
import engine.entity.attributes.EntityClass
import engine.utility.Common
import engine.entity.behavior.EntityBehavior
import engine.entity.template.EntityMonsterTemplate
import engine.game.Game
import engine.item.ItemWeapon
import engine.item.template.ItemTemplates
import engine.magic.Spells
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

                if(Game.running) {
                    Game.delay(2000)
                }
                if (Game.running) {
                    addNpcs(this)
                    Game.delay(2000)
                }
                if (Game.running) {
                    addJanitors(this)
                    Game.delay(2000)
                }
                if (Game.running) {
                    addHealers(this)
                    Game.delay(2000)
                }
                if (Game.running) {
                    addWizards(this)
                    Game.delay(2000)
                }
                if (Game.running) {
                    addFarmers(this)
                    Game.delay(2000)
                }
                if (Game.running) {
                    launch { removeSearchedEntities(allMonsters) }
                    Game.delay(2000)
                }
                if (Game.running) {
                    launch { removeSearchedEntities(allNpcs) }
                    Game.delay(2000)
                }
                if (Game.running) {
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

    private fun createDefaultNpc() =
        EntityFriendlyNpc(
            name = allNpcNames.random(),
            level = 1,
            entityClass = EntityClass.PEASANT,
            behavior = EntityBehavior.defaultNpc,
            inventory = Inventory.defaultNpc()
        )

    private fun createHealer() =
        EntityFriendlyNpc(
            name = allNpcNames.random(),
            level = 1,
            entityClass = EntityClass.HEALER,
            behavior = EntityBehavior.healer,
            spells = Spells.healer
        )

    private fun createWizard() =
        EntityFriendlyNpc(
            name = allNpcNames.random(),
            level = 1,
            entityClass = EntityClass.WIZARD,
            behavior = EntityBehavior.wizard,
            spells = Spells.wizard
        )

    private fun createJanitor() =
        EntityFriendlyNpc(
            name = allNpcNames.random(),
            level = 1,
            entityClass = EntityClass.JANITOR,
            behavior = EntityBehavior.janitor,
            arriveSuffix = "arrives, broom in hand",
            delayMin = 500,
            delayMax = 1000,
            weapon = ItemTemplates.createItemFromString("broom") as ItemWeapon
        )

    private fun createFarmer() =
        EntityFriendlyNpc(
            name = allNpcNames.random(),
            level = 1,
            entityClass = EntityClass.FARMER,
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
            entityClass = EntityClass.BERSERKER,
            behavior = EntityBehavior.berserker,
            arriveSuffix = "storms in",
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