package engine.entity.attributes

import com.beust.klaxon.Json
import kotlin.math.max

class EntityAttributes(
    var strength: Int = 20,
    var intelligence: Int = 20,
    var vitality: Int = 20,
    var speed: Int = 20,
    @Json(name = "defense")
    var baseDefense: Int = 20,
    @Json(name = "health")
    var maximumHealth: Int = 20,
    @Json(name = "magic")
    var maximumMagic: Int = 20,
) {
    // TODO: derive from stats
    val naturalHealthRestorationRate = 1
    val naturalMagicRestorationRate = 1

    var currentHealth = max(maximumHealth - 10, 1)
        set(value) {
            field = if (value > maximumHealth) {
                maximumHealth
            } else if (value < 0) {
                0
            } else {
                value
            }
        }

    var currentMagic = max(maximumMagic - 3, 0)
        set(value) {
            field = if (value > maximumMagic) {
                maximumMagic
            } else if (value < 0) {
                0
            } else {
                value
            }
        }

    val healthString
        get() = "Health: $currentHealth/$maximumHealth"
    val magicString
        get() = "Magic: $currentMagic/$maximumMagic"

    fun isInjuredMinor() = (currentHealth.toDouble() / maximumHealth) < 0.9
    fun isInjuredModerate() = (currentHealth.toDouble() / maximumHealth) < 0.6
    fun isInjuredMajor() = (currentHealth.toDouble() / maximumHealth) < 0.3

    companion object {
        val defaultNpc
            get() = EntityAttributes(
                strength = 5,
                intelligence = 5,
                vitality = 5,
                speed = 5,
                baseDefense = 5,
                maximumHealth = 30,
                maximumMagic = 10
            )
        val player
            get() = EntityAttributes(
                strength = 50,
                intelligence = 50,
                vitality = 50,
                speed = 50,
                baseDefense = 100,
                maximumHealth = 500,
                maximumMagic = 500
            )
        val defaultBerserker
            get() = EntityAttributes(
                strength = 50,
                intelligence = 2,
                vitality = 50,
                speed = 10,
                baseDefense = 30,
                maximumHealth = 200,
                maximumMagic = 0
            )

        val defaultCritter
            get() = EntityAttributes(1, 1, 1, 1, 1, 1, 1)
    }
}