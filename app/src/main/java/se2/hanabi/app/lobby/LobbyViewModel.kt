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


class LobbyViewModel : ViewModel() {

    private val _players = MutableStateFlow<List<String>>(emptyList())
    val players: StateFlow<List<String>> = _players

    private val _lobbyCode = mutableStateOf<String?>(null)

    val lobbyCode: String?
        get() = _lobbyCode.value

    private val client = HttpClient(CIO)

    fun setLobbyCode(code: String) {
        _lobbyCode.value = code
    }

    fun fetchPlayers() {
        viewModelScope.launch {
            try {
                val code = _lobbyCode.value ?: return@launch
                val response: List<String> =  client.get("http://10.0.2.2:8080/lobby/$code/players").body()
                println("Fetched players: $response") // Debugging line
                _players.value = response
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

