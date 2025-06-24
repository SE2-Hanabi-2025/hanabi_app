package se2.hanabi.app.lobby

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.set
import androidx.lifecycle.lifecycleScope
import com.google.zxing.BarcodeFormat
import com.google.zxing.common.BitMatrix
import com.google.zxing.qrcode.QRCodeWriter
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.launch
import se2.hanabi.app.R
import se2.hanabi.app.Services.MusicService
import se2.hanabi.app.activities.GameActivity
import se2.hanabi.app.ui.components.MuteButton
import se2.hanabi.app.ui.theme.ClientTheme
import se2.hanabi.app.ui.theme.customFont
import se2.hanabi.app.utils.ServerAddressManager
import android.graphics.Color as AndroidColor

class LobbyActivity : ComponentActivity() {

    private val viewModel: LobbyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val receivedLobbyCode = intent.getStringExtra("lobbyCode") ?: "Kein Code"
        val receivedPlayerId = intent.getIntExtra("playerId", -1)
        val isHost = intent.getBooleanExtra("isHost", false)
        val username = intent.getStringExtra("username") ?: ""

        viewModel.setLobbyCode(receivedLobbyCode)
        viewModel.setPlayerId(receivedPlayerId)
        viewModel.setIsHost(isHost)
        viewModel.setUsername(username)
        viewModel.startFetchPlayersAndStartSync()


