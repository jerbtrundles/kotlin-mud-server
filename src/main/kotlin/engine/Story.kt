package engine

import engine.entity.core.EntityBase
import engine.game.Game
import engine.player.Player

class Story(
    val parts: List<String>,
    val partCooldown: Long = 3000L,
    val storyCooldown: Long = 15000L
) {
    private var index = 0

    suspend fun play(player: Player, speaker: EntityBase) {
        while(!isDone()) {
            playNext(player, speaker)
            Game.delay(partCooldown)
        }
    }

    private fun playNext(player: Player, speaker: EntityBase) {
        if (!isDone()) {
            player.sendToMe("${speaker.names.story} says \"${parts[index++]}\"")
        }
    }

    private fun isDone() = index == parts.count()

    companion object {
        val default = Story(
            listOf(
                "Now this is a story all about how.",
                "My life got flipped turned upside down.",
                "And I'd like to take a minute, just sit right there.",
                "I'll tell you how I became the prince of a town called Bel-Air."
            )
        )
    }
}