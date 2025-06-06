package se2.hanabi.app.activities

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import se2.hanabi.app.MainActivity
import se2.hanabi.app.gamePlayUI.GamePlayUI
import se2.hanabi.app.gamePlayUI.GamePlayViewModel
import se2.hanabi.app.gamePlayUI.GamePlayViewModelFactory

class GameActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Get the lobby ID and player ID from the intent
        val lobbyId = intent.getStringExtra("lobbyId") ?: ""
        val playerId = intent.getIntExtra("playerId", -1)
        
        setContent {
            val viewModel = viewModel<GamePlayViewModel>(
                factory = GamePlayViewModelFactory(lobbyId, playerId)
            )

            if (!viewModel.gameOver.collectAsState().value) {
                GamePlayUI()
            } else {
                EndScreen( onBackToMenu = {
                    //Navigate back to MainActiviy and clear the back stack
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                })
            }
        }
    }
}
