package se2.hanabi.app.activities

import android.content.Intent
import android.content.SharedPreferences
import android.hardware.SensorManager
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import se2.hanabi.app.MainActivity
import se2.hanabi.app.Services.MusicService
import se2.hanabi.app.endScreen.EndScreen
import se2.hanabi.app.gamePlayUI.GamePlayUI
import se2.hanabi.app.gamePlayUI.GamePlayViewModel
import se2.hanabi.app.gamePlayUI.GamePlayViewModelFactory
import se2.hanabi.app.ui.components.MuteButton
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

class GameActivity : ComponentActivity() {
    private val inputBuffer = mutableListOf<Int>()
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
            val context = LocalContext.current
            val prefs = context.getSharedPreferences("hanabi_prefs", MODE_PRIVATE)
            val isMuted = remember { mutableStateOf(prefs.getBoolean("isMuted", false)) }
            Box(modifier = Modifier.fillMaxSize()) {
                GamePlayUI(viewModel)
                if (viewModel.gameOver.collectAsState().value) {
                    EndScreen( onBackToMenu = {
                        val intent = Intent(applicationContext, MainActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                        finish()
                    })
                }
                MuteButton(
                    isMuted = isMuted.value,
                    onToggle = {
                        isMuted.value = !isMuted.value
                        prefs.edit().putBoolean("isMuted", isMuted.value).apply()
                        val intent = Intent(context, MusicService::class.java)
                        if (isMuted.value) {
                            intent.action = "MUTE"
                        } else {
                            intent.action = "UNMUTE"
                        }
                        context.startService(intent)
                    },
                    tint = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 24.dp, end = 24.dp)
                        .size(56.dp)
                )
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
            return true
        }
        return super.onKeyDown(keyCode, event)
    }

    @Composable
    fun Content() {
        GamePlayUI(viewModel)
        if (viewModel.gameOver.collectAsState().value) {
            EndScreen(onBackToMenu = {
                //Navigate back to MainActivity and clear the back stack
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            })
        }
    }
}
