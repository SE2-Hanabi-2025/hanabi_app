package se2.hanabi.app.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

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