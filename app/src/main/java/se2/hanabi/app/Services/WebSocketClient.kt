package se2.hanabi.app.Services

import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import se2.hanabi.app.model.Card
import se2.hanabi.app.model.Hint

/**
 * WebSocketClient is a sample implementation showing how to use the WebSocketService
 * for the Hanabi game.
 * 
 * This class demonstrates the usage patterns and is meant to be used as a reference
 * or for testing purposes.
 */
class WebSocketClient(
    private val lobbyId: String,
    private val playerId: Int
) {
    companion object {
        private const val TAG = "HanabiWSClient"
    }

    private val webSocketService = WebSocketService()
    private var reconnectJob: Job? = null
    private var autoReconnect = true
    
    /**
     * Starts the WebSocket client and sets up listeners
     */
    fun start() {
        Log.d(TAG, "Starting WebSocket client for lobby $lobbyId, player $playerId")
        
        setupListeners()
        webSocketService.connect(lobbyId, playerId)
    }
    
    /**
     * Stops the WebSocket client and cleans up resources
     */
    fun stop() {
        Log.d(TAG, "Stopping WebSocket client")
        autoReconnect = false
        reconnectJob?.cancel()
        reconnectJob = null
        webSocketService.disconnect()
    }
    
    /**
     * Sets up listeners for WebSocket events
     */
    private fun setupListeners() {
        CoroutineScope(Dispatchers.Main).launch {
            webSocketService.connectionState.collect { state ->
                when (state) {
                    WebSocketService.ConnectionState.CONNECTED -> {
                        Log.d(TAG, "WebSocket connected")
                        reconnectJob?.cancel()
                        reconnectJob = null
                    }
                    WebSocketService.ConnectionState.CONNECTING -> {
                        Log.d(TAG, "WebSocket connecting")
                    }
                    WebSocketService.ConnectionState.DISCONNECTED -> {
                        Log.d(TAG, "WebSocket disconnected")
                        if (autoReconnect && reconnectJob == null) {
                            setupReconnectJob()
                        }
                    }
                }
            }
        }
        
        CoroutineScope(Dispatchers.Main).launch {
            webSocketService.gameState.collect { gameState ->
                gameState?.let {
                    Log.d(TAG, "Received game state: ${it.players.size} players, " +
                            "${it.numRemainingCard} cards remaining")
                }
            }
        }
        
        CoroutineScope(Dispatchers.Main).launch {
            webSocketService.actionResult.collect { result ->
                Log.d(TAG, "Action result: ${result.type} - ${result.message}")
            }
        }
        
        CoroutineScope(Dispatchers.Main).launch {
            webSocketService.error.collect { error ->
                Log.e(TAG, "WebSocket error: $error")
            }
        }
    }
    
    /**
     * Sets up an automatic reconnect job
     */
    private fun setupReconnectJob() {
        reconnectJob = CoroutineScope(Dispatchers.IO).launch {
            var retryCount = 0
            val maxRetries = 5
            
            while (autoReconnect && retryCount < maxRetries) {
                delay(3000L + (retryCount * 2000L)) // Exponential backoff
                Log.d(TAG, "Attempting to reconnect, attempt ${retryCount + 1}/$maxRetries")
                
                webSocketService.connect(lobbyId, playerId)
                retryCount++
            }
            
            if (retryCount >= maxRetries) {
                Log.e(TAG, "Failed to reconnect after $maxRetries attempts")
            }
        }
    }
    
    /**
     * Example: Play a card
     */
    suspend fun playCard(cardIndex: Int) {
        Log.d(TAG, "Playing card at index $cardIndex")
        webSocketService.playCard(lobbyId, playerId, cardIndex)
    }
    
    /**
     * Example: Discard a card
     */
    suspend fun discardCard(cardIndex: Int) {
        Log.d(TAG, "Discarding card at index $cardIndex")
        webSocketService.discardCard(lobbyId, playerId, cardIndex)
    }
    
    /**
     * Example: Give a color hint
     */
    suspend fun giveColorHint(toPlayerId: Int, color: Card.Color) {
        Log.d(TAG, "Giving $color hint to player $toPlayerId")
        webSocketService.giveHint(lobbyId, playerId, toPlayerId, Hint(color))
    }
    
    /**
     * Example: Give a value hint
     */
    suspend fun giveValueHint(toPlayerId: Int, value: Int) {
        Log.d(TAG, "Giving $value hint to player $toPlayerId")
        webSocketService.giveHint(lobbyId, playerId, toPlayerId, Hint(value))
    }
}
