import debug.Debug
import engine.entity.EntityManager
import engine.entity.MonsterTemplates
import engine.item.template.ItemTemplates
import engine.magic.Spells
import engine.world.World
import engine.world.template.ShopTemplates
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import plugins.configureRouting
import plugins.configureSockets

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start()

    init()

    runBlocking {
        World.regions.values.forEach { region ->
            launch { region.entityManager.start(this.coroutineContext) }
        }
    }
}

fun Application.module() {
    configureSockets()
    configureRouting()
}

fun init() {
    loadResources()
    // add debug items, monsters, npcs
    Debug.init()
}

fun loadResources() {
    val c = {}.javaClass

    // no dependencies
    ItemTemplates.load(c)
    EntityManager.load(c)
    Spells.load(c)

    // needs items first
    MonsterTemplates.load(c)
    ShopTemplates.load(c)

    // needs shops first
    World.load(c)
}
