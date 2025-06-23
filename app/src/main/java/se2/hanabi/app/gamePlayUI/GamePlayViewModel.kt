package se2.hanabi.app.gamePlayUI

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import se2.hanabi.app.model.GameStatus
import se2.hanabi.app.model.Player
import se2.hanabi.app.Services.GamePlayService
import se2.hanabi.app.Services.WebSocketService
import se2.hanabi.app.model.Card
import se2.hanabi.app.model.Hint
import se2.hanabi.app.model.websocket.ResultType
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect

/**
 * GamePlayViewModel displays a gameStatus object.
 * it passes on action (hint, play, discard) to the GamePlayService.
 * handles local logic of showing selected card/hand/ih, as well as when hint selecter is shown.
 *
 */
class GamePlayViewModel(
    private val lobbyId: String,
    private val playerId: Int,
    private val max_score: Int = 25,
): ViewModel() {
    companion object {
        private const val TAG = "HanabiGamePlayVM"
    }

    private val gamePlayService: GamePlayService = GamePlayService(
        lobbyId = lobbyId,
        playerId = playerId
    )

    private val webSocketService = WebSocketService()

    // Leerer initialer GameStatus, wird vom Backend gefüllt
    private var gameStatus: GameStatus = GameStatus(
        players = emptyList(),
        playerCardIds = emptyList(),
        visibleHands = emptyMap(),
        playedCards = emptyMap(),
        discardPile = emptyList(),
        numRemainingCards = 0,
        cardsShowingColorHints = emptyMap(),
        cardsShowingValueHints = emptyMap(),
        numRemainingHintTokens = 8,
        strikes = 0,
        gameOver = false,
        gameLost = false,
        currentScore = 0,
        currentPlayerId = 0
    )

    // Status-Nachricht für Feedback
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage

    // Connection state
    private val _connectionState = MutableStateFlow(WebSocketService.ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<WebSocketService.ConnectionState> = _connectionState

    // game state info
    private val _players = MutableStateFlow<List<Player>>(emptyList())
    val players: MutableStateFlow<List<Player>> = _players

    private val _thisPlayer = MutableStateFlow(playerId)
    val thisPlayer: MutableStateFlow<Int> = _thisPlayer

    private val _currentPlayer = MutableStateFlow(0)
    val currentPlayer: StateFlow<Int> = _currentPlayer

    private val _isMyTurn = MutableStateFlow(false)
    val isMyTurn: StateFlow<Boolean> = _isMyTurn

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

    private val _gameOver = MutableStateFlow(false)
    val gameOver: StateFlow<Boolean> = _gameOver

    private val _gameLost = MutableStateFlow(false)
    val gameLost: StateFlow<Boolean> = _gameLost

    private val _currentScore = MutableStateFlow(0)
    val currentScore: StateFlow<Int> = _currentScore

    // game play info
    private val _selectedCard = MutableStateFlow<Int>(-1)
    val selectedCardId: MutableStateFlow<Int> = _selectedCard

    private val _selectedPlayer = MutableStateFlow<Int>(-1)
    val selectedPlayerId: MutableStateFlow<Int> = _selectedPlayer

    private val _selectedHint = MutableStateFlow<Hint?>(null)
    val selectedHint: MutableStateFlow<Hint?> = _selectedHint

    private val _isValidHint = MutableStateFlow(false)
    val isValidHint: MutableStateFlow<Boolean> = _isValidHint

    private val _cardsShowingColorHints = MutableStateFlow<Map<Int, Card.Color>>(emptyMap())
    val cardsShowingColorHints: StateFlow<Map<Int, Card.Color>> = _cardsShowingColorHints
    private val _cardsShowingValueHints =  MutableStateFlow<Map<Int, Int>>(emptyMap())
    val cardsShowingValueHints: StateFlow<Map<Int,Int>> = _cardsShowingValueHints

    // Drag-and-drop state
    private val _draggedCardId = MutableStateFlow<Int?>(null)
    val draggedCardId: StateFlow<Int?> = _draggedCardId

    private val _cheatHand = MutableStateFlow<List<Card>>(emptyList())
    val cheatHand: StateFlow<List<Card>> = _cheatHand.asStateFlow()

    private var cheatSent = false

    // Track the last round a cheat was sent
    private var lastCheatRound: Int? = null

    private var cheatSentThisTurn = false

    // Track if cheat hand has been shown this turn (for UI logic)
    var cheatHandShownThisTurn = false

    // Drag-and-drop pointer and drop zone state
    private val _pointerPosition = MutableStateFlow<Offset?>(null)
    val pointerPosition: StateFlow<Offset?> = _pointerPosition
    private val colorStackBounds = mutableMapOf<Card.Color, Rect>()
    private var discardZoneBounds: Rect? = null

    init {
        Log.d(TAG, "GamePlayViewModel initialized for player $playerId, lobby $lobbyId")
        Log.d(TAG, "Initialisiere GamePlayViewModel - LobbyId: $lobbyId, PlayerId: $playerId")

        // Set up WebSocket listeners
        setupWebSocketListeners()

        // Connect to WebSocket
        connectToWebSocket()

        // Initialen Spielstatus abrufen (für den Fall, dass WebSocket nicht sofort verbindet)
        viewModelScope.launch {
            _statusMessage.value = "Spieldaten werden geladen..."
            Log.d(TAG, "Fordere initialen Spielstatus an...")

            gamePlayService.getGameStatus()?.let { status ->
                Log.d(TAG, "Spielstatus erfolgreich erhalten: ${status.players.size} Spieler, " +
                        "${status.numRemainingCards} verbleibende Karten")
                updateGameStatus(status)
                _statusMessage.value = "Spiel wurde geladen"
            } ?: run {
                Log.e(TAG, "Fehler beim Laden des initialen Spielstatus")
                _statusMessage.value = "Fehler beim Laden der Spieldaten"
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Disconnect WebSocket when ViewModel is cleared
        webSocketService.disconnect()
    }

    private fun connectToWebSocket() {
        Log.d(TAG, "Verbinde mit WebSocket - Lobby: $lobbyId, Spieler: $playerId")
        webSocketService.connect(lobbyId, playerId)
    }

    private fun setupWebSocketListeners() {
        viewModelScope.launch {
            // Listen for connection state changes
            webSocketService.connectionState.collect { state ->
                _connectionState.value = state
                when (state) {
                    WebSocketService.ConnectionState.CONNECTED -> {
                        _statusMessage.value = "Verbunden mit dem Spielserver"
                        Log.d(TAG, "WebSocket verbunden")
                    }
                    WebSocketService.ConnectionState.CONNECTING -> {
                        _statusMessage.value = "Verbinde mit dem Spielserver..."
                        Log.d(TAG, "WebSocket verbindet...")
                    }
                    WebSocketService.ConnectionState.DISCONNECTED -> {
                        _statusMessage.value = "Verbindung zum Server getrennt"
                        Log.d(TAG, "WebSocket getrennt")
                    }
                }
            }
        }

        viewModelScope.launch {
            // Listen for game state updates
            webSocketService.gameState.collect { newGameState ->
                newGameState?.let {
                    Log.d(TAG, "Neuer Spielstatus empfangen")
                    updateGameStatus(it)
                }
            }
        }

        viewModelScope.launch {
            // Listen for action results
            webSocketService.actionResult.collect { result ->
                when (result.type) {
                    ResultType.SUCCESS -> {
                        _statusMessage.value = "Erfolgreich: ${result.message}"
                        Log.d(TAG, "Aktion erfolgreich: ${result.message}")
                    }
                    ResultType.FAILURE -> {
                        _statusMessage.value = "Failure: ${result.message}"
                        Log.d(TAG, "Action failed: ${result.message}")
                    }
                    ResultType.INVALID_MOVE -> {
                        _statusMessage.value = "Ungültiger Zug: ${result.message}"
                        Log.w(TAG, "Ungültiger Zug: ${result.message}")
                    }
                    ResultType.ERROR -> {
                        _statusMessage.value = "Fehler: ${result.message}"
                        Log.e(TAG, "Fehler bei Aktion: ${result.message}")
                    }
                }
            }
        }

        viewModelScope.launch {
            // Listen for errors
            webSocketService.error.collect { errorMsg ->
                _statusMessage.value = "Fehler: $errorMsg"
                Log.e(TAG, "WebSocket Fehler: $errorMsg")
            }
        }
    }

    private fun updateGameStatus(newStatus: GameStatus) {
        Log.d(TAG, "Aktualisiere Spielstatus")
        gameStatus = newStatus

        _players.value = newStatus.players
        Log.v(TAG, "Spieler: ${newStatus.players.joinToString { it.name }}")

        if (_currentPlayer.value != newStatus.currentPlayerId) {
            cheatSentThisTurn = false
            cheatHandShownThisTurn = false // Reset when turn changes
        }
        _currentPlayer.value = newStatus.currentPlayerId
        _isMyTurn.value = newStatus.currentPlayerId == playerId
        Log.v(TAG, "Aktueller Spieler: ${newStatus.currentPlayerId}, Ich bin dran: ${_isMyTurn.value}")

        _thisPlayersHand.value = newStatus.playerCardIds
        _cheatHand.value = newStatus.ownHand ?: emptyList()
        Log.v(TAG, "Eigene Hand: ${newStatus.playerCardIds.size} Karten, IDs: ${newStatus.playerCardIds}")

        _otherPlayersHands.value = newStatus.visibleHands
        Log.v(TAG, "Hände anderer Spieler: ${newStatus.visibleHands.size} Spieler haben sichtbare Karten")
        newStatus.visibleHands.forEach { (playerId, cards) ->
            Log.v(TAG, "  Spieler $playerId: ${cards.size} Karten - ${cards.joinToString { "${it.color}_${it.value}" }}")
        }

        _stackValues.value = newStatus.playedCards
        Log.v(TAG, "Gespielte Karten: ${newStatus.playedCards.entries.joinToString { "${it.key}: ${it.value}" }}")

        _numRemainingCard.value = newStatus.numRemainingCards
        Log.v(TAG, "Verbleibende Karten im Deck: ${newStatus.numRemainingCards}")

        _lastDiscardedCard.value = newStatus.discardPile.lastOrNull()
        Log.v(TAG, "Ablagestapel: ${newStatus.discardPile.size} Karten, letzte Karte: ${newStatus.discardPile.lastOrNull()}")

        _cardsShowingColorHints.value = newStatus.cardsShowingColorHints
        Log.v(TAG, "Cards showing color hints: ${newStatus.cardsShowingColorHints.size} hints")
        newStatus.cardsShowingColorHints.forEach { (cardId, color) ->
            Log.v(TAG, "  Card: $cardId - $color")
        }

        _cardsShowingValueHints.value = newStatus.cardsShowingValueHints
        Log.v(TAG, "Cards showing value hints: ${newStatus.cardsShowingValueHints.size} hints")
        newStatus.cardsShowingValueHints.forEach { (cardId, value) ->
            Log.v(TAG, "  Card: $cardId - $value")
        }
        _numRemainingHintTokens.value = newStatus.numRemainingHintTokens
        Log.v(TAG, "Hinweis-Token: ${newStatus.numRemainingHintTokens}")

        _numRemainingFuseTokens.value = newStatus.strikes
        Log.v(TAG, "Fehlschläge: ${newStatus.strikes}")

        _gameOver.value = newStatus.gameOver
        Log.v(TAG, "Spiel beendet: ${newStatus.gameOver}")

        _gameLost.value = newStatus.gameLost
        Log.v(TAG, "Game lost: ${newStatus.gameLost}")

        _currentScore.value = newStatus.currentScore
        Log.v(TAG, "Current score: ${newStatus.currentScore}")


        // Reset ausgewählte Elemente nach Statusänderung
        resetSelection()
    }

    // UI-Interaktionsmethoden
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
            val playerHand = _otherPlayersHands.value[targetPlayerId]

            if (playerHand != null) {
                playerHand.forEach { card ->
                    val matchColor = card.color == _selectedHint.value?.getColor()
                    val matchValue = card.value == _selectedHint.value?.getValue()

                    if (matchColor || matchValue) {
                        Log.d(TAG, "Karte ${card.color}_${card.value} passt zum Hinweis - Hinweis ist gültig")
                        validHint = true
                    }
                }
            } else {
                Log.e(TAG, "Keine Karten für Spieler $targetPlayerId gefunden")
            }

            _isValidHint.value = validHint
            Log.d(TAG, "Hinweis ist ${if (validHint) "gültig" else "ungültig"}")
        } else {
            _isValidHint.value = false
            Log.d(TAG, "Kein Hinweis ausgewählt")
        }
    }

    fun onColorStackClick(color: Card.Color) {
        // Handle the color stack click event here
        // For example, log the click or update some state
        Log.d("GamePlayViewModel", "Color stack clicked: $color")
    }

    // WebSocket-Aktionen
    fun onPlayCardClick() {
        if (_selectedCard.value < 0) {
            _statusMessage.value = "Wähle zuerst eine Karte aus"
            return
        }

        if (!_isMyTurn.value) {
            _statusMessage.value = "Du bist nicht an der Reihe"
            return
        }

        viewModelScope.launch {
            val cardIndex = _thisPlayersHand.value.indexOf(_selectedCard.value)
            if (cardIndex >= 0) {
                _statusMessage.value = "Spiele Karte..."
                Log.d(TAG, "Spiele Karte an Index $cardIndex")
                webSocketService.playCard(lobbyId, playerId, cardIndex)
            } else {
                _statusMessage.value = "Fehler: Karte nicht gefunden"
                Log.e(TAG, "Karte $_selectedCard.value nicht in der Hand gefunden")
            }
        }
    }

    fun onDiscardCardClick() {
        if (_selectedCard.value < 0) {
            _statusMessage.value = "Wähle zuerst eine Karte aus"
            return
        }

        if (!_isMyTurn.value) {
            _statusMessage.value = "Du bist nicht an der Reihe"
            return
        }

        if (_numRemainingHintTokens.value >= 8) {
            _statusMessage.value = "Du kannst keine Karte abwerfen, wenn alle Hint-Token verfügbar sind"
            return
        }

        viewModelScope.launch {
            val cardIndex = _thisPlayersHand.value.indexOf(_selectedCard.value)
            if (cardIndex >= 0) {
                _statusMessage.value = "Werfe Karte ab..."
                Log.d(TAG, "Werfe Karte an Index $cardIndex ab")
                webSocketService.discardCard(lobbyId, playerId, cardIndex)
            } else {
                _statusMessage.value = "Fehler: Karte nicht gefunden"
                Log.e(TAG, "Karte $_selectedCard.value nicht in der Hand gefunden")
            }
        }
    }

    fun onGiveHintClick() {
        val selectedPlayer = _selectedPlayer.value
        val selectedHint = _selectedHint.value

        if (selectedPlayer < 0) {
            _statusMessage.value = "Wähle zuerst einen Spieler aus"
            return
        }

        if (selectedHint == null) {
            _statusMessage.value = "Wähle zuerst einen Hinweis aus"
            return
        }

        if (!_isValidHint.value) {
            _statusMessage.value = "Der ausgewählte Hinweis ist nicht gültig"
            return
        }

        if (!_isMyTurn.value) {
            _statusMessage.value = "Du bist nicht an der Reihe"
            return
        }

        if (_numRemainingHintTokens.value <= 0) {
            _statusMessage.value = "Keine Hinweis-Token mehr verfügbar"
            return
        }

        viewModelScope.launch {
            _statusMessage.value = "Gebe Hinweis..."
            Log.d(TAG, "Gebe Hinweis an Spieler $selectedPlayer: $selectedHint")
            webSocketService.giveHint(lobbyId, playerId, selectedPlayer, selectedHint)
        }
    }

    fun reconnectWebSocket() {
        webSocketService.disconnect()
        connectToWebSocket()
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

    fun getMaxScore(): Int {
        return max_score
    }

    fun startDraggingCard(cardId: Int) {
        _draggedCardId.value = cardId
    }

    fun stopDraggingCard() {
        _draggedCardId.value = null
    }

    /**
     * Called when a card is dropped on a color stack.
     * Handles Hanabi rules: auto-placement for 1s, validation, and strikes.
     * Updated: Dropping a 1 on any empty stack auto-places it on the correct color stack.
     */
    fun onPlayCardDrop(cardId: Int?, targetColor: Card.Color) {
        val chand = thisPlayersHand.value
        if (cardId == null || chand.none { it == cardId }) {
            Log.d(TAG, "Drop error: cardId=$cardId not found in hand: $chand")
        }
        if (cardId == null) return
        if (!_isMyTurn.value) {
            _statusMessage.value = "Du bist nicht an der Reihe"
            return
        }
        val hand = _thisPlayersHand.value
        val cardIndex = hand.indexOf(cardId)
        if (cardIndex < 0) {
            _statusMessage.value = "Karte nicht gefunden"
            return
        }
        // Get the card details from own hand (cheatHand) instead of visible hands
        val myVisibleHand = cheatHand.value
        val card = myVisibleHand.find { it.getID() == cardId }
        if (card == null) {
            _statusMessage.value = "Karte nicht gefunden (Details fehlen)"
            Log.d(TAG, "Drop error: cardId=$cardId not found in own hand: ${myVisibleHand.map { it.getID() }}")
            return
        }
        // Hanabi rules: auto-place 1s, validate others
        if (card.value == 1) {
            // Find the correct color stack for this 1
            val correctStackValue = stackValues.value[card.color] ?: 0
            if (correctStackValue == 0) {
                // Play the card regardless of drop target if its color stack is empty
                playCardByIndex(cardIndex)
                return
            } else {
                // 1 cannot be played if its color stack is not empty
                giveStrikeAndDiscard(cardIndex)
                return
            }
        } else {
            // For other values, must match stack color and be next in sequence
            val stackValue = stackValues.value[targetColor] ?: 0
            if (card.color == targetColor && stackValue == card.value - 1) {
                playCardByIndex(cardIndex)
                return
            } else {
                giveStrikeAndDiscard(cardIndex)
                return
            }
        }
    }

    private fun playCardByIndex(cardIndex: Int) {
        viewModelScope.launch {
            _statusMessage.value = "Spiele Karte..."
            Log.d(TAG, "Spiele Karte an Index $cardIndex (Drag-and-drop)")
            webSocketService.playCard(lobbyId, playerId, cardIndex)
        }
    }

    private fun giveStrikeAndDiscard(cardIndex: Int) {
        viewModelScope.launch {
            _statusMessage.value = "Falsche Karte! Strike erhalten."
            Log.d(TAG, "Falsche Karte an Index $cardIndex (Drag-and-drop), Strike und Ablage")
            // Use playCard, not discardCard, so backend handles strike/fuse logic
            webSocketService.playCard(lobbyId, playerId, cardIndex)
        }
    }

    fun observeTiltCheat(isTilted: StateFlow<Boolean>) {
        viewModelScope.launch {
            var lastTilted = false
            isTilted.collect { tilted ->
                if (tilted && !lastTilted && !cheatSentThisTurn && _isMyTurn.value) {
                    webSocketService.sendCheatAction(lobbyId, playerId)
                    cheatSentThisTurn = true
                }
                lastTilted = tilted
            }
        }
    }

    fun onDiscardCardDrop(cardId: Int?) {
        val hand = thisPlayersHand.value
        if (cardId == null || hand.none { it == cardId }) {
            Log.d(TAG, "Drop error: cardId=$cardId not found in hand: $hand")
        }
        if (cardId == null) return
        if (!_isMyTurn.value) {
            _statusMessage.value = "Du bist nicht an der Reihe"
            return
        }
        if (_numRemainingHintTokens.value >= 8) {
            _statusMessage.value = "Du kannst keine Karte abwerfen, wenn alle Hint-Token verfügbar sind"
            return
        }
        viewModelScope.launch {
            val cardIndex = _thisPlayersHand.value.indexOf(cardId)
            if (cardIndex >= 0) {
                _statusMessage.value = "Werfe Karte ab..."
                Log.d(TAG, "Werfe Karte an Index $cardIndex ab (Drag-and-drop)")
                webSocketService.discardCard(lobbyId, playerId, cardIndex)
            } else {
                _statusMessage.value = "Fehler: Karte nicht gefunden"
                Log.e(TAG, "Karte $cardId nicht in der Hand gefunden")
            }
        }
    }

    // Drag-and-drop pointer and drop zone bounds
    fun updatePointerPosition(position: Offset) {
        Log.d(TAG, "Pointer position updated: $position")
        _pointerPosition.value = position
    }
    fun updateColorStackBounds(color: Card.Color, bounds: Rect) {
        Log.d(TAG, "Color stack bounds updated for $color: $bounds")
        colorStackBounds[color] = bounds
    }
    fun updateDiscardZoneBounds(bounds: Rect) {
        Log.d(TAG, "Discard zone bounds updated: $bounds")
        discardZoneBounds = bounds
    }

    fun tryDropOnColorStack(cardId: Int?, color: Card.Color) {
        val pointer = _pointerPosition.value
        val bounds = colorStackBounds[color]
        Log.d(TAG, "Try drop on color stack $color: pointer=$pointer, bounds=$bounds")
        if (pointer != null && bounds != null && bounds.contains(pointer)) {
            Log.d(TAG, "Pointer is inside color stack $color bounds. Dropping card.")
            onPlayCardDrop(cardId, color)
        } else {
            Log.d(TAG, "Pointer is NOT inside color stack $color bounds. Drop ignored.")
        }
    }
    fun tryDropOnDiscard(cardId: Int?) {
        val pointer = _pointerPosition.value
        val bounds = discardZoneBounds
        Log.d(TAG, "Try drop on discard: pointer=$pointer, bounds=$bounds")
        if (pointer != null && bounds != null && bounds.contains(pointer)) {
            Log.d(TAG, "Pointer is inside discard bounds. Dropping card.")
            onDiscardCardDrop(cardId)
        } else {
            Log.d(TAG, "Pointer is NOT inside discard bounds. Drop ignored.")
        }
    }

    fun tryDropOnAnyZone(cardId: Int?) {
        val pointer = _pointerPosition.value
        var dropped = false
        // Try all color stacks
        for ((color, bounds) in colorStackBounds) {
            if (pointer != null && bounds != null && bounds.contains(pointer)) {
                Log.d(TAG, "Pointer is inside color stack $color bounds. Dropping card.")
                onPlayCardDrop(cardId, color)
                dropped = true
                break
            }
        }
        // Try discard
        val discardBounds = discardZoneBounds
        if (!dropped && pointer != null && discardBounds != null && discardBounds.contains(pointer)) {
            Log.d(TAG, "Pointer is inside discard bounds. Dropping card.")
            onDiscardCardDrop(cardId)
            dropped = true
        }
        // If not dropped anywhere, do nothing (no strike, no discard)
        if (!dropped) {
            Log.d(TAG, "Pointer is not inside any drop zone. Card will return to hand. No strike or discard.")
            // No action needed; UI should reset card position
        }
    }

    fun onCheatRequested() {
        // Only send cheat if not already shown this turn
        if (!cheatHandShownThisTurn) {
            viewModelScope.launch {
                webSocketService.sendCheatAction(lobbyId, playerId)
            }
            cheatHandShownThisTurn = true
        }
    }
}
