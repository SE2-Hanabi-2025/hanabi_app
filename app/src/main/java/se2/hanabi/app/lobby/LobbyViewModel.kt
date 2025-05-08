package se2.hanabi.app.lobby


import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel


class LobbyViewModel : ViewModel() {

    private val _lobbyCode = mutableStateOf<String?>(null)
    val lobbyCode: String?
        get() = _lobbyCode.value

    fun setLobbyCode(code: String) {
        _lobbyCode.value = code
    }
}