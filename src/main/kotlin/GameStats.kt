import connection.ConnectionManager

object GameStats {
    val monstersKilled = mutableMapOf<String, Int>()
    var numNpcsKilled = 0
    var totalMonstersKilled = 0

    val statsString: String
        get() {
            with(StringBuilder()) {
                append("STATS:NPCs killed: $numNpcsKilled\nMonsters killed: $totalMonstersKilled\n")
                monstersKilled.forEach { (name, count) ->
                    appendLine("$name: $count")
                }
                return toString()
            }
        }

    fun sendStatsToPlayers() = ConnectionManager.sendToAll(statsString)
}