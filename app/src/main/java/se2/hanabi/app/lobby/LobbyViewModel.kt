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

    private val _isGameStarted = MutableStateFlow(false)
    val isGameStarted: StateFlow<Boolean> = _isGameStarted

    private val _players = MutableStateFlow<List<PlayerInLobby>>(emptyList())
    val players: StateFlow<List<PlayerInLobby>> = _players

    private val _lobbyCode = mutableStateOf<String?>(null)

    val lobbyCode: String?
        get() = _lobbyCode.value

    private val _isHost = mutableStateOf(false)
    val isHost: Boolean
        get() = _isHost.value

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
                val response: List<PlayerInLobby> =  client.get("http://10.0.2.2:8080/lobby/$code/players").body()

                _players.value = response

                val gameStatusResponse = client.get("http://10.0.2.2:8080/start-game/$code/status")
                if (gameStatusResponse.status == HttpStatusCode.OK) {
                    val gameStarted: Boolean = gameStatusResponse.body()
                    if (gameStarted){
                        _isGameStarted.value = true
                    }
                }
            }catch (e: Exception){
                e.printStackTrace()
                _players.value = emptyList()
            }
        }
    }

    fun startPlayerSync(intervalMillis : Long = 1000L){
        viewModelScope.launch {
            while (true) {
                fetchPlayers()
                delay(intervalMillis)
            }
        }
    }
}

