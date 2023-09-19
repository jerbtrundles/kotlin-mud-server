package connection.plugins

import connection.ConnectionManager
import engine.player.Player
import engine.world.World
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import java.time.Duration

fun Application.configureSockets() {
    install(WebSockets) {
        pingPeriod = Duration.ofSeconds(15)
        timeout = Duration.ofSeconds(15)
        maxFrameSize = Long.MAX_VALUE
        masking = false
    }
    routing {
        webSocket("/ws") { // websocketSession
            ConnectionManager.webSocketSessions.add(this)

            val player = Player("Namey Name", this)
            World.zero.addPlayer(player)
            player.onInput("look")

            for (frame in incoming) {
                if (frame is Frame.Text) {
                    val text = frame.readText()

                    player.onInput(text)

                    // TODO: close condition
                    if (text.lowercase() == "quit") {
                        this.close(
                            CloseReason(
                                CloseReason.Codes.NORMAL, "client said quit"
                            )
                        )
                    }
                }
            }

            ConnectionManager.webSocketSessions.remove(this)
        }
    }
}
