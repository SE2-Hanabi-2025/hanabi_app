package se2.hanabi.app.activities

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.zIndex
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import se2.hanabi.app.Services.MusicService
import se2.hanabi.app.ui.components.MuteButton
import se2.hanabi.app.ui.theme.ClientTheme

class StartMenuActivity : ComponentActivity() {
    private val TAG = "StartMenuActivity"
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Camera permission is granted, proceed with camera operations
            Log.d(TAG, "Camera permission granted")
            Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show()
            setContent {
                ClientTheme {
                    val startMenu = StartMenue()
                    startMenu.StartMenuScreen()
                }
            }
        } else {
            // Handle the case where permission is denied
            Log.d(TAG, "Camera permission denied")
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
            setContent {
                ClientTheme {
                    val startMenu = StartMenue()
                    startMenu.StartMenuScreen()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Start background music service
        val musicIntent = Intent(this, MusicService::class.java)
        startService(musicIntent)
        Log.d(TAG, "onCreate called")
        // Check if camera permission is already granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.d(TAG, "Camera permission already granted")
            setContent {
                ClientTheme {
                    val prefs = getSharedPreferences("hanabi_prefs", MODE_PRIVATE)
                    val isMuted = remember { mutableStateOf(prefs.getBoolean("isMuted", false)) }
                    val startMenu = StartMenue()
                    Box(modifier = Modifier.fillMaxSize()) {
                        startMenu.StartMenuScreen()
                        MuteButton(
                            isMuted = isMuted.value,
                            onToggle = {
                                isMuted.value = !isMuted.value
                                prefs.edit().putBoolean("isMuted", isMuted.value).apply()
                                val intent = Intent(this@StartMenuActivity, MusicService::class.java)
                                if (isMuted.value) {
                                    intent.action = "MUTE"
                                } else {
                                    intent.action = "UNMUTE"
                                }
                                startService(intent)
                            },
                            tint = Color.White,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 24.dp, end = 24.dp)
                                .size(56.dp)
                        )
                    }
                }
            }
        } else {
            // Request camera permission
            Log.d(TAG, "Requesting camera permission")
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    override fun onResume() {
        super.onResume()
        // Ensure music state matches mute preference
        val prefs = getSharedPreferences("hanabi_prefs", MODE_PRIVATE)
        val isMuted = prefs.getBoolean("isMuted", false)
        val musicIntent = Intent(this, MusicService::class.java)
        musicIntent.action = if (isMuted) "MUTE" else "UNMUTE"
        startService(musicIntent)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop background music service to prevent resource leaks
        val musicIntent = Intent(this, MusicService::class.java)
        stopService(musicIntent)
    }
}