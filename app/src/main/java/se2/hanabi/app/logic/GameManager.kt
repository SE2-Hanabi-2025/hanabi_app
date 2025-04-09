package se2.hanabi.app.logic

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class GameManager {

    var deckSize by mutableStateOf(50)
    var selectedCardIndex by mutableStateOf<Int?>(null)
    var lastDrawnMessage by mutableStateOf("No card drawn yet")
    var lastDiscardedMessage by mutableStateOf("No card discarded yet")
    var drawnCards = mutableStateListOf<String>()
    var boxes = mutableStateListOf<String?>(null, null, null, null, null)

    fun drawCard(cardValue: String): Boolean {
        if (deckSize == 0) {
            lastDrawnMessage = "No more cards in the deck!"
            return false
        }

        drawnCards.add(cardValue)
        deckSize--
        lastDrawnMessage = "Drew a card: $cardValue"
        return true
    }

    fun discardSelectedCard(): Boolean {
        return if (selectedCardIndex != null) {
            drawnCards.removeAt(selectedCardIndex!!)
            lastDiscardedMessage = "Discarded card at index $selectedCardIndex"
            selectedCardIndex = null
            true
        } else {
            lastDiscardedMessage = "No card selected to discard"
            false
        }
    }

    fun removeCardByValue(cardValue: String): Boolean {
        return drawnCards.remove(cardValue)
    }

    fun selectCard(index: Int) {
        selectedCardIndex = if (selectedCardIndex == index) null else index
    }

    fun getSelectedCard(): String? {
        return selectedCardIndex?.let { drawnCards[it] }
    }

    fun placeCardInBox(boxIndex: Int): PlaceCardResult {
        val selectedCard = getSelectedCard()
        val cardValue = selectedCard?.toIntOrNull()
        val boxValue = boxes[boxIndex]?.toIntOrNull()

        if (cardValue == null) return PlaceCardResult.InvalidSelection

        if (boxValue == 5) return PlaceCardResult.StackFull

        val canPlace = when (boxValue) {
            null -> cardValue == 1
            else -> cardValue == boxValue + 1
        }

        return if (canPlace) {
            boxes[boxIndex] = cardValue.toString()
            drawnCards.removeAt(selectedCardIndex!!)
            selectedCardIndex = null
            if (cardValue == 5) {
                PlaceCardResult.StackCompleted
            } else if (boxes.all { it == "5" }) {
                PlaceCardResult.GameWon
            } else {
                PlaceCardResult.Valid
            }
        } else {
            PlaceCardResult.InvalidMove(expected = boxValue?.plus(1) ?: 1)
        }
    }

}