package se2.hanabi.app.gamePlayUI

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import se2.hanabi.app.card.Card
import kotlin.random.Random

class GamePlayViewModel: ViewModel() {

    // game state info
    private val _hands = MutableStateFlow(generateTestHands(5))
    val hands: MutableStateFlow<List<List<Card>>> = _hands

    private val _stackValues = generateTestColorStackValues()
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

    private val _isValidHint = MutableStateFlow(false)
    val isValidHint: MutableStateFlow<Boolean> = _isValidHint

    fun onPlayersCardClick(card: Card?) {
        _selectedHandIndex.value = -1
        hintReset()
         _selectedCard.value = if (card == selectedCard.value) null else card
    }

    fun onOtherPlayersHandClick(handIndex: Int) {
        _selectedCard.value = null
        hintReset()
        _selectedHandIndex.value = if (handIndex == selectedHandIndex.value) -1 else handIndex
    }

    fun onHintClick(hint: String) {
        _selectedHint.value = if (hint == selectedHint.value) "" else hint
        if (_selectedHint.value != "") {
            var validHint = false;
            _hands.value[selectedHandIndex.value+1].forEach() { card -> // issue with which hand is selected
                if (card.color == _selectedHint.value || card.number.toString() == _selectedHint.value) {
                    validHint = true
                }
            }
            _isValidHint.value = validHint
        } else {
            _isValidHint.value = false
        }

    }

    fun onColorStackClick(color: String) {
        //TODO send place card request to server
        _selectedCard.value?.let { card ->
            val colorIndex = colors.indexOf(color)
            if (card.color == color && card.number == _stackValues[colorIndex] + 1) {
                _stackValues[colorIndex]++
                _selectedCard.value = null
            }
        }
        // more logic: discard selected card, draw next one
    }

    fun onGiveHintClick() {
        if (isValidHint.value) {
            //TODO send hint to server
            reset()
        }
    }

    //
    private fun reset() {
        _selectedCard.value = null
        _selectedHandIndex.value = -1
        hintReset()
    }

    private fun hintReset() {
        _selectedHint.value = ""
        _isValidHint.value = false
    }

}

// functions to produce game state for demo/development
fun generateTestColorStackValues(): SnapshotStateList<Int> {
    return mutableStateListOf(Random.nextInt(6),Random.nextInt(6),Random.nextInt(6),Random.nextInt(6),Random.nextInt(6),)
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