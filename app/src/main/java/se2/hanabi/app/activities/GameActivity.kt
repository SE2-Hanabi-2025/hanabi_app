package se2.hanabi.app.activities

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import se2.hanabi.app.MainActivity
import se2.hanabi.app.endScreen.EndScreen
import se2.hanabi.app.gamePlayUI.GamePlayUI
import se2.hanabi.app.gamePlayUI.GamePlayViewModel
import se2.hanabi.app.gamePlayUI.GamePlayViewModelFactory

class GameActivity : ComponentActivity() {
    private val cheatSequence = listOf(
        KeyEvent.KEYCODE_VOLUME_DOWN,
        KeyEvent.KEYCODE_VOLUME_DOWN,
        KeyEvent.KEYCODE_VOLUME_UP,
        KeyEvent.KEYCODE_VOLUME_DOWN
    )
    private val inputBuffer = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Get the lobby ID and player ID from the intent
        val lobbyId = intent.getStringExtra("lobbyId") ?: ""
        val playerId = intent.getIntExtra("playerId", -1)
        
        setContent {
            val viewModel = viewModel<GamePlayViewModel>(
                factory = GamePlayViewModelFactory(lobbyId, playerId)
            )

            GamePlayUI()
            if (viewModel.gameOver.collectAsState().value) {
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

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        inputBuffer.add(keyCode)
        if (inputBuffer.size > cheatSequence.size) inputBuffer.removeAt(0)
        if (inputBuffer == cheatSequence) {
            setContent {
                val viewModel = viewModel<GamePlayViewModel>(
                    factory = GamePlayViewModelFactory(
                        intent.getStringExtra("lobbyId") ?: "",
                        intent.getIntExtra("playerId", -1)
                    )
                )
                viewModel.defuseStrikeCheat()
                viewModel.fetchAndUpdateGameStatus()
                GamePlayUI()
                if (viewModel.gameOver.collectAsState().value) {
                    EndScreen( onBackToMenu = {
                        val intent = Intent(this@GameActivity, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    })
                }
            }
            inputBuffer.clear()
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
