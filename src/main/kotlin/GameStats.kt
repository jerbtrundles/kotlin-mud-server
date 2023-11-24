import connection.ConnectionManager
import engine.Messages

object GameStats {
    var numNpcsKilled = 0
    var numMonstersKilled = 0

    val statsString
        get() = "STATS:NPCs killed: $numNpcsKilled\nMonsters killed: $numMonstersKilled"

    fun sendStatsToPlayers() = ConnectionManager.sendToAll(statsString)
}