        setContent {
            ClientTheme {
                val players by viewModel.players.collectAsState()
                val lobbyCode = viewModel.lobbyCode
                val isHostState = viewModel.isHost
                val isGameStarted by viewModel.isGameStarted.collectAsState()

                LaunchedEffect(isGameStarted) {
                    if (isGameStarted) {
                        lobbyCode?.let { lc ->
                            val currentPlayerId = viewModel.getPlayerId()
                            if (currentPlayerId != null) {
                                navigateToGame(lc, currentPlayerId)
                            }
                        }
                    }
                }

                LobbyScreen(
                    playerList = players,
                    lobbyCode = lobbyCode,
                    isHost = isHostState,
                    onLeaveLobby = {
                        val lobbyc = viewModel.lobbyCode
                        val playerid = viewModel.getPlayerId()
                            if ( lobbyc != null && playerid!= null && playerid !=-1){
                                leaveLobbyRequest(lobbyc, playerid){
                                    finish()
                                }
                            } else {
                        finish()
            }},
                    onStartGame = { lobbyCode?.let { startGameRequest(it) } },
                )
            }
        }
    }

    private fun navigateToGame(lobbyId: String, playerId: Int) {
        val intent = Intent(this, GameActivity::class.java).apply {
            putExtra("lobbyId", lobbyId)
            putExtra("playerId", playerId)
        }
        
        startActivity(intent)
    }

    private fun generateQRCode(lobbyCode: String, size: Int): ImageBitmap? {
        return try {
            val writer = QRCodeWriter()
            val bitMatrix: BitMatrix = writer.encode(lobbyCode, BarcodeFormat.QR_CODE, size, size)
            val bitmap = createBitmap(size, size, Bitmap.Config.RGB_565)
            
            for (x in 0 until size) {
                for (y in 0 until size) {
                    bitmap[x, y] = if (bitMatrix[x, y]) AndroidColor.BLACK else AndroidColor.WHITE
                }
            }
            
            bitmap.asImageBitmap()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @Composable
    fun QRCodeDialog(lobbyCode: String, onDismiss: () -> Unit) {
        val qrCodeBitmap = remember { generateQRCode(lobbyCode, 300) }
        
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Scan to Join Lobby") },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Lobby Code: $lobbyCode", fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    qrCodeBitmap?.let {
                        Image(
                            bitmap = it,
                            contentDescription = "QR Code",
                            modifier = Modifier
                                .size(250.dp)
                                .background(Color.White)
                                .padding(8.dp)
                        )
                    } ?: Text("Could not generate QR code")
                }
            },
            confirmButton = {
                Button(onClick = onDismiss) {
                    Text("Close")
                }
            }
        )
    }

    private fun startGameRequest(lobbyCode: String) {
        lifecycleScope.launch {
            try {
                val response: HttpResponse = HttpClient(CIO).get(ServerAddressManager.getStartGameUrl(lobbyCode)) {
                        parameter("isCasualMode", viewModel.isCasualMode.value)
                    }
                if (response.status == HttpStatusCode.OK) {
                    viewModel.setIsGameStarted(true)
                }
            } catch (e: Exception) {
            }
        }
    }

    private fun leaveLobbyRequest(lobbyCode: String, playerId: Int, onComplete: () -> Unit){
        lifecycleScope.launch {
            try {
                val client = HttpClient(CIO)
                val response: HttpResponse = client.get(ServerAddressManager.getLeaveLobbyUrl(lobbyCode, playerId))

                if (response.status == HttpStatusCode.OK){
                    println("Leaving lobby server notification")
                } else {
                    println("Failed to nofity")
                }
            } catch (e: Exception){
                e.printStackTrace()
            } finally {
                onComplete()
            }
        }
    }

    @Composable
    fun LobbyScreen(
        playerList: List<PlayerInLobby>,

        lobbyCode: String?,
        onLeaveLobby: () -> Unit,
        onStartGame: () -> Unit,
        isHost: Boolean,
    ) {
        var showQRCodeDialog by remember { mutableStateOf(false) }
        Box(modifier = Modifier.fillMaxSize()) {
            val context = LocalContext.current
            val prefs = context.getSharedPreferences("hanabi_prefs", MODE_PRIVATE)
            val isMuted = remember { mutableStateOf(prefs.getBoolean("isMuted", false)) }
            Image(
                painter = painterResource(id = R.drawable.lobbyscreen_bg),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(60.dp)
                    .offset(x = 33.dp, y = 100.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            )
            {
                Text(
                    text = lobbyCode?.let { "Lobby Code: $it" } ?: "Loading...",
                    color = Color.White,
                    fontSize = 22.sp
                )
            }
            //Player list Placeholder
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .heightIn(max = 400.dp)
                    .offset(y = 200.dp, x = 55.dp)
                    .clip(RoundedCornerShape(40.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 30.dp, vertical = 50.dp)
            )
            {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(playerList) { player ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(30.dp)
                        )
                        {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(Color.DarkGray)
                            ){
                                Image( painter = painterResource(id = player.avatarResID),
                                        contentDescription = "Avatar",
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }

                            Text(
                                text = player.name,
                                color = Color.White,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Start,
                                fontSize = 20.sp
                            )
                        }
                    }
                }
            }

            //Buttons
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 62.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val buttonSpacing = 18.dp

                //start game
                if (isHost) {
                    val wiggleAnim = rememberInfiniteTransition(label = "wiggle").animateFloat(
                        initialValue = -2f,
                        targetValue = 2f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 600, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ), label = "wiggle"
                    )
                    Text(
                        text = "START GAME",
                        modifier = Modifier
                            .clickable { onStartGame() }
                            .padding(vertical = 16.dp)
                            .fillMaxWidth()
                            .graphicsLayer {
                                rotationZ = wiggleAnim.value
                            },
                        textAlign = TextAlign.Center,
                        fontSize = 35.sp,
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
                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // Leave Lobby, QR Code, and game mode buttons in a row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(buttonSpacing),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val wiggleAnimLeave = rememberInfiniteTransition(label = "wiggleLeave").animateFloat(
                        initialValue = 2f,
                        targetValue = -2f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 600, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ), label = "wiggleLeave"
                    )
                    Text(
                        text = "LEAVE",
                        modifier = Modifier
                            .clickable { onLeaveLobby() }
                            .padding(vertical = 16.dp)
                            .graphicsLayer {
                                rotationZ = wiggleAnimLeave.value
                            },
                        textAlign = TextAlign.Center,
                        fontSize = 26.sp, 
                        color = Color.Green,
                        fontFamily = customFont,
                        style = TextStyle(
                            shadow = Shadow(
                                color = Color.Black.copy(alpha = 0.5f),
                                offset = Offset(2f, 4f),
                                blurRadius = 8f
                            )
                        )
                    )
                    val wiggleAnimQR = rememberInfiniteTransition(label = "wiggleQR").animateFloat(
                        initialValue = -2f,
                        targetValue = 2f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(durationMillis = 600, easing = LinearEasing),
                            repeatMode = RepeatMode.Reverse
                        ), label = "wiggleQR"
                    )
                    Box(
                        modifier = Modifier
                            .graphicsLayer {
                                rotationZ = wiggleAnimQR.value
                            }
                            .background(Color.Transparent, shape = RoundedCornerShape(8.dp))
                            .border(2.dp, Color(0xFFFCAE21), shape = RoundedCornerShape(8.dp))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Q R",
                            modifier = Modifier
                                .clickable { showQRCodeDialog = true },
                            textAlign = TextAlign.Center,
                            fontSize = 45.sp,
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
                    if (isHost) {
                        val checked = viewModel.isCasualMode.collectAsState().value
                        val gameModeLabel = if (checked) "Casual" else "Normal"
                        val textColor = if (checked) Color.White else Color(0xFFFCAE21)
                        val wiggleAnimMode = rememberInfiniteTransition(label = "wiggleMode").animateFloat(
                            initialValue = 2f,
                            targetValue = -2f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(durationMillis = 600, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ), label = "wiggleMode"
                        )
                        Text(
                            text = gameModeLabel.uppercase(),
                            modifier = Modifier
                                .clickable { viewModel.onGameModeToggle() }
                                .padding(vertical = 16.dp)
                                .graphicsLayer {
                                    rotationZ = wiggleAnimMode.value
                                },
                            textAlign = TextAlign.Center,
                            fontSize = 26.sp, 
                            color = textColor,
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
                }
            }

            // Mute button: top right, white, large, shadow, zIndex
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
        
        // QR Code Dialog
        if (showQRCodeDialog && lobbyCode != null) {
            QRCodeDialog(
                lobbyCode = lobbyCode,
                onDismiss = { showQRCodeDialog = false }
            )
        }
    }
}