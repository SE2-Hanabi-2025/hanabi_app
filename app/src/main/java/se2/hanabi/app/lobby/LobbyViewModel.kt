package se2.hanabi.app.lobby

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import io.ktor.client.request.get
import kotlinx.coroutines.delay
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json


@kotlinx.serialization.Serializable
data class PlayerInLobby(
    val name: String,
    val avatarResID: Int
)

class LobbyViewModel : ViewModel() {

    // Public, um direkten Zugriff aus der Activity zu ermöglichen
    val _isGameStarted = MutableStateFlow(false)
    val isGameStarted: StateFlow<Boolean> = _isGameStarted

    private val _players = MutableStateFlow<List<PlayerInLobby>>(emptyList())
    val players: StateFlow<List<PlayerInLobby>> = _players

    private val _lobbyCode = mutableStateOf<String?>(null)
    val lobbyCode: String?
        get() = _lobbyCode.value

    private val _playerId = mutableStateOf<Int?>(null)

    private val _isHost = mutableStateOf(false)
    val isHost: Boolean
        get() = _isHost.value
        
    // Username of the current player
    private val _username = mutableStateOf("")
    val username: String
        get() = _username.value

    // Server URL for the game server
    private val serverUrl = "http://10.0.2.2:8080"

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json{
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }
    fun setIsHost(isHost: Boolean) {
        _isHost.value = isHost
    }

    fun setLobbyCode(code: String) {
        _lobbyCode.value = code
    }

    fun setPlayerId(playerId: Int?) {
        _playerId.value = playerId
    }

    fun getPlayerId(): Int? {
        return _playerId.value
    }

    fun fetchPlayers() {
        viewModelScope.launch {
            try {
                val code = _lobbyCode.value ?: return@launch

                  // Fetch players from server
                val response: List<PlayerInLobby> =  client.get("$serverUrl/lobby/$code/players").body()
                
                // Use all players including duplicates
                val allPlayers = response.toMutableList()
                
                // Make sure current player is in the list
                val currentUsername = _username.value
                if (currentUsername.isNotEmpty() && !allPlayers.any{it.name ==currentUsername}) {
                    allPlayers.add(PlayerInLobby(currentUsername, 0))
                    
                    println("Added current player ($currentUsername) to list: ${allPlayers.joinToString { it.name }}")
                } else {
                    
                    // Log for debugging
                    println("Updated player list: ${allPlayers.joinToString { it.name }}")
                }
                  // Use the complete list including duplicates
                _players.value = allPlayers
                
                // Check game status
                try {
                    println("Checking game status for lobby: $code")
                    val gameStatusUrl = "$serverUrl/start-game/$code/status"
                    println("URL: $gameStatusUrl")
                    
                    val gameStatusResponse = client.get(gameStatusUrl)
                    println("Game status response: ${gameStatusResponse.status}")
                    
                    if (gameStatusResponse.status == HttpStatusCode.OK) {
                        val gameStarted: Boolean = gameStatusResponse.body()
                        println("Game started: $gameStarted")
                        _isGameStarted.value = gameStarted
                    } else {
                        println("Game status check failed: ${gameStatusResponse.status}")
                    }
                } catch (e: Exception) {
                    println("Error checking game status: ${e.message}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("Error fetching players: ${e.message}")
                
                // If there's an error, ensure at least the current player is in the list
                val currentUsername = _username.value
                if (currentUsername.isNotEmpty() && (_players.value.isEmpty() || !_players.value.any{it.name == currentUsername})) {
                    _players.value = listOf(PlayerInLobby(currentUsername, 0))
                    println("Added only current player ($currentUsername) after error")
                }
            }
        }
    }

    fun startPlayerSync(intervalMillis: Long = 2000L) {
        viewModelScope.launch {
            // Initial delay to make sure any join operations are completed
            delay(500L)
            
            // Fetch players immediately once
            fetchPlayers()
            
            // Then start regular polling
            while (true) {
                delay(intervalMillis)
                fetchPlayers()
            }
        }
    } 
    
    // Set username when joining a lobby
    fun setUsername(username: String) {
        _username.value = username
    }

    /*// Get player ID by matching username in the players list
    fun getPlayerId(): Int {
        val username = _username.value
        val playersList = _players.value
        
        // If we have a username, find its index in the players list
        // The index corresponds to the player ID assigned by the server
        if (username.isNotEmpty()) {
            val index = playersList.indexOf(username)
            if (index != -1) {
                return index
            }
        }
        
        // If we can't find the player or have no username, return -1
        // The calling code should handle this appropriately
        return 0
    }*/
}

