package se2.hanabi.app.activities

import android.content.Intent
import android.hardware.SensorManager
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
    private var isDefuseSequenceStarted = false
    private var isProximityDark = false
    private lateinit var proximityHelper: ProximityCheatHelper
    private lateinit var viewModel: GamePlayViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Get the lobby ID and player ID from the intent
        val lobbyId = intent.getStringExtra("lobbyId") ?: ""
        val playerId = intent.getIntExtra("playerId", -1)
        viewModel = androidx.lifecycle.ViewModelProvider(
            this,
            GamePlayViewModelFactory(lobbyId, playerId)
        )[GamePlayViewModel::class.java]
        setContent {
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
        proximityHelper = ProximityCheatHelper(
            this,
            onProximityDark = { isProximityDark = true },
            onProximityLight = { isProximityDark = false }
        )
    }

    override fun onResume() {
        super.onResume()
        proximityHelper.register()
    }

    override fun onPause() {
        super.onPause()
        proximityHelper.unregister()
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Only allow cheat if proximity is dark
        if (!isProximityDark) {
            inputBuffer.clear()
            isDefuseSequenceStarted = false
            return super.onKeyDown(keyCode, event)
        }
        inputBuffer.add(keyCode)
        // If buffer reaches 4 keys, always use defuseAttemptCheat
        if (inputBuffer.size == 4) {
            val sequence = inputBuffer.map {
                when (it) {
                    KeyEvent.KEYCODE_VOLUME_DOWN -> "DOWN"
                    KeyEvent.KEYCODE_VOLUME_UP -> "UP"
                    else -> "DOWN" // fallback to DOWN for any other key
                }
            }
            val proximity = if (isProximityDark) "DARK" else "LIGHT"
            viewModel.defuseAttemptCheat(sequence, proximity)
            inputBuffer.clear()
            isDefuseSequenceStarted = false
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}
