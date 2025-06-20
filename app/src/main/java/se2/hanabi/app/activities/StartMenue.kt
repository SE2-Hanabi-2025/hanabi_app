package se2.hanabi.app.activities

import android.content.Intent
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.launch
import se2.hanabi.app.endScreen.endAnimations.FireworkLauncher
import se2.hanabi.app.lobby.LobbyActivity
import se2.hanabi.app.R
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import se2.hanabi.app.Handler.CameraPermissionHandler
import se2.hanabi.app.components.QRScanner
import se2.hanabi.app.ui.theme.customFont
import se2.hanabi.app.utils.ServerAddressManager
import androidx.compose.foundation.layout.BoxWithConstraints

class StartMenue {
    @Composable
    fun StartMenuScreen() {
        var showConnectDialog by remember { mutableStateOf(false) }
        var showStartDialog by remember { mutableStateOf(false) }
        var showStatusDialog by remember { mutableStateOf(false) }
        var showJoinDialog by remember { mutableStateOf(false) }
        var lobbyCode by remember { mutableStateOf("") }
        var statusMessage by remember { mutableStateOf("Fetching status...") }
        var isLoading by remember { mutableStateOf(false) }
        var isConnected by remember { mutableStateOf(false) }
        var username by remember { mutableStateOf("") }
        var isUsernameError by remember { mutableStateOf(false) }
        var showAvatarDialog by remember { mutableStateOf(false) }
        var selectedAvatarResId by remember { mutableIntStateOf(R.drawable.whiteavatar) }
        val coroutineScope = rememberCoroutineScope()
        val client = remember { HttpClient(CIO) }
        val context = LocalContext.current
        var showQRScanner by remember { mutableStateOf(false) }
        var hasCameraPermission by remember { mutableStateOf(false) }
        var permissionDenied by remember { mutableStateOf(false) }
        val elementSpacing = 10.dp
        val elementWidth = 200.dp
        val elementHeight = 60.dp

        fun fetchStatus() {
            coroutineScope.launch {
                isLoading = true
                try {
                    val response: HttpResponse = client.get(ServerAddressManager.STATUS_URL)
                    statusMessage = response.body()
                } catch (e: Exception) {
                    statusMessage = "Failed to fetch status"
                }
                showStatusDialog = true
                isLoading = false            }
        }

        fun joinLobby(code: String) {
            if (code.length != 6) {
                statusMessage = "Invalid lobby code. Code must be 6 characters."
                showStatusDialog = true
                return
            }
              coroutineScope.launch {
                isLoading = true
                try {
                    val encodedName = URLEncoder.encode(username, StandardCharsets.UTF_8.toString())
                    // Join the lobby
                    val response: HttpResponse = client.get(ServerAddressManager.getJoinLobbyUrl(code) + "?name=$encodedName&avatarResID=$selectedAvatarResId")
                    val responseBody: String = response.body()
                    
                    if (responseBody.startsWith("Joined lobby", ignoreCase = true)) {
                        isConnected = true
                        val playerId = responseBody.split(" ").last().toIntOrNull()
                        val intent = Intent(context, LobbyActivity::class.java).apply {
                           putExtra("lobbyCode", code)
                           putExtra("playerId", playerId)
                           putExtra("username", username)
                           putExtra("avatarResID", selectedAvatarResId)
                           putExtra("isHost", false)
                        }
                        context.startActivity(intent)
                    } else {
                        statusMessage = "Failed to join lobby: $responseBody"
                        showStatusDialog = true

                    }
                } catch (e: Exception) {
                    statusMessage = "Error joining lobby: ${e.localizedMessage}"
                    showStatusDialog = true
                } finally {
                    isLoading = false                }
            }
        }
        
        fun createLobbyAndJoin() {
            coroutineScope.launch {
                isLoading = true
                try {
                    val response: HttpResponse = client.get(ServerAddressManager.getCreateLobbyUrl())
                    val createdCode: String = response.body()
                    val encodedName = URLEncoder.encode(username, StandardCharsets.UTF_8.toString())
                    val joinResponse: HttpResponse = client.get(ServerAddressManager.getJoinLobbyUrl(createdCode) + "?name=$encodedName&avatarResID=$selectedAvatarResId")
                    val joinResponseBody: String = joinResponse.body()

                    println("-> Join Response: $joinResponseBody")
                    
                    if (joinResponseBody.startsWith("Joined lobby", ignoreCase = true)) {
                        val playerId = joinResponseBody.split(" ").last().toIntOrNull()
                        val intent = Intent(context, LobbyActivity::class.java).apply {
                            putExtra("lobbyCode", createdCode)
                            putExtra("playerId", playerId)
                            putExtra("username", username)
                            putExtra("avatarResID", selectedAvatarResId)
                            putExtra("isHost", true)
                        }
                        context.startActivity(intent)
                    } else {
                        statusMessage = "Failed to join lobby: $joinResponseBody"
                        showStatusDialog = true
                    }
                } catch (e: Exception) {
                    statusMessage = "Failed to create lobby"
                    showStatusDialog = true
                } finally {
                    isLoading = false
                }
            }
        }

        BoxWithConstraints(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            val screenHeight = maxHeight
            val titleHeight = screenHeight * 0.27f
            val showFireworksCounter = remember { mutableIntStateOf(0) }
            val fireworkOn = showFireworksCounter.intValue >= 3

            Image(
                painter = painterResource(id = R.drawable.backgroundimage),
                contentDescription = "Background Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            if (fireworkOn) {
                FireworkLauncher(onAnimationEnd = {
                    showFireworksCounter.intValue = 0
                })
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(elementSpacing),
                modifier = Modifier.fillMaxSize()
            ) {
                // Title area (reserves space, clickable for fireworks)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(titleHeight)
                        .background(Color.Transparent)
                        .clickable { showFireworksCounter.intValue += 1 },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "",
                        fontSize = 40.sp,
                        color = Color.White,
                        fontFamily = customFont,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.5f),
                                offset = Offset(2f, 4f),
                                blurRadius = 8f
                            )
                        )
                    )
                }
                //Avatar Placeholder
                Box(
                    modifier = Modifier
                        .size(elementHeight)
                        .clip(CircleShape)
                        .background(Color.DarkGray)
                        .clickable { showAvatarDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = selectedAvatarResId),
                        contentDescription = "Select Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                androidx.compose.material3.TextField(
                    value = username,
                    onValueChange = { newValue ->
                        val maxLength = 6
                        val allowedChars = "a-zA-Z0-9,.!_;:?"

                        val regex = Regex("^[$allowedChars]*$")
                        if (newValue.length <= maxLength){
                            if (newValue.matches(regex)){
                                username=newValue
                                isUsernameError = false
                            }
                            else{
                                if (newValue.length < username.length || newValue.isEmpty()){
                                    username = newValue
                                }
                                isUsernameError = newValue.isNotEmpty()
                            }
                        }
                        else{
                            isUsernameError = true
                        }
                    },
                    label = {
                        Text("Enter Username")
                    },
                    singleLine = true,
                    isError = isUsernameError,
                    supportingText ={
                        if (isUsernameError){
                            Text("Wrong input!")
                        }
                    },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .width(elementWidth),
                    colors = TextFieldDefaults.colors(
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent
                    ),
                    textStyle = LocalTextStyle.current.copy(textAlign = TextAlign.Center)
                )
                val wiggleAnim2 = rememberInfiniteTransition(label = "wiggle2").animateFloat(
                    initialValue = 2f,
                    targetValue = -2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 600, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ), label = "wiggle2"
                )
                Text(
                    text = "CREATE A LOBBY",
                    modifier = Modifier
                        .clickable { createLobbyAndJoin() }
                        .padding(screenHeight * 0.05f)
                        .fillMaxWidth()

                        .graphicsLayer {
                            rotationZ = wiggleAnim2.value
                        },
                    textAlign = TextAlign.Center,
                    fontSize = 26.sp,
                    color = Color(0xFFFCAE21),
                    fontFamily = customFont,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.5f),
                            offset = Offset(2f, 4f),
                            blurRadius = 8f
                        )
                    )
                )
                val wiggleAnim = rememberInfiniteTransition(label = "wiggle").animateFloat(
                    initialValue = -2f,
                    targetValue = 2f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 600, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    ), label = "wiggle"
                )
                Text(
                    text = "JOIN A LOBBY",
                    modifier = Modifier
                        .clickable { showJoinDialog = true }
                        .padding(screenHeight * 0.05f)
                        .fillMaxWidth()
                        .graphicsLayer {
                            rotationZ = wiggleAnim.value
                        },
                    textAlign = TextAlign.Center,
                    fontSize = 26.sp,
                    color = Color(0xFFFCAE21),
                    fontFamily = customFont,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 0.5f),
                            offset = Offset(2f, 4f),
                            blurRadius = 8f
                        )
                    )
                )
            }
            Surface(
                modifier = Modifier
                    .padding(16.dp)
                    .size(48.dp)
                    .align(Alignment.BottomEnd),
                shape = RoundedCornerShape(25.dp),
                color = Color.White,
                tonalElevation = 4.dp
            ) {
                IconButton(
                    onClick = { fetchStatus() }
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "Status",
                        tint = Color.DarkGray,
                        modifier = Modifier.size(50.dp)
                    )
                }
            }
        }


        // Show Loading Spinner
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Color.Black.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
        }

        if (showConnectDialog) {
            PopupDialog("Connecting...", "Attempting to connect to server.") {
                showConnectDialog = false
            }
        }

        if (showStartDialog) {
            PopupDialog("Starting Game...", "Preparing game lobby.") {
                showStartDialog = false
            }
        }

        if (showStatusDialog) {
            PopupDialog("Server Status", statusMessage) {
                showStatusDialog = false
            }
        }

        if (showJoinDialog) {
            AlertDialog(
                onDismissRequest = { showJoinDialog = false },
                title = { Text("Join Lobby") },
                text = {
                    Column {
                        androidx.compose.material3.TextField(
                            value = lobbyCode,
                            onValueChange = { lobbyCode = it.filter { char -> char.isLetterOrDigit()}.take(6).uppercase()},
                            label = { Text("Enter Lobby Code with 6 chars") }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {showQRScanner = true},
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            Icon(imageVector = Icons.Default.Camera, contentDescription = "Scan QR Code")
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Scan QR Code")
                        }
                    } },
                confirmButton = {
                    Button(onClick = {
                        if (lobbyCode.length == 6) {
                            joinLobby(lobbyCode)
                            showJoinDialog = false
                        }
                    }) {
                        Text("Join")
                    }
                },
                dismissButton = {
                    Button(onClick = { showJoinDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
        if (showAvatarDialog){
            AvatarSelectionDialog(
                onDismiss = {showAvatarDialog = false},
                onAvatarSelected = {
                        avatarRes -> selectedAvatarResId = avatarRes
                    showAvatarDialog = false
                }
            )
        }
        if (showQRScanner) {
            CameraPermissionHandler(
                onPermissionGranted = {
                    hasCameraPermission = true
                    permissionDenied = false
                    println("Camera permission granted in StartMenue!")
                },
                onPermissionDenied = {
                    hasCameraPermission = false
                    permissionDenied = true
                    println("Camera permission denied in StartMenue!")
                }
            )

            if (hasCameraPermission) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    QRScanner(
                        onScanned = { scannedCode ->
                            println("QR Code scanned: $scannedCode")
                            if (scannedCode.length == 6) {
                                // Make sure we have a 6-character lobby code
                                joinLobby(scannedCode)
                                showQRScanner = false
                                showJoinDialog = false
                            } else {
                                statusMessage = "Invalid QR code format. Expected 6-character lobby code."
                                showStatusDialog = true
                                showQRScanner = false
                            }
                        },
                        onDismiss = { showQRScanner = false }
                    )
                }
            } else if (permissionDenied) {
                // Zeige Snackbar oder Dialog
                statusMessage = "Camera permission denied. Please grant camera permission in app settings."
                showStatusDialog = true
                showQRScanner = false
            }
        }
    }

    @Composable
    fun AvatarSelectionDialog(
        onDismiss: () -> Unit,
        onAvatarSelected: (Int) -> Unit
    ){
        val avatarOptions = listOf(
            R.drawable.redavatar,
            R.drawable.whiteavatar,
            R.drawable.greenavatar,
            R.drawable.blueavatar,
            R.drawable.yellowavatar
        )

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Choose your Avatar")},
            text = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    avatarOptions.forEach {avatarRes ->
                        Image(painter = painterResource(id = avatarRes),
                            contentDescription = "Choose your player",
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .clickable { onAvatarSelected(avatarRes) }
                                .padding(4.dp),
                            contentScale = ContentScale.Crop)
                    }
                }
            },
            confirmButton = {
                Button(onClick = onDismiss) { Text("Cancel") }
            }
        )}

    @Composable
    fun PopupDialog(title: String, message: String, onDismiss: () -> Unit) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(title, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
            text = { Text(message, fontSize = 16.sp) },
            confirmButton = {
                Button(onClick = onDismiss) {
                    Text("OK")
                }
            }
        )
    }
}

