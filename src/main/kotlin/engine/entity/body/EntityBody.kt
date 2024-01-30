package engine.entity.body

class EntityBody(val parts: Array<EntityBodyPart> = emptyArray()) {
    companion object {
        fun humanoid() = EntityBody(
            parts = arrayOf(
                EntityBodyPart.humanoidChest(),
                EntityBodyPart.humanoidHands(),
                EntityBodyPart.humanoidFeet(),
                EntityBodyPart.humanoidArms(),
                EntityBodyPart.humanoidLegs(),
                EntityBodyPart.humanoidHead()
            )
        )

        fun critter() = EntityBody()
    }
}