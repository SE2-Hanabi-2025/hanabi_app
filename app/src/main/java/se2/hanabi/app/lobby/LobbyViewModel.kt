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


class LobbyViewModel : ViewModel() {

    private val _isGameStarted = MutableStateFlow(false)
    val isGameStarted: StateFlow<Boolean> = _isGameStarted

    private val _players = MutableStateFlow<List<String>>(emptyList())
    val players: StateFlow<List<String>> = _players

    private val _lobbyCode = mutableStateOf<String?>(null)

    val lobbyCode: String?
        get() = _lobbyCode.value

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

    fun fetchPlayers() {
        viewModelScope.launch {
            try {
                val code = _lobbyCode.value ?: return@launch
                
                // Fetch players from server
                val response: List<String> = client.get("$serverUrl/lobby/$code/players").body()
                
                // Filter out duplicates
                val uniquePlayers = response.toSet().toList()
                
                // Make sure current player is in the list
                val currentUsername = _username.value
                if (currentUsername.isNotEmpty() && !uniquePlayers.contains(currentUsername)) {
                    val updatedList = uniquePlayers.toMutableList()
                    updatedList.add(currentUsername)
                    _players.value = updatedList
                    
                    // Log for debugging
                    println("Added current player ($currentUsername) to list: ${updatedList.joinToString()}")
                } else {
                    _players.value = uniquePlayers
                    
                    // Log for debugging
                    println("Updated player list: ${uniquePlayers.joinToString()}")
                }
                
                // Check game status
                val gameStatusResponse = client.get("$serverUrl/start-game/$code/status")
                if (gameStatusResponse.status == HttpStatusCode.OK) {
                    val gameStarted: Boolean = gameStatusResponse.body()
                    if (gameStarted) {
                        _isGameStarted.value = true
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("Error fetching players: ${e.message}")
                
                // If there's an error, ensure at least the current player is in the list
                val currentUsername = _username.value
                if (currentUsername.isNotEmpty() && (_players.value.isEmpty() || !_players.value.contains(currentUsername))) {
                    _players.value = listOf(currentUsername)
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
}

