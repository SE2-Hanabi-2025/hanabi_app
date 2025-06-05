package se2.hanabi.app.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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
        
        Log.d(TAG, "onCreate called")
        
        // Check if camera permission is already granted
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {            
            // Permission is already granted, proceed with camera operations
            Log.d(TAG, "Camera permission already granted")
            setContent {
                ClientTheme {
                    val startMenu = StartMenue()
                    startMenu.StartMenuScreen()
                }
            }
        } else {
            // Request camera permission
            Log.d(TAG, "Requesting camera permission")
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
}