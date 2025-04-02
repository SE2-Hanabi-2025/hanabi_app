package se2.hanabi.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import se2.hanabi.app.ui.theme.ClientTheme
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.client.engine.cio.*
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ClientTheme {
                AppContent()
            }
        }
    }

    @Composable
    fun AppContent() {
        var showConnectDialog by remember { mutableStateOf(false) }
        var showStartDialog by remember { mutableStateOf(false) }
        var showStatusDialog by remember { mutableStateOf(false) }
        var statusMessage by remember { mutableStateOf("Fetching status...") }
        var isLoading by remember { mutableStateOf(false) }
        val coroutineScope = rememberCoroutineScope()
        val client = remember { HttpClient(CIO) }
        val urlEmulator = "http://10.0.2.2:8080"
        val urlLocalHost = "http://localhost:8080"



        fun startGame() {
            coroutineScope.launch {
                isLoading = true
                try {
                    val response: HttpResponse = client.get("$urlEmulator/game/start") // FIXED URL
                    statusMessage = response.body()
                    startActivity(Intent(this@MainActivity, GameActivity::class.java))
                } catch (e: Exception) {
                    statusMessage = "Failed to start the game: ${e.localizedMessage}"
                    showStatusDialog = true
                }
                isLoading = false
            }
        }



        fun connectToServer() {
            coroutineScope.launch {
                isLoading = true // Show loading spinner
                try {
                    val response: HttpResponse = client.get("$urlEmulator/connect")
                    statusMessage = response.body()
                } catch (e: Exception) {
                    statusMessage = "Failed to connect"
                }
                showStatusDialog = true
                isLoading = false // Hide loading spinner
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = R.drawable.background_image),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // Center content
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Hanabi!",
                    fontFamily = FontFamily.Cursive,
                    color = Color(0xFFfbf7b5),
                    fontSize = 84.sp,
                    fontWeight = FontWeight.Bold,
                    style = TextStyle(
                        shadow = Shadow(
                            color = Color.Black.copy(alpha = 1f),
                            offset = Offset(0f, 0f),
                            blurRadius = 50f
                        )
                    ),
                    textAlign = TextAlign.Center
                )
            }

            // Bottom buttons
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(0.dp, 100.dp)
                    .offset(x = (-70).dp, y = (30).dp)
                    .padding(15.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { connectToServer() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2ecc71),
                        contentColor = Color.White
                    ),
                    border = BorderStroke(8.dp, Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    enabled = !isLoading // Disable button when loading
                ) {
                    Text("Connect to Server", color = Color.White)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { startGame() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.DarkGray,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                        .border(5.dp, Color.White, shape = RoundedCornerShape(64.dp)),
                    enabled = !isLoading // Disable button when loading
                ) {
                    Text("Start Game", color = Color.White)
                }

                Spacer(modifier = Modifier.height(16.dp))

            }

            // Show Loading Spinner
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f), shape = RoundedCornerShape(16.dp))
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

    @Preview(showBackground = true)
    @Composable
    fun DefaultPreview() {
        ClientTheme {
            AppContent()
        }
    }
}
