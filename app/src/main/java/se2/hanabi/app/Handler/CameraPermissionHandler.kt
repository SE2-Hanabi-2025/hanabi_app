package se2.hanabi.app.Handler

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.core.content.ContextCompat
import androidx.compose.ui.platform.LocalContext

@Composable
fun CameraPermissionHandler(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit
) {
    val TAG = "CameraPermissionHandler"
    val context = LocalContext.current
    var hasCheckedPermission by remember { mutableStateOf(false) }

    val cameraPermission = Manifest.permission.CAMERA
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        Log.d(TAG, "Permission result: $isGranted")
        if (isGranted) {
            Log.d(TAG, "Camera permission granted via launcher")
            onPermissionGranted()
        } else {
            Log.d(TAG, "Camera permission denied via launcher")
            onPermissionDenied()
        }
        hasCheckedPermission = true
    }

    LaunchedEffect(key1 = Unit) {
        Log.d(TAG, "Checking camera permission")
        when {
            ContextCompat.checkSelfPermission(context, cameraPermission) == 
                PackageManager.PERMISSION_GRANTED -> {
                Log.d(TAG, "Camera permission already granted")
                onPermissionGranted()
                hasCheckedPermission = true
            }
            else -> {
                Log.d(TAG, "Requesting camera permission")
                permissionLauncher.launch(cameraPermission)
            }
        }
    }
}
