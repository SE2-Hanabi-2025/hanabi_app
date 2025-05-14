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
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import se2.hanabi.app.R

data class PlayerInLobby(
    val name: String,
    val avatarResID: Int
)

class LobbyViewModel : ViewModel() {

    private val _players = MutableStateFlow<List<PlayerInLobby>>(emptyList())
    val players: StateFlow<List<PlayerInLobby>> = _players

    private var currentPlayerName: String? = null
    private var currentPlayerAvatarResID: Int = R.drawable.whiteavatar

    private val _lobbyCode = mutableStateOf<String?>(null)

    val lobbyCode: String?
        get() = _lobbyCode.value

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json{
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    fun setLobbyCode(code: String) {
        _lobbyCode.value = code
    }

    fun setCurrentPlayerInfo(name: String, avatarResID: Int){
        currentPlayerName = name
        currentPlayerAvatarResID = avatarResID
        _players.value = players.value + PlayerInLobby(name, avatarResID)
    }

    fun leaveLobby(currentName: String?){
        currentName?.let { name -> _players.value = players.value.filterNot { it.name == name } }
    }

    fun startPlayerSync(intervalMillis : Long = 1000L){
        viewModelScope.launch {
            while (true) {
                fetchPlayers()
                delay(intervalMillis)
            }
        }
    }

    private suspend fun fetchPlayers() {
        val code = _lobbyCode.value ?: return
            try {
                val rawList: List<String> =  client.get("http://10.0.2.2:8080/lobby/$code/players").body()
                val updated = rawList.map { name ->
                    if ( name == currentPlayerName){
                        PlayerInLobby(name, currentPlayerAvatarResID)
                    } else {
                        PlayerInLobby(name, R.drawable.whiteavatar)
                    }
                }
                _players.value = updated
            }catch (e: Exception){
                e.printStackTrace()
                _players.value = emptyList()

        }
    }
}

