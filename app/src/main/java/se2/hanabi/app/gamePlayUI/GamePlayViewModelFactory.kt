package se2.hanabi.app.gamePlayUI

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory for creating a GamePlayViewModel with parameters.
 */
class GamePlayViewModelFactory(
    private val lobbyId: String,
    private val playerId: Int
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GamePlayViewModel::class.java)) {
            return GamePlayViewModel(lobbyId, playerId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
