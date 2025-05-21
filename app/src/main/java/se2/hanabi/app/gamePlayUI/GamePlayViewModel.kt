package se2.hanabi.app.gamePlayUI

import androidx.compose.runtime.mutableStateOf
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
 * GamePlayViewModel displays a gameStatus object.
 * it passes on action (hint, play, discard) to the GamePlayService.
 * handles local logic of showing selected card/hand/ih, as well as when hint selecter is shown.
 *
 */
class GamePlayViewModel(
    private val _lobbyCode: String,
    private val _thisPlayerId: Int
) : ViewModel() {

    val thisPlayer: MutableStateFlow<Int> = MutableStateFlow(_thisPlayerId)


    private lateinit var gamePlayService: GamePlayService
    init {
        gamePlayService= GamePlayService(_lobbyCode, _thisPlayerId)
    }

    private var gameStatus: GameStatus = generateTestGameStatus()

    // game state info
    private val _Players = MutableStateFlow(gameStatus.players)
    val numPlayers: MutableStateFlow<List<Player>> = _Players

    private val _thisPlayersHand = MutableStateFlow<List<Int>>(gameStatus.playerCardIds) // id of "cat" from genertaeTestGameStatus
    val thisPlayersHand: MutableStateFlow<List<Int>> = _thisPlayersHand

    private val _otherPlayersHands = MutableStateFlow(gameStatus.visibleHands)
    val otherPlayersHands: MutableStateFlow<Map<Int, List<Card>>> = _otherPlayersHands

    private val _stackValues = MutableStateFlow<Map<Card.Color, Int>>(gameStatus.playedCards)
    val stackValues: MutableStateFlow<Map<Card.Color, Int>> = _stackValues

    private val _numRemainingCard = MutableStateFlow(gameStatus.numRemainingCards)
    val numRemainingCard: MutableStateFlow<Int> = _numRemainingCard

    private val _lastDiscardedCard = MutableStateFlow<Card?>(gameStatus.discardPile.last())
    val lastDiscardedCard: MutableStateFlow<Card?> = _lastDiscardedCard

    private val _numRemainingHintTokens = MutableStateFlow(gameStatus.numRemainingHintTokens)
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

    private val _cardsShowingColorHints = MutableStateFlow<Map<Int, Card.Color>>(gameStatus.cardsShowingColorHints)
    val cardsShowingColorHints: StateFlow<Map<Int, Card.Color>> = _cardsShowingColorHints
    private val _cardsShowingValueHints =  MutableStateFlow<Map<Int, Int>>(gameStatus.cardsShowingValueHints)
    val cardsShowingValueHints: StateFlow<Map<Int,Int>> = _cardsShowingValueHints

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

    val shownColorHints = mapOf (
        0 to Card.Color.RED,
        5 to Card.Color.BLUE,
        8 to Card.Color.GREEN,
    )

    val shownValueHints = mapOf (
        3 to 5,
        5 to 4,
        7 to 3,
    )

    return GameStatus(
        players = players,
        playerCardIds = listOf(6,7,8),
        visibleHands = visibleHands,
        playedCards = playedCards,
        discardPile = discardPile,
        numRemainingCards = Random.nextInt(16),
        cardsShowingColorHints = shownColorHints,
        cardsShowingValueHints = shownValueHints,
        numRemainingHintTokens = 8,
        strikes = 0,
        gameOver = false,
        currentPlayer = 1
    )
}