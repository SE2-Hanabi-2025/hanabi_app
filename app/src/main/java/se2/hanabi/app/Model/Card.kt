package se2.hanabi.app.Model

import kotlinx.serialization.Serializable

/**
 * Represents a single Hanabi Card
 */

@Serializable
data class Card(
    val color: Color,
    val value: Int,
    // ids will eventuelly come from back end.
    private val id: Int = nextID++
) {
    companion object {
        private var nextID = 0
    }

    fun getID():Int {
        return id
    }

    override fun toString(): String {
        return "Card{value=$value, color=$color}"
    }

    @Serializable
    enum class Color {
        RED,
        BLUE,
        GREEN,
        YELLOW,
        WHITE
    }
}