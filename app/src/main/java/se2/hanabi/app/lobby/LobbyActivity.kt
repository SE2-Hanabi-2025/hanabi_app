package se2.hanabi.app.lobby

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import se2.hanabi.app.R
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import se2.hanabi.app.GameActivity
import se2.hanabi.app.ui.theme.ClientTheme

class LobbyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent{
            ClientTheme {
                LobbyScreen(
                    //TODO: add dynamic data
                    playerList = listOf("Player1","Player2","Player3", "Player4", "Player5"),
                    onLeaveLobby = { finish()},
                    onStartGame = {navigateToGame()}
                )
            }
        }
    }
    private fun navigateToGame(){
        val intent = Intent(this, GameActivity::class.java)
        startActivity(intent)
    }
}

@Composable
fun LobbyScreen (playerList: List<String>,
                 onLeaveLobby: () -> Unit,
                 onStartGame: () -> Unit){
    Box(modifier = Modifier.fillMaxSize()){
        Image(painter = painterResource(id = R.drawable.lobbyscreen_bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop)

    //TODO: change this I dont like it
        Box(modifier = Modifier.fillMaxWidth().height(56.dp).background(Color.DarkGray), contentAlignment = Alignment.TopCenter)
               { Text (
            text = "Lobby",
            color = Color.White
        )}

                //Player list Placeholder
                Box(modifier = Modifier.fillMaxWidth(0.8f).heightIn(max = 400.dp).offset(y = 250.dp, x = 42.dp).clip(
                    RoundedCornerShape(40.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 30.dp, vertical = 10.dp))
                {
                 LazyColumn (
                 modifier = Modifier.fillMaxWidth()
              ){
                    items(playerList) { player ->
                        Row (modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(20.dp))
                        {
                            Box(
                                modifier = Modifier.size(40.dp).clip(CircleShape).background(Color.DarkGray)
                            )

                            Text(
                            text = player,
                            color = Color.White,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Start
                        )
              }}
            }
           }
        //Buttons
    Column (modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally){

        //start game
        Button(
            onClick = onStartGame,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Green),
            border = BorderStroke(2.dp, Color.White),
            modifier = Modifier.width(200.dp).height(60.dp)
        ) {
            Text("Start Game", color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        //leave Lobby
        Button(
            onClick = onLeaveLobby,
            colors = ButtonDefaults.buttonColors(
           containerColor = Color.DarkGray),
            border = BorderStroke(2.dp, Color.White),
            modifier = Modifier.width(200.dp).height(60.dp)
        ) {
            Text("Leave Lobby", color = Color.White)
        }

    }
}
}

@Preview(showBackground = true)
@Composable
fun LobbyScreenPreview(){
    ClientTheme {
        LobbyScreen(
        playerList = listOf("Player 1", "Player 2", "Player 3", "Player 4", "Player 5"),
        onLeaveLobby = {},
            onStartGame = {}
    ) }
}
