package se2.hanabi.app.model

import kotlinx.serialization.Serializable

@Serializable
data class GameStatus(
    val players: List<Player>,
    val playerCardIds: List<Int>,
    val visibleHands: Map<Int, List<Card>>,
    val playedCards: Map<Card.Color, Int>,
    val discardPile: List<Card>,
    val numRemainingCards: Int,
    val shownHints: Map<Int, Hint>,
    val numRemainingHintTokens: Int,
    val strikes: Int,
    val gameOver: Boolean,
    val currentPlayer: Int
) {

}