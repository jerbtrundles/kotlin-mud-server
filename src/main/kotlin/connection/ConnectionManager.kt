package connection

import io.ktor.websocket.*
import kotlinx.coroutines.runBlocking

object ConnectionManager {
    val webSocketSessions = mutableListOf<DefaultWebSocketSession>()

    fun sendToAll(what: String) = runBlocking {
        webSocketSessions.forEach { it.send(what) }
    }
}