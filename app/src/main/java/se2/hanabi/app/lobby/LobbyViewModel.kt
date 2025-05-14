package se2.hanabi.app.lobby


import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import okhttp3.internal.http2.ErrorCode

data class PlayerInLobby(
    val name: String,
    val avatarResID: Int
)


class LobbyViewModel : ViewModel() {

    private val _lobbyCode = mutableStateOf<String?>(null)
    val lobbyCode: String?
        get() = _lobbyCode.value

    fun setLobbyCode(code: String) {
        _lobbyCode.value = code
    }

    private val _playerList = mutableListOf<PlayerInLobby>()
    val playerList: List<PlayerInLobby>
        get() = _playerList

    fun setCurrentPlayerInfo(name: String, avatarResID: Int, lobbyCode: String){
        _playerList.add(PlayerInLobby(name, avatarResID))
    }

    fun leaveLobby(currentName: String?){
        currentName?.let { name ->
        _playerList.removeAll { it.name == name }
    }}
}