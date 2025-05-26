package se2.hanabi.app.gamePlayUI

import android.util.Log
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

/**
 * GamePlayViewModel displays a gameStatus object.
 * Simplified version that only handles initialization of game state
 * without WebSockets or game action mechanics.
 */
class GamePlayViewModel(
    private val lobbyId: String,
    private val playerId: Int
): ViewModel() {
    companion object {
        private const val TAG = "HanabiGamePlayVM"
    }
    private val gamePlayService: GamePlayService = GamePlayService(
        lobbyId = lobbyId,
        playerId = playerId
    )

    // Leerer initialer GameStatus, wird vom Backend gefüllt
    private var gameStatus: GameStatus = GameStatus(
        players = emptyList(),
        playersHand = emptyList(),
        visibleHands = emptyMap(),
        playedCards = emptyMap(),
        discardPile = emptyList(),
        numRemainingCard = 0,
        shownHints = emptyMap(),
        hintTokens = 8,
        strikes = 0,
        gameOver = false,
        currentPlayer = 0
    )

    // Status-Nachricht für Feedback
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage

    // game state info
    private val _Players = MutableStateFlow<List<Player>>(emptyList())
    val numPlayers: MutableStateFlow<List<Player>> = _Players

    private val _thisPlayer = MutableStateFlow(playerId)
    val thisPlayer: MutableStateFlow<Int> = _thisPlayer

    private val _thisPlayersHand = MutableStateFlow<List<Int>>(emptyList())
    val thisPlayersHand: MutableStateFlow<List<Int>> = _thisPlayersHand

    private val _otherPlayersHands = MutableStateFlow<Map<Int, List<Card>>>(emptyMap())
    val otherPlayersHands: MutableStateFlow<Map<Int, List<Card>>> = _otherPlayersHands

    private val _stackValues = MutableStateFlow<Map<Card.Color, Int>>(emptyMap())
    val stackValues: MutableStateFlow<Map<Card.Color, Int>> = _stackValues

    private val _numRemainingCard = MutableStateFlow(0)
    val numRemainingCard: MutableStateFlow<Int> = _numRemainingCard

    private val _lastDiscardedCard = MutableStateFlow<Card?>(null)
    val lastDiscardedCard: MutableStateFlow<Card?> = _lastDiscardedCard

    private val _numRemainingHintTokens = MutableStateFlow(8)
    val numRemainingHintTokens: MutableStateFlow<Int> = _numRemainingHintTokens

    private val _numRemainingFuseTokens = MutableStateFlow(0)
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

    private val _shownColorHints = MutableStateFlow<MutableMap<Int, Card.Color>>(mutableMapOf())
    val shownColorHints: StateFlow<MutableMap<Int, Card.Color>> = _shownColorHints
    private val _shownValueHints =  MutableStateFlow<MutableMap<Int, Int>>(mutableMapOf())
    val shownValueHints: StateFlow<MutableMap<Int,Int>> = _shownValueHints

    init {
        Log.d(TAG, "Initialisiere GamePlayViewModel - LobbyId: $lobbyId, PlayerId: $playerId")

        // Initialen Spielstatus abrufen
        viewModelScope.launch {
            _statusMessage.value = "Spieldaten werden geladen..."
            Log.d(TAG, "Fordere initialen Spielstatus an...")

            gamePlayService.getGameStatus()?.let { status ->
                Log.d(TAG, "Spielstatus erfolgreich erhalten: ${status.players.size} Spieler, " +
                        "${status.numRemainingCard} verbleibende Karten")
                updateGameStatus(status)
                _statusMessage.value = "Spiel wurde geladen"
            } ?: run {
                Log.e(TAG, "Fehler beim Laden des initialen Spielstatus")
                _statusMessage.value = "Fehler beim Laden der Spieldaten"
            }
        }
    }

    private fun updateGameStatus(newStatus: GameStatus) {
        Log.d(TAG, "Aktualisiere Spielstatus")
        gameStatus = newStatus

        _Players.value = newStatus.players
        Log.v(TAG, "Spieler: ${newStatus.players.joinToString { it.name }}")

        _thisPlayersHand.value = newStatus.playersHand
        Log.v(TAG, "Eigene Hand: ${newStatus.playersHand.size} Karten, IDs: ${newStatus.playersHand}")

        _otherPlayersHands.value = newStatus.visibleHands
        Log.v(TAG, "Hände anderer Spieler: ${newStatus.visibleHands.size} Spieler haben sichtbare Karten")
        newStatus.visibleHands.forEach { (playerId, cards) ->
            Log.v(TAG, "  Spieler $playerId: ${cards.size} Karten - ${cards.joinToString { "${it.color}_${it.value}" }}")
        }

        _stackValues.value = newStatus.playedCards
        Log.v(TAG, "Gespielte Karten: ${newStatus.playedCards.entries.joinToString { "${it.key}: ${it.value}" }}")

        _numRemainingCard.value = newStatus.numRemainingCard
        Log.v(TAG, "Verbleibende Karten im Deck: ${newStatus.numRemainingCard}")

        _lastDiscardedCard.value = newStatus.discardPile.lastOrNull()
        Log.v(TAG, "Ablagestapel: ${newStatus.discardPile.size} Karten, letzte Karte: ${newStatus.discardPile.lastOrNull()}")

        _numRemainingHintTokens.value = newStatus.hintTokens
        Log.v(TAG, "Hinweis-Token: ${newStatus.hintTokens}")

        _numRemainingFuseTokens.value = newStatus.strikes
        Log.v(TAG, "Fehlschläge: ${newStatus.strikes}")

        Log.d(TAG, "Aktueller Spieler: ${newStatus.currentPlayer}, Spiel beendet: ${newStatus.gameOver}")

        // Reset ausgewählte Elemente nach Statusänderung
        resetSelection()
    }

    // Einfache UI-Interaktionsmethoden, ohne Serveranfragen
    fun onPlayersCardClick(cardId: Int) {
        Log.d(TAG, "Karte in eigener Hand geklickt: $cardId")
        _selectedPlayer.value = -1
        hintReset()

        val newValue = if (cardId == selectedCardId.value) {
            Log.d(TAG, "Karte abgewählt")
            -1
        } else {
            Log.d(TAG, "Karte ausgewählt")
            cardId
        }
        _selectedCard.value = newValue
    }

    fun onOtherPlayersHandClick(playerId: Int) {
        Log.d(TAG, "Hand eines anderen Spielers geklickt: Spieler $playerId")
        _selectedCard.value = -1
        hintReset()

        val newValue = if (playerId == _selectedPlayer.value) {
            Log.d(TAG, "Spieler abgewählt")
            -1
        } else {
            Log.d(TAG, "Spieler ausgewählt für Hinweis")
            playerId
        }
        _selectedPlayer.value = newValue
    }

    fun onHintClick(hint: Hint) {
        val hintType = if (hint.getColor() != null) "Farbe: ${hint.getColor()}" else "Wert: ${hint.getValue()}"
        Log.d(TAG, "Hinweis ausgewählt: $hintType")

        _selectedHint.value = if (hint == selectedHint.value) {
            Log.d(TAG, "Hinweis abgewählt")
            null
        } else {
            Log.d(TAG, "Hinweis-Typ festgelegt: $hintType")
            hint
        }

        if (_selectedHint.value != null) {
            var validHint = false
            val targetPlayerId = selectedPlayerId.value

            Log.d(TAG, "Prüfe, ob Hinweis für Spieler $targetPlayerId gültig ist")
            _otherPlayersHands.value[targetPlayerId]?.forEach { card ->
                val matchColor = card.color == _selectedHint.value?.getColor()
                val matchValue = card.value == _selectedHint.value?.getValue()

                if (matchColor || matchValue) {
                    Log.d(TAG, "Karte ${card.color}_${card.value} passt zum Hinweis - Hinweis ist gültig")
                    validHint = true
                }
            }

            _isValidHint.value = validHint
            Log.d(TAG, "Hinweis ist ${if (validHint) "gültig" else "ungültig"}")
        } else {
            _isValidHint.value = false
            Log.d(TAG, "Kein Hinweis ausgewählt")
        }
    }

    // Platzhalter-Methoden für UI ohne Serverinteraktion
    fun onGiveHintClick() {
        Log.d(TAG, "Hinweis-Funktion nicht implementiert (keine Serverinteraktion)")
        _statusMessage.value = "Hinweis-Funktion nicht implementiert"
    }

    fun onColorStackClick(color: Card.Color) {
        Log.d(TAG, "Kartenspielen nicht implementiert (keine Serverinteraktion)")
        _statusMessage.value = "Kartenspielen nicht implementiert"
    }

    fun onDiscardStackClick() {
        Log.d(TAG, "Kartenabwerfen nicht implementiert (keine Serverinteraktion)")
        _statusMessage.value = "Kartenabwerfen nicht implementiert"
    }

    // helper functions
    private fun resetSelection() {
        Log.v(TAG, "Setze Auswahl zurück")
        _selectedCard.value = -1
        _selectedPlayer.value = -1
        hintReset()
    }

    private fun hintReset() {
        Log.v(TAG, "Setze Hinweis-Auswahl zurück")
        _selectedHint.value = null
        _isValidHint.value = false
    }
}
