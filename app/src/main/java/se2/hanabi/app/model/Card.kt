package se2.hanabi.app.model

import kotlinx.serialization.Serializable

/**
 * Represents a single Hanabi Card
 */

@Serializable
data class Card(
    private val color: Color,
    private val value: Int,
    private val id: Int = 0
) {

    fun getColor(): Color {
        return color
    }

    fun getValue(): Int {
        return value
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