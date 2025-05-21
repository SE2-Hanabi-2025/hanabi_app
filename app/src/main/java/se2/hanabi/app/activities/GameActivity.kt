package se2.hanabi.app.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import se2.hanabi.app.gamePlayUI.GamePlayUI
import se2.hanabi.app.gamePlayUI.GamePlayViewModel

class GameActivity : ComponentActivity() {

    private val viewModel: GamePlayViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val receivedLobbyCode = intent.getStringExtra("lobbyCode") ?: "Kein Code"
        val receivedPlayerId = intent.getIntExtra("playerId", -1)

        setContent {
            // Create an anonymous ViewModelProvider.Factory using an object expression
            val viewModel: GamePlayViewModel = ViewModelProvider(
                this,
                object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        if (modelClass.isAssignableFrom(GamePlayViewModel::class.java)) {
                            @Suppress("UNCHECKED_CAST")
                            return GamePlayViewModel(receivedLobbyCode, receivedPlayerId) as T
                        }
                        throw IllegalArgumentException("Unknown ViewModel class")
                    }
                }
            ).get(GamePlayViewModel::class.java)

            GamePlayUI(viewModel = viewModel)
        }
    }
}
