package se2.hanabi.app.gamePlayUI

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import se2.hanabi.app.card.Card
import kotlin.random.Random

class GamePlayViewModel: ViewModel() {

    // game state info
    val testHand1 = listOf(Card("red",1),Card("red",2),Card("red",3),Card("red",4),)
    private val _hands = MutableStateFlow(listOf(testHand1,testHand1,testHand1,testHand1,testHand1,))
    val hands: MutableStateFlow<List<List<Card>>> = _hands

    private val _stackValues = mutableStateListOf(0,2,3,4,5)
    val stackValues:  List<Int> = _stackValues

    private val _numRemainingCard = MutableStateFlow(Random.nextInt(35))
    val numRemainingCard: MutableStateFlow<Int> = _numRemainingCard

    private val _lastDiscardedCard = MutableStateFlow<Card?>(randomCard())
    val lastDiscardedCard: MutableStateFlow<Card?> = _lastDiscardedCard

    private val _numRemainingHintTokens = MutableStateFlow(Random.nextInt(9))
    val numRemainingHintTokens: MutableStateFlow<Int> = _numRemainingHintTokens

    private val _numRemainingFuzeTokens = MutableStateFlow(Random.nextInt(4))
    val numRemainingFuzeTokens: MutableStateFlow<Int> = _numRemainingFuzeTokens

    // game play info
    private val _selectedCard = MutableStateFlow<Card?>(null)
    val selectedCard: MutableStateFlow<Card?> = _selectedCard

    private val _selectedHandIndex = MutableStateFlow(-1)
    val selectedHandIndex: MutableStateFlow<Int> = _selectedHandIndex

    private val _selectedHint = MutableStateFlow("")
    val selectedHint: MutableStateFlow<String> = _selectedHint


    fun onPlayersCardClick(card: Card?) {
        _selectedHandIndex.value = -1
        _selectedHint.value = ""
         _selectedCard.value = if (card == selectedCard.value) null else card
    }

    fun onOtherPlayersHandClick(handIndex: Int) {
        _selectedCard.value = null
        _selectedHint.value = ""
        _selectedHandIndex.value = if (handIndex == selectedHandIndex.value) -1 else handIndex
    }

    fun onHintClick(hint: String) {
        _selectedHint.value = if (hint == selectedHint.value) "" else hint
    }

    fun onColorStackClick(color: String) {
        _selectedCard.value?.let { card ->
            val colorIndex = colors.indexOf(color)
            if (card.color == color && card.number == _stackValues[colorIndex] + 1) {
                _stackValues[colorIndex]++
                _selectedCard.value = null
            }
        }
        // more logic: discard selected card, draw next one
    }

}

// functions to produce game state for demo/development
fun generateTestColorStackValues(): IntArray {
    var values = IntArray(5)
    for (i in colors.indices) {
        values[i] = Random.nextInt(6)
    }
    return values
}

fun randomCard(): Card {
    return Card(colors[Random.nextInt(colors.size)],Random.nextInt(5)+1)
}

fun generateTestHands(numPlayers: Int): List<List<Card>> {
    val numPlayers = numPlayers.coerceIn(2,5)
    val hands = mutableListOf<List<Card>>()
    val handSize = if (numPlayers<=3) 5 else 4

    for (i in 0 until numPlayers) {
        hands.add(randomHand(handSize))
    }
    return hands
}

fun randomHand(numCards: Int): List<Card> {

    val hand = mutableListOf<Card>()
    for (i in 0 until numCards) {
        hand.add( Card( colors[Random.nextInt(colors.size)], Random.nextInt(4)+1))
    }
    return hand
}