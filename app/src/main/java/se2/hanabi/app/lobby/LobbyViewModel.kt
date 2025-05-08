package se2.hanabi.app.lobby

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import kotlinx.coroutines.launch
import java.lang.Exception

class LobbyViewModel : ViewModel() {

    init {
        println("LOBBY VIEWMODEL INIT")
        fetchLobbyCode()
    }

    var lobbyCode by mutableStateOf<String?>(null)
        private set

    private val client = HttpClient(CIO)
    private val baseUrl = "http://localhost:8080"

    fun fetchLobbyCode() {
        viewModelScope.launch {
            try {
                println("Fetching lobby code from server...")
                val response: String = client.get("$baseUrl/create-lobby").body()
                println("Fetched lobby code: $response")
                lobbyCode = response
            } catch (e: Exception) {
                println("ERROR fetching code: ${e.localizedMessage}")
                lobbyCode = "Error fetching lobby code"
            }
        }
    }
}