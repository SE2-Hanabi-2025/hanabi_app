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

    // Mutable state für den Lobby-Code
    private val _lobbyCode = mutableStateOf<String?>(null)
    // Öffentliche Eigenschaft für den Lobby-Code
    val lobbyCode: String?
        get() = _lobbyCode.value

    // Methode zum Setzen des Lobby-Codes
    fun setLobbyCode(code: String) {
        _lobbyCode.value = code
    }
}