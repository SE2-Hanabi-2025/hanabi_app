package se2.hanabi.app.Model

import kotlinx.serialization.Serializable

@Serializable
data class GameStatus(
    val players: List<Player>,
    val visibleHands: Map<Int, List<Card>>,
    val playedCards: Map<Card.Color, Int>,
    val discardPile: List<Card>,
    val hints: Int,
    val strikes: Int,
    val gameOver: Boolean,
    val currentPlayer: Int
)

@Serializable
data class Player(
    val name: String,
    private val id: Int
)