package se2.hanabi.app.Model

import kotlinx.serialization.Serializable

@Serializable
data class GameStatus(
    val players: List<Player>,
    val playersHand: List<Int>,
    val visibleHands: Map<Int, List<Card>>,
    val playedCards: Map<Card.Color, Int>,
    val discardPile: List<Card>,
    val numRemainingCard: Int,
    val shownHints: Map<Int, Hint>,
    val hintTokens: Int,
    val strikes: Int,
    val gameOver: Boolean,
    val currentPlayer: Int
) {

}