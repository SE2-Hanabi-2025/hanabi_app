package se2.hanabi.app.Services

import android.util.Log
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.websocket.*
import io.ktor.serialization.kotlinx.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ClosedReceiveChannelException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.Json
import se2.hanabi.app.model.GameStatus
import se2.hanabi.app.model.Hint
import se2.hanabi.app.model.websocket.*

class WebSocketService(
    private val baseUrl: String = "ws://10.0.2.2:8080", // Configurable base URL
    private val json: Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }
) {
    companion object {
        private const val TAG = "HanabiWebSocketService"
    }
    
    private val client = HttpClient(CIO) {
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(json)
        }
    }
    
    private var webSocketSession: WebSocketSession? = null
    private var connectionJob: Job? = null
    
    // Connection state
    private val _connectionState = MutableStateFlow(ConnectionState.DISCONNECTED)
    val connectionState: StateFlow<ConnectionState> = _connectionState
    
    // State for incoming game responses
    private val _gameState = MutableStateFlow<GameStatus?>(null)
    val gameState: StateFlow<GameStatus?> = _gameState
    
    private val _actionResult = MutableSharedFlow<ActionResult>()
    val actionResult: SharedFlow<ActionResult> = _actionResult
    
    private val _error = MutableSharedFlow<String>()
    val error: SharedFlow<String> = _error

    /**
     * Connect to the WebSocket server
     *
     * @param lobbyId The ID of the game lobby
     * @param playerId The ID of the player
     */
    fun connect(lobbyId: String, playerId: Int) {
        if (connectionJob?.isActive == true) {
            Log.d(TAG, "Already connected or connecting")
            return
        }

        _connectionState.value = ConnectionState.CONNECTING
        
        val wsUrl = "$baseUrl/ws/game?lobbyId=$lobbyId&playerId=$playerId"
        Log.d(TAG, "Connecting to $wsUrl")
        
        connectionJob = CoroutineScope(Dispatchers.IO).launch {
            try {
                client.webSocket(urlString = wsUrl) {
                    _connectionState.value = ConnectionState.CONNECTED
                    webSocketSession = this
                    Log.d(TAG, "WebSocket connected")
                    
                    try {
                        // Listen for incoming messages
                        for (frame in incoming) {
                            if (frame is Frame.Text) {
                                handleIncomingMessage(frame.readText())
                            }
                        }
                    } catch (e: ClosedReceiveChannelException) {
                        Log.d(TAG, "WebSocket closed", e)
                    } catch (e: Exception) {
                        Log.e(TAG, "WebSocket error", e)
                        _error.emit("WebSocket error: ${e.localizedMessage}")
                    } finally {
                        _connectionState.value = ConnectionState.DISCONNECTED
                        webSocketSession = null
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to connect to WebSocket", e)
                _connectionState.value = ConnectionState.DISCONNECTED
                _error.emit("Connection failed: ${e.localizedMessage}")
            }
        }
    }
    
    /**
     * Disconnect from the WebSocket server
     */
    fun disconnect() {
        Log.d(TAG, "Disconnecting WebSocket")
        connectionJob?.cancel()
        connectionJob = null
        
        CoroutineScope(Dispatchers.IO).launch {
            webSocketSession?.close()
            webSocketSession = null
            _connectionState.value = ConnectionState.DISCONNECTED
        }
    }
    
    /**
     * Send a play card action
     *
     * @param lobbyId The ID of the game lobby
     * @param playerId The ID of the player
     * @param cardIndex The index of the card to play
     */
    suspend fun playCard(lobbyId: String, playerId: Int, cardIndex: Int) {
        val action = PlayCardAction(
            lobbyId = lobbyId,
            playerId = playerId,
            cardIndex = cardIndex
        )
        sendAction(action)
    }
    
    /**
     * Send a discard card action
     *
     * @param lobbyId The ID of the game lobby
     * @param playerId The ID of the player
     * @param cardIndex The index of the card to discard
     */
    suspend fun discardCard(lobbyId: String, playerId: Int, cardIndex: Int) {
        val action = DiscardCardAction(
            lobbyId = lobbyId,
            playerId = playerId,
            cardIndex = cardIndex
        )
        sendAction(action)
    }
    
    /**
     * Send a hint action
     *
     * @param lobbyId The ID of the game lobby
     * @param playerId The ID of the player giving the hint
     * @param toPlayerId The ID of the player receiving the hint
     * @param hint The hint to give
     */
    suspend fun giveHint(lobbyId: String, playerId: Int, toPlayerId: Int, hint: Hint) {
        val hintType = if (hint.getColor() != null) HintType.COLOR else HintType.VALUE
        val hintValue = if (hint.getColor() != null) {
            hint.getColor().toString()
        } else {
            hint.getValue().toString()
        }
        
        val action = GiveHintAction(
            lobbyId = lobbyId,
            playerId = playerId,
            toPlayerId = toPlayerId,
            hintType = hintType,
            hintValue = hintValue
        )
        sendAction(action)
    }
    
    /**
     * Send an action to the server
     *
     * @param action The action to send
     */    private suspend fun sendAction(action: ClientAction) {
        if (_connectionState.value != ConnectionState.CONNECTED) {
            Log.e(TAG, "Cannot send message: not connected")
            _error.emit("Cannot send message: not connected")
            return
        }
        
        try {            // Manually create JSON string in the expected format            // Log the action details for debugging
            when (action) {
                is PlayCardAction -> {
                    Log.d(TAG, "Preparing PLAY action: lobbyId=${action.lobbyId}, playerId=${action.playerId}, cardIndex=${action.cardIndex}")
                }
                is DiscardCardAction -> {
                    Log.d(TAG, "Preparing DISCARD action: lobbyId=${action.lobbyId}, playerId=${action.playerId}, cardIndex=${action.cardIndex}")
                }
                is GiveHintAction -> {
                    Log.d(TAG, "Preparing HINT action: lobbyId=${action.lobbyId}, playerId=${action.playerId}, toPlayerId=${action.toPlayerId}, hintType=${action.hintType}, hintValue=${action.hintValue}")
                }
            }
            
            val message = when (action) {
                is PlayCardAction -> """
                    {
                        "action": "${action.action}",
                        "lobbyId": "${action.lobbyId}",
                        "playerId": ${action.playerId},
                        "cardIndex": ${action.cardIndex}
                    }
                """.trimIndent()
                
                is DiscardCardAction -> """
                    {
                        "action": "${action.action}",
                        "lobbyId": "${action.lobbyId}",
                        "playerId": ${action.playerId},
                        "cardIndex": ${action.cardIndex}
                    }
                """.trimIndent()
                
                is GiveHintAction -> """
                    {
                        "action": "${action.action}",
                        "lobbyId": "${action.lobbyId}",
                        "playerId": ${action.playerId},
                        "toPlayerId": ${action.toPlayerId},
                        "hintType": "${action.hintType}",
                        "hintValue": "${action.hintValue}"
                    }
                """.trimIndent()
            }
            Log.d(TAG, "Sending message: $message")
            webSocketSession?.send(Frame.Text(message))
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message", e)
            _error.emit("Failed to send message: ${e.localizedMessage}")
        }
    }
    
    /**
     * Handle incoming messages from the server
     *
     * @param message The message received
     */
    private suspend fun handleIncomingMessage(message: String) {
        try {
            Log.d(TAG, "Received message: $message")
            
            when {
                message.contains("\"type\":") -> {
                    // This is likely an ActionResult
                    val result = json.decodeFromString<ActionResult>(message)
                    _actionResult.emit(result)
                    Log.d(TAG, "Processed action result: ${result.type} - ${result.message}")
                }
                message.contains("\"error\":") -> {
                    // This is an error message
                    val errorResponse = json.decodeFromString<ErrorResponse>(message)
                    _error.emit(errorResponse.error)
                    Log.d(TAG, "Processed error: ${errorResponse.error}")
                }
                message.contains("\"gameState\":") || message.contains("\"players\":") -> {
                    // This looks like a game state update
                    try {
                        // Try parsing as a wrapped GameStateUpdate first
                        val update = json.decodeFromString<GameStateUpdate>(message)
                        _gameState.value = update.gameState
                        Log.d(TAG, "Processed game state update")
                    } catch (e: Exception) {
                        // If that fails, try parsing directly as GameStatus
                        val gameStatus = json.decodeFromString<GameStatus>(message)
                        _gameState.value = gameStatus
                        Log.d(TAG, "Processed game state update (direct)")
                    }
                }
                else -> {
                    Log.w(TAG, "Unknown message format: $message")
                    _error.emit("Received unknown message format")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error processing message", e)
            _error.emit("Error processing message: ${e.localizedMessage}")
        }
    }
    
    enum class ConnectionState {
        DISCONNECTED,
        CONNECTING, 
        CONNECTED
    }
}