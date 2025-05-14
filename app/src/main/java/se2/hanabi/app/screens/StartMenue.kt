package se2.hanabi.app.screens

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import io.ktor.http.parameters
import kotlinx.coroutines.launch
import se2.hanabi.app.EndAnimations.FireworkLauncher
import se2.hanabi.app.GameActivity
import se2.hanabi.app.lobby.LobbyActivity
import se2.hanabi.app.R
import se2.hanabi.app.ui.theme.ClientTheme


class StartMenuActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ClientTheme {
                StartMenuScreen()
            }
        }
    }

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
        var isValidUsername by remember { mutableStateOf(false) }
        var invalidUsernameMsg by remember { mutableStateOf("Invalid username") }
        var isUsernameError by remember { mutableStateOf(false) }
        var showAvatarDialog by remember { mutableStateOf(false) }
        var avatarResId by remember { mutableIntStateOf(R.drawable.whiteavatar) }
        val coroutineScope = rememberCoroutineScope()
        val client = remember { HttpClient(CIO) }
        val urlEmulator = "http://10.0.2.2:8080"
        val urlLocalHost = "http://localhost:8080"
        val context = LocalContext.current

        fun fetchStatus() {
            coroutineScope.launch {
                isLoading = true // Show loading spinner
                try {
                    val response: HttpResponse = client.get("$urlEmulator/status")
                    statusMessage = response.body()
                } catch (e: Exception) {
                    statusMessage = "Failed to fetch status"
                }
                showStatusDialog = true
                isLoading = false // Hide loading spinner
            }
        }

        fun connectToServer() {
            coroutineScope.launch {
                isLoading = true // Show loading spinner
                try {
                    val response: HttpResponse = client.get("$urlEmulator/connect")
                    statusMessage = response.body()
                    //After connecting, navigate to LobbyScreen
                    context.startActivity(Intent(context, LobbyActivity::class.java))
                } catch (e: Exception) {
                    statusMessage = "Failed to connect"
                }
                showStatusDialog = true
                isLoading = false // Hide loading spinner
            }
        }

        fun startGame() {
            coroutineScope.launch {
                isLoading = true
                try {
                    val response: io.ktor.client.statement.HttpResponse =
                        client.get("$urlEmulator/game/start") // FIXED URL
                    statusMessage = response.body()
                    val intent = Intent(context, GameActivity::class.java)
                    context.startActivity(intent)
                } catch (e: Exception) {
                    statusMessage = "Failed to start the game: ${e.localizedMessage}"
                    showStatusDialog = true
                }
                isLoading = false
            }
        }

        fun joinLobby(code: String) {
            coroutineScope.launch {
                isLoading = true
                try {
                    val encodedUsername = Uri.encode(username)
                    val response: HttpResponse = client.get("$urlEmulator/join-lobby/$code"){
                        parameter("name", encodedUsername)
                    }
                    val responseBodyText: String = response.body()
                    if (response.status.isSuccess() && responseBodyText.startsWith("Joined lobby", ignoreCase = true)){
                        val intent = Intent(context, LobbyActivity::class.java).apply {
                            putExtra("avatarResID", avatarResId)
                            putExtra("username", username)
                            putExtra("lobbyCode", code)
                        }
                        context.startActivity(intent)
                    }
          /*          val intent = Intent(context, LobbyActivity::class.java).apply {
                        putExtra("avatarResID", avatarResId)
                        putExtra("username", username)
                        putExtra("lobbyCode", code)
                    }
                    context.startActivity(intent)
                }*/
                else{
                    statusMessage = "Error ${response.status.value}(${response.status.description}): $responseBodyText"
                    showStartDialog = true}}
                     catch (e: Exception) {
                    statusMessage = "Failed to join lobby"
                }
                finally {

                isLoading = false
            }}
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.backgroundimage),
                contentDescription = "Background Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // EasterEgg, click three times on title and watch firework display
            val showFireworksCounter = remember { mutableIntStateOf(0) }
            if (showFireworksCounter.intValue >= 3) {
                FireworkLauncher(onAnimationEnd = {
                    showFireworksCounter.intValue = 0
                })
            }
            val titleModifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 50.dp)
                .clickable(
//                        interactionSource = remember { MutableInteractionSource() },
//                        indication = null
                ) {
                    showFireworksCounter.intValue += 1
                }
            Title(modifier = titleModifier)

            Column(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(bottom = 62.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                //Avatar Placeholder
                Box(modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.DarkGray)
                    .clickable { showAvatarDialog = true },
                    contentAlignment = Alignment.Center){
                    Image(painter = painterResource(id = avatarResId),
                        contentDescription = "Select Avatar",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop)
                }
                isUsernameError = !isValidUsername && username.isNotEmpty()
                androidx.compose.material3.TextField(
                    value = username,
                    onValueChange = {
                            newValue ->
                        val maxLength = 6
                        val allowedChars = "\"a-zA-Z0-9_\\-.,"
                        val regex = Regex("^[$allowedChars]+$")
                        username = newValue
                        if (newValue.length > maxLength){
                            isValidUsername = false
                            invalidUsernameMsg = "Max length: $maxLength"
                            }
                            else if (!newValue.matches(regex)){
                                    isValidUsername = false
                            isUsernameError = true
                            invalidUsernameMsg = "Use only letters, numbers, or _ - . ,"
                                }
                        else {
                            isValidUsername = true
                            isUsernameError = false
                            }
                        },
                    label = {
                        Text("Enter Username")
                    },
                    singleLine = true,
                    supportingText ={
                        if (isUsernameError){
                            Text(invalidUsernameMsg)
                        }
                    },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .padding(top = 16.dp, bottom = 24.dp)
                        .width(220.dp))

                Button(
                    onClick = { showJoinDialog = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2ecc71),
                        contentColor = Color.White
                    ),
                    border = BorderStroke(2.dp, Color.White),
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .width(200.dp)
                        .height(60.dp)
                ) {
                    Text(
                        text = "Join Lobby",
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp
                    )
                }

                Button(
                    onClick = {
                        if (username.isBlank()){
                          invalidUsernameMsg = "Username should not be empty!"
                          isUsernameError = true
                          return@Button
                        }
                        if (!isValidUsername){
                            isUsernameError = true
                            return@Button
                        }
                       coroutineScope.launch {
                           isLoading = true
                         try {
                                val response: HttpResponse = client.get("$urlEmulator/create-lobby")
                                    if (!response.status.isSuccess()){
                                        val errorBody: String = try {
                                            response.body()
                                        }
                                        catch (e: Exception){ "" }
                                        statusMessage = "Failed to create lobby: ${response.status.value}(${response.status.description}) - $errorBody"
                                        showStatusDialog = true
                                    }
                             else{
                                val createdCode: String = response.body()
                                println("Created lobby code: $createdCode")
                             val encodedUsername = Uri.encode(username)
                                //lobby beitreten
                                val joinResponse: HttpResponse = client.get("$urlEmulator/join-lobby/$createdCode"){
                                    parameter ("name", encodedUsername)
                                }
                                        if (!joinResponse.status.isSuccess()){
                                            val errorBodyJoin: String = try {
                                                joinResponse.body()}
                                            catch (e: Exception){""}
                                            statusMessage = "Failed to join lobby: ${joinResponse.status.value} (${joinResponse.status.description}) - $errorBodyJoin"
                                        showStatusDialog = true
                                        } else{
                               val joinResponseBody: String = joinResponse.body()

                                if (joinResponseBody.startsWith("Joined lobby" , ignoreCase = true)) {
                                    //Navigation zur LobbyActivity
                                val intent = Intent(context, LobbyActivity::class.java).apply {
                                    putExtra("lobbyCode", createdCode) // Übergabe des Lobby-Codes
                                    putExtra("username", username)
                                    putExtra("AvatarResId", avatarResId)
                                }
                                context.startActivity(intent)
                                } else{
                                    statusMessage = "Failed to join lobby: $joinResponseBody"
                                    showStatusDialog = true
                                }
                                        }
                                    }
                            } catch (e: Exception) {
                                println("Error creating lobby: ${e.localizedMessage}")
                                statusMessage = "Failed to create lobby: ${e.localizedMessage}"
                                showStatusDialog = true
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.DarkGray,
                        contentColor = Color.White
                    ),
                    border = BorderStroke(2.dp, Color.White),
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .width(200.dp)
                        .height(60.dp)
                ) {
                    Text(
                        text = "Create Lobby",
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp
                    )
                }


                Button(
                    onClick = { startGame() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.DarkGray,
                        contentColor = Color.White
                    ),
                    border = BorderStroke(2.dp, Color.White),
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .width(200.dp)
                        .height(60.dp)
                ) {
                    Text(text = "Start game",
                        textAlign = TextAlign.Center,
                        fontSize = 20.sp)
                }
                /*Button(
                    onClick = {
                        context.startActivity(
                            Intent(
                                context,
                                LobbyActivity::class.java
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth().height(60.dp)
                ) {
                    Text("Temporary: Go to lobby")
                }*/
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
                        avatarRes -> avatarResId = avatarRes
                    showAvatarDialog = false
                }
            )
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
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ){
                    avatarOptions.forEach {avatarRes ->
                        Image(painter = painterResource(id = avatarRes),
                            contentDescription = "Choose your player",
                            modifier = Modifier.size(50.dp).clip(CircleShape).clickable { onAvatarSelected(avatarRes) }.padding(4.dp),
                            contentScale = ContentScale.Crop)
                    }
                }
            },
            confirmButton = {
                Button(onClick = onDismiss) { Text("Cancel") }
            }
        )}

    @Preview(showBackground = true)
    @Composable
    fun StartMenuScreenPreview() {
        ClientTheme {
            StartMenuScreen()
        }
    }

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

@Composable
fun Title(modifier: Modifier = Modifier) {
    Text(
        text = "Hanabi!",
        fontFamily = FontFamily.Cursive,
        color = Color(0xFFF2FF90),
        fontSize = 100.sp,
        fontWeight = FontWeight.Bold,
        style = TextStyle(
            shadow = Shadow(
                color = Color.Black.copy(alpha = 100f),
                offset = Offset(-0f, 0f),
                blurRadius = 50f
            )
        ),
        modifier = modifier
    )
}
