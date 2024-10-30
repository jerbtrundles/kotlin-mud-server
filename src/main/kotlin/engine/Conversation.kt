package engine

import engine.entity.core.EntityBase
import engine.entity.core.EntityFriendlyNpc
import engine.world.Room

class Conversation(
    firstParticipant: EntityBase,
    secondParticipant: EntityBase,
    lines: List<String>
) {
    companion object {
        private const val PROMPT = """Create a 5-line conversation between these two village characters: 
            first and second. 
            Choose a random topic (e.g., weather, profession, shop feedback). 
            Use the format: Name says "Dialogue." 
            Output only the conversation, nothing else. Keep it under 300 characters."""

        private fun getPrompt(firstParticipant: EntityBase, secondParticipant: EntityBase) =
            PROMPT.replace("first", firstParticipant.names.full)
                .replace("second", secondParticipant.names.full)
    }


    fun createFromRoom(room: Room, conversationStarter: EntityBase) =
        room.getRandomFriendlyOrNull(conversationStarter)?.let { secondParticipant ->
            Conversation(conversationStarter, secondParticipant, listOf())
        }
}