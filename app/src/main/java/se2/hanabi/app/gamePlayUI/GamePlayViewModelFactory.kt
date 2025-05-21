// In a separate file (e.g., gamePlayUI/GamePlayViewModelFactory.kt)
package se2.hanabi.app.gamePlayUI

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class GamePlayViewModelFactory(
    private val lobbyCode: String,
    private val thisPlayerId: Int
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GamePlayViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GamePlayViewModel(lobbyCode, thisPlayerId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}