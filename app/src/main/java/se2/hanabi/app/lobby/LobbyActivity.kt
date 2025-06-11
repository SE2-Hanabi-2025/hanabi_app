package se2.hanabi.app.lobby

import androidx.activity.viewModels
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import se2.hanabi.app.R
import androidx.compose.ui.unit.sp
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.ktor.http.HttpStatusCode
import androidx.lifecycle.lifecycleScope
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlinx.coroutines.launch
import se2.hanabi.app.activities.GameActivity
import se2.hanabi.app.ui.theme.ClientTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import android.graphics.Bitmap
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchColors
import androidx.compose.material3.SwitchDefaults
import androidx.compose.ui.draw.rotate
import android.graphics.Color as AndroidColor
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.common.BitMatrix
import androidx.core.graphics.set
import androidx.core.graphics.createBitmap

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
        viewModel.startPlayerSync()


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
                val response: HttpResponse =
                    HttpClient(CIO).get("http://10.0.2.2:8080/start-game/$lobbyCode") {
                        parameter("isCasualMode", viewModel.isCasualMode.value)
                    }
                if (response.status == HttpStatusCode.OK) {
                    // Assuming viewModel.getPlayerId() returns the current player's ID
                    val currentPlayerId = viewModel.getPlayerId() // Placeholder for actual player ID retrieval
                    if (currentPlayerId != null) {
                        navigateToGame(lobbyCode, currentPlayerId)
                    }
                }
            } catch (e: Exception) {
            }
        }
    }

    private fun leaveLobbyRequest(lobbyCode: String, playerId: Int, onComplete: () -> Unit){
        lifecycleScope.launch {
            try {
                val client = HttpClient(CIO)
                val response: HttpResponse = client.get("http://10.0.2.2:8080/leave-lobby/$lobbyCode/$playerId")

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
        //avatarResID: Int,
        //username: String
    ) {
        var showQRCodeDialog by remember { mutableStateOf(false) }
        
        Box(modifier = Modifier.fillMaxSize()) {
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

                //start game
                if (isHost) {
                    Button(
                        onClick = { onStartGame() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2ecc71),
                            contentColor = Color.White
                        ),
                        border = BorderStroke(2.dp, Color.White),
                        modifier = Modifier.width(200.dp).height(60.dp)
                    ) {
                        Text("Start Game", color = Color.White, fontSize = 20.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }
                
                // QR Code and Leave Lobby buttons in a row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isHost) {
                        Box(
                            modifier = Modifier
                                .height(60.dp),
                                /*.clip(RoundedCornerShape(30.dp))
                                .background(Color.Green)

                                .border(border = BorderStroke(2.dp, Color.White)),*/

                            contentAlignment = Alignment.Center
                        ) {
                            val checked = viewModel.isCasualMode.collectAsState().value
                            Switch(
                                modifier = Modifier.fillMaxHeight(),

                                checked = checked,
                                onCheckedChange = { newCheckedState ->
                                    viewModel.onGameModeToggle(newCheckedState)
                                },
                            )
                            val switchLabel = if (checked) "Casual" else "Normal"
                            Text(
                                text = switchLabel,
                                fontSize = 16.sp,
                                color = Color.White,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // QR Code Button
                    Button(
                        onClick = { showQRCodeDialog = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF3498db)
                        ),
                        border = BorderStroke(2.dp, Color.White),
                        modifier = Modifier.width(95.dp).height(60.dp)
                    ) {
                        Text("QR Code", color = Color.White, fontSize = 16.sp)
                    }
                    
                    // Leave Lobby Button
                    Button(
                        onClick = onLeaveLobby,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.DarkGray
                        ),
                        border = BorderStroke(2.dp, Color.White),
                        modifier = Modifier.width(95.dp).height(60.dp)
                    ) {
                        Text("Leave", color = Color.White, fontSize = 16.sp)
                    }
                }
            }
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