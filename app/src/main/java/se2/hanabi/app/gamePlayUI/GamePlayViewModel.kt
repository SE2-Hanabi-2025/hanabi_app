package se2.hanabi.app.gamePlayUI

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import se2.hanabi.app.model.GameStatus
import se2.hanabi.app.model.Player
import se2.hanabi.app.Services.GamePlayService
import se2.hanabi.app.model.Card
import se2.hanabi.app.model.Hint
import kotlin.random.Random

/**
 * GamePlayViewModel stores the current game state and handles event such as button clicks.
 *
 */
class GamePlayViewModel: ViewModel() {
    private val gamePlayService: GamePlayService = GamePlayService(lobbyId = 12345, playerId = 42)
    private var gameStatus: GameStatus = generateTestGameStatus()

    // game state info
    private val _Players = MutableStateFlow(gameStatus.players)
    val numPlayers: MutableStateFlow<List<Player>> = _Players

    private val _thisPlayer = MutableStateFlow(2) // id of "cat" from genertaeTestGameStatus
    val thisPlayer: MutableStateFlow<Int> = _thisPlayer

    private val _thisPlayersHand = MutableStateFlow<List<Int>>(gameStatus.playersHand) // id of "cat" from genertaeTestGameStatus
    val thisPlayersHand: MutableStateFlow<List<Int>> = _thisPlayersHand

    private val _otherPlayersHands = MutableStateFlow(gameStatus.visibleHands)
    val otherPlayersHands: MutableStateFlow<Map<Int, List<Card>>> = _otherPlayersHands

    private val _stackValues = MutableStateFlow<Map<Card.Color, Int>>(gameStatus.playedCards)
    val stackValues: MutableStateFlow<Map<Card.Color, Int>> = _stackValues

    private val _numRemainingCard = MutableStateFlow(gameStatus.numRemainingCard)
    val numRemainingCard: MutableStateFlow<Int> = _numRemainingCard

    private val _lastDiscardedCard = MutableStateFlow<Card?>(gameStatus.discardPile.last())
    val lastDiscardedCard: MutableStateFlow<Card?> = _lastDiscardedCard

    private val _numRemainingHintTokens = MutableStateFlow(gameStatus.hintTokens)
    val numRemainingHintTokens: MutableStateFlow<Int> = _numRemainingHintTokens

    private val _numRemainingFuseTokens = MutableStateFlow(gameStatus.strikes)
    val numRemainingFuseTokens: MutableStateFlow<Int> = _numRemainingFuseTokens

    // game play info
    private val _selectedCard = MutableStateFlow<Int>(-1)
    val selectedCardId: MutableStateFlow<Int> = _selectedCard

    private val _selectedPlayer = MutableStateFlow<Int>(-1)
    val selectedPlayerId: MutableStateFlow<Int> = _selectedPlayer

    private val _selectedHint = MutableStateFlow<Hint?>(null)
    val selectedHint: MutableStateFlow<Hint?> = _selectedHint

    private val _isValidHint = MutableStateFlow(false)
    val isValidHint: MutableStateFlow<Boolean> = _isValidHint

    private val _currentPlayer = MutableStateFlow(gameStatus.currentPlayer)
    val currentPlayer: MutableStateFlow<Int> = _currentPlayer

    private val _shownColorHints = MutableStateFlow<MutableMap<Int, Card.Color>>(mutableMapOf())
    val shownColorHints: StateFlow<MutableMap<Int, Card.Color>> = _shownColorHints
    private val _shownValueHints =  MutableStateFlow<MutableMap<Int, Int>>(mutableMapOf())
    val shownValueHints: StateFlow<MutableMap<Int,Int>> = _shownValueHints

    // local functions
    fun onPlayersCardClick(cardId: Int) {
        _selectedPlayer.value = -1
        hintReset()
         _selectedCard.value = if (cardId == selectedCardId.value) -1 else cardId
    }

    fun onOtherPlayersHandClick(playerId: Int) {
        _selectedCard.value = -1
        hintReset()
        _selectedPlayer.value = if (playerId == _selectedPlayer.value) -1 else playerId
    }

    fun onHintClick(hint: Hint) {
        _selectedHint.value = if (hint == selectedHint.value) null else hint
        if (_selectedHint.value != null) {
            var validHint = false;
            _otherPlayersHands.value.get(selectedPlayerId.value)?.forEach() { card ->
                if (card.color == _selectedHint.value?.getColor() || card.value == _selectedHint.value?.getValue()) {
                    validHint = true
                }
            }
            _isValidHint.value = validHint
        } else {
            _isValidHint.value = false
        }
    }


    // call server requests
    fun onGiveHintClick() {
        if (isValidHint.value) {
            viewModelScope.launch {
                gamePlayService.giveHint(toPlayerId = selectedPlayerId.value, hint = selectedHint.value!!)
                gameStatus = gamePlayService.getGameStatus()?: gameStatus
            }
            resetSelection()
        }
    }

    fun onColorStackClick(color: Card.Color) {
        if (_selectedCard.value != -1) {
            viewModelScope.launch {
                gamePlayService.playCard(selectedCardId.value, color)
                gameStatus = gamePlayService.getGameStatus()?: gameStatus
            }
        }

    }

    fun onDiscardStackClick() {
        viewModelScope.launch {
            gamePlayService.discardCard(cardId = selectedCardId.value)
            gameStatus = gamePlayService.getGameStatus()?: gameStatus
        }
    }

    // helper functions
    private fun resetSelection() {
        _selectedCard.value = -1
        _selectedPlayer.value = -1
        hintReset()
    }

    private fun hintReset() {
        _selectedHint.value = null
        _isValidHint.value = false
    }

}

// functions to produce game state for demo/development
fun generateTestColorStackValues(): SnapshotStateList<Int> {
    return mutableStateListOf(Random.nextInt(6),Random.nextInt(6),Random.nextInt(6),Random.nextInt(6),Random.nextInt(6),)
}

fun randomCard(): Card {
    return Card(Card.Color.entries[Random.nextInt(Card.Color.entries.size)],Random.nextInt(5)+1)
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
        hand.add( randomCard())
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
        Card(color = Card.Color.RED, value = 1),
        Card(color = Card.Color.BLUE, value = 3),
        Card(color = Card.Color.GREEN, value = 2)
    )
    val hand2 = listOf(
        Card(color = Card.Color.YELLOW, value = 1),
        Card(color = Card.Color.WHITE, value = 5),
        Card(color = Card.Color.RED, value = 2)
    )
    // cat's hand is not visable
    val visibleHands = mapOf(0 to hand1, 1 to hand2)

    val playedCards = mapOf(
        Card.Color.RED to 1,
        Card.Color.BLUE to 0,
        Card.Color.GREEN to 0,
        Card.Color.YELLOW to 4,
        Card.Color.WHITE to 0
    )

    val discardPile = listOf(
        Card(color = Card.Color.RED, value = 1),
        Card(color = Card.Color.BLUE, value = 2)
    )

    return GameStatus(
        players = players,
        playersHand = listOf(6,7,8),
        visibleHands = visibleHands,
        playedCards = playedCards,
        discardPile = discardPile,
        numRemainingCard = Random.nextInt(16),
        shownHints = HashMap<Int, Hint>(),
        hintTokens = 8,
        strikes = 0,
        gameOver = false,
        currentPlayer = 2
    )
}