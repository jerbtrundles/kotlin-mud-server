package connection

import io.ktor.websocket.*

object ConnectionManager {
    val webSocketSessions = mutableListOf<DefaultWebSocketSession>()
}