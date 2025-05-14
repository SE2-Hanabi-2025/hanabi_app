package se2.hanabi.app.screens

import android.content.Intent
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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import kotlinx.coroutines.launch
import se2.hanabi.app.EndAnimations.FireworkLauncher
import se2.hanabi.app.GameActivity
import se2.hanabi.app.lobby.LobbyActivity
import se2.hanabi.app.R
import se2.hanabi.app.ui.theme.ClientTheme
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlin.math.sin


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
        var invalidUsernameMsg by remember { mutableStateOf("Wrong username") }
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
                    val encodedName = URLEncoder.encode(username)
                    val response: HttpResponse = client.get("$urlEmulator/join-lobby/$code"){
                        parameter("name", encodedName)
                    }
                    val bodyText: String = response.body()
                    if (response.status.isSuccess() && bodyText.startsWith("Joined lobby", ignoreCase = true)){
                        Intent (context, LobbyActivity:: class.java).apply {
                            putExtra("lobbyCode", code)
                            putExtra("username", username)
                            putExtra("AvatarResID", avatarResId)
                    }.also ( context::startActivity )
                    //statusMessage = response.body()
                    //isConnected = true
                }
                else {
                    statusMessage = "Error ${response.status.value}(${response.status.description}): $bodyText"
                showStatusDialog = true
            }}
                catch (e: Exception) {
                    statusMessage = "Failed to join lobby ${e.localizedMessage}"
                    showStatusDialog = true
                }
                finally {
                isLoading = false
                }
                }
        }

        fun createLobbyAndJoin() {
            coroutineScope.launch {
                isLoading = true
                try {
                    val createResp: HttpResponse = client.get("$urlEmulator/create-lobby")
                    val createdCode: String = createResp.body()
                    val encodedName = URLEncoder.encode(username, StandardCharsets.UTF_8.toString())
                    val joinResp: HttpResponse = client.get("$urlEmulator/join-lobby/$createdCode?name=$encodedName")
                    val joinBody: String = joinResp.body()

                    if (joinBody.startsWith("Joined lobby", ignoreCase = true)) {
                            Intent(context, LobbyActivity::class.java).apply {
                            putExtra("lobbyCode", createdCode)
                            putExtra("username", username)
                            putExtra("avatarResID", avatarResId)
                        }.also (context::startActivity)
                    } else {
                        statusMessage = "Failed to join lobby: $joinBody"
                        showStatusDialog = true
                    }
                } catch (e: Exception) {
                    statusMessage = "Failed to create lobby ${e.localizedMessage}"
                    showStatusDialog = true
                } finally {
                    isLoading = false
                }
            }
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

                Spacer(Modifier.height(16.dp))

                        val maxLength = 6
                        val allowedChars = "a-zA-Z0-9,.!_;:?"
                        val regex = Regex("^[$allowedChars]*$")
                        OutlinedTextField(
                            value = username,
                            onValueChange = {
                                new -> username = new.take(maxLength)
                                isValidUsername = username.matches(regex)
                                invalidUsernameMsg = when{
                                    username.isEmpty() -> "Please enter username"
                                    !username.matches(regex) -> "Wrong input"
                                    else -> ""
                                }
                            },
                            label = {
                                Text("Username")},
                            isError = !isValidUsername,
                            singleLine = true)
                                if (!isValidUsername){
                                    Text(invalidUsernameMsg, color = MaterialTheme.colorScheme.error)
                                }

                        Spacer(Modifier.height(20.dp))

              //      shape = RoundedCornerShape(50),
                //    modifier = Modifier
                        //      .padding(top = 16.dp, bottom = 24.dp)
                      //  .width(220.dp))

                Button(
                    onClick = { if (isValidUsername) showJoinDialog = true },
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
                        if (isValidUsername) createLobbyAndJoin()
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
                        avatarResId = it
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
