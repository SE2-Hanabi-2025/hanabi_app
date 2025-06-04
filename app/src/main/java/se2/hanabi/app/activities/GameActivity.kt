package se2.hanabi.app.activities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import se2.hanabi.app.gamePlayUI.GamePlayUI

class GameActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Get the lobby ID and player ID from the intent
        val lobbyId = intent.getStringExtra("lobbyId") ?: ""
        val playerId = intent.getIntExtra("playerId", -1)
        
        setContent {
            GamePlayUI(
                lobbyId = lobbyId,
                playerId = playerId
            )
        }
    }
}
