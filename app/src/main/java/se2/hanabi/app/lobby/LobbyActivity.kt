package se2.hanabi.app.lobby

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import se2.hanabi.app.ui.theme.ClientTheme
import se2.hanabi.app.lobby.LobbyPlayerItem

class LobbyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent{
            ClientTheme {
                LobbyScreen(
                    //TODO: add dynamic data
                    playerList = listOf("Player1","Player2","Player3", "Player4", "Player5"),
                    onLeaveLobby = { finish()}
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LobbyScreen (playerList: List<String>, onLeaveLobby: () -> Unit){
    Scaffold (
        topBar = {
            TopAppBar(title = { Text("Lobby")})
        },
        content = { padding ->
            Column (
                modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween,
                horizontalAlignment = Alignment.CenterHorizontally){

                //Player list Placeholder

                LazyColumn (
                    modifier = Modifier.fillMaxWidth().weight(1f)
                ){
                    items(playerList) { player ->
                        LobbyPlayerItem (name = player)
                }
            }

                //leave Lobby button

        Button(
            onClick = onLeaveLobby,
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Leave Lobby")
        }
            }
}
    )
}