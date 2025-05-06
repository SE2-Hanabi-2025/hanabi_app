package se2.hanabi.app.gamePlayUI

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import se2.hanabi.app.Model.GameStatus
import se2.hanabi.app.Model.Player
import se2.hanabi.app.Services.GamePlayService
import se2.hanabi.app.card.Card
import kotlin.random.Random

/**
 * GamePlayViewModel stores the current game state and handles event such as button clicks.
 *
 */
class GamePlayViewModel: ViewModel() {
    private val gamePlayService: GamePlayService = GamePlayService()
    private val gameStatus: GameStatus = generateTestGameStatus()

    // game state info
    private val _numPlayers = MutableStateFlow(5)
    val numPlayers: MutableStateFlow<Int> = _numPlayers

    private val _thisPlayerId = MutableStateFlow(Random.nextInt(numPlayers.value))
    val thisPlayerId: MutableStateFlow<Int> = _thisPlayerId

    private val _hands = MutableStateFlow(generateTestHands(numPlayers.value))
    val hands: MutableStateFlow<List<List<Card>>> = _hands

    private val _stackValues = generateTestColorStackValues()
    val stackValues:  List<Int> = _stackValues

    private val _numRemainingCard = MutableStateFlow(Random.nextInt(35))
    val numRemainingCard: MutableStateFlow<Int> = _numRemainingCard

    private val _lastDiscardedCard = MutableStateFlow<Card?>(randomCard())
    val lastDiscardedCard: MutableStateFlow<Card?> = _lastDiscardedCard

    private val _numRemainingHintTokens = MutableStateFlow(Random.nextInt(9))
    val numRemainingHintTokens: MutableStateFlow<Int> = _numRemainingHintTokens

    private val _numRemainingFuseTokens = MutableStateFlow(Random.nextInt(4))
    val numRemainingFuseTokens: MutableStateFlow<Int> = _numRemainingFuseTokens

    // game play info
    private val _selectedCard = MutableStateFlow<Card?>(null)
    val selectedCard: MutableStateFlow<Card?> = _selectedCard

    private val _selectedHandIndex = MutableStateFlow(-1)
    val selectedHandIndex: MutableStateFlow<Int> = _selectedHandIndex

    private val _selectedHint = MutableStateFlow("")
    val selectedHint: MutableStateFlow<String> = _selectedHint

    private val _isValidHint = MutableStateFlow(false)
    val isValidHint: MutableStateFlow<Boolean> = _isValidHint

    private val _shownColorHints = SnapshotStateList<Int>()
    val shownColorHints: List<Int> = _shownColorHints
    private val _shownValueHints = SnapshotStateList<Int>()
    val shownValueHints: List<Int> = _shownValueHints

    fun onPlayersCardClick(card: Card) {
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
            _hands.value[selectedHandIndex.value].forEach() { card -> // issue with which hand is selected
                if (card.color == _selectedHint.value || card.number.toString() == _selectedHint.value) {
                    validHint = true
                }
            }
            _isValidHint.value = validHint
        } else {
            _isValidHint.value = false
        }
    }

    fun onGiveHintClick() {
        if (isValidHint.value) {
            //TODO send hint to server and recieve update to shownHints
            _hands.value[_selectedHandIndex.value].forEach() {card ->
                if (card.color == _selectedHint.value) {
                    _shownColorHints.add(card.getID())
                } else if (card.number.toString() == _selectedHint.value) {
                    _shownValueHints.add(card.getID())
                }
            }
            resetSelection()
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

    fun onDiscardStackClick() {
        //TODO send that thisPlayer wants to discard SelectedCard
        viewModelScope.launch {
            gamePlayService.updateGameStatus()
        }
    }

    //
    private fun resetSelection() {
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

// Test game status
fun generateTestGameStatus(): GameStatus {
    val player1 = Player(name = "Alice", id = 0)
    val player2 = Player(name = "Bob", id = 1)
    val player3 = Player(name = "Cat", id = 2)
    val players = listOf(player1, player2, player3)

    val hand1 = listOf(
        se2.hanabi.app.Model.Card(color = se2.hanabi.app.Model.Card.Color.RED, value = 1),
        se2.hanabi.app.Model.Card(color = se2.hanabi.app.Model.Card.Color.BLUE, value = 3),
        se2.hanabi.app.Model.Card(color = se2.hanabi.app.Model.Card.Color.GREEN, value = 2)
    )
    val hand2 = listOf(
        se2.hanabi.app.Model.Card(color = se2.hanabi.app.Model.Card.Color.YELLOW, value = 1),
        se2.hanabi.app.Model.Card(color = se2.hanabi.app.Model.Card.Color.WHITE, value = 5),
        se2.hanabi.app.Model.Card(color = se2.hanabi.app.Model.Card.Color.RED, value = 2)
    )
    // cat's hand is not visable
    val visibleHands = mapOf(0 to hand1, 1 to hand2)

    val playedCards = mapOf(
        se2.hanabi.app.Model.Card.Color.RED to 1,
        se2.hanabi.app.Model.Card.Color.BLUE to 0,
        se2.hanabi.app.Model.Card.Color.GREEN to 0,
        se2.hanabi.app.Model.Card.Color.YELLOW to 4,
        se2.hanabi.app.Model.Card.Color.WHITE to 0
    )

    val discardPile = listOf(
        se2.hanabi.app.Model.Card(color = se2.hanabi.app.Model.Card.Color.RED, value = 1),
        se2.hanabi.app.Model.Card(color = se2.hanabi.app.Model.Card.Color.BLUE, value = 2)
    )

    return GameStatus(
        players = players,
        visibleHands = visibleHands,
        playedCards = playedCards,
        discardPile = discardPile,
        hints = 8,
        strikes = 0,
        gameOver = false,
        currentPlayer = 1
    )
}