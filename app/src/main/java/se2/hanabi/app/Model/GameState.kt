package se2.hanabi.app.Model

import kotlinx.serialization.Serializable

@Serializable
data class GameState(
    val players: List<Player>,
    val visibleHands: Map<String, List<Card>>,
    val playedCards: Map<Card.Color, Int>,
    val discardPile: List<Card>,
    val hints: Int,
    val strikes: Int,
    val gameOver: Boolean,
    val currentPlayer: String
)

@Serializable
data class Player(
    val name: String
)

@Serializable
data class Card(
    val color: Color,
    val value: Int
) {

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