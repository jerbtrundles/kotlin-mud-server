package connection

import connection.plugins.configureRouting
import connection.plugins.configureSockets
import debug.Debug
import engine.entity.EntityManager
import engine.entity.EntityTemplates
import engine.item.template.ItemTemplates
import engine.world.World
import engine.world.template.ShopTemplates
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start()

    init()

    runBlocking {
        launch { EntityManager.start() }
        launch { pingConnections() }
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
    // load items first; no other dependencies
    ItemTemplates.load(c)
    // load npc names and jobs next; no other dependencies
    EntityManager.load(c)
    // load entities next; depends on items
    EntityTemplates.load(c)
    // load shops next; depends on items
    ShopTemplates.load(c)
    // load world next; depends on shops
    World.load(c)
}

suspend fun pingConnections() {
    while(true) {
        delay(5000)
        ConnectionManager.webSocketSessions.forEach { connection ->
            // connection.send("ping!")
        }
    }
}