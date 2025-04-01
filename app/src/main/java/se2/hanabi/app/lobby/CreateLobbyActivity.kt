package se2.hanabi.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import se2.hanabi.app.ui.theme.AppTheme

class CreateLobbyActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CreateLobbyScreen()
                }
            }
        }
    }
}

@Composable
fun CreateLobbyScreen() {
    val lobbyCode = remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = { lobbyCode.value = generateLobbyCode() }) {
                Text("Generate Lobby Code")
            }
            Text(text = "Lobby Code: ${lobbyCode.value}", modifier = Modifier.padding(top = 16.dp))
        }
    }
}

fun generateLobbyCode(): String {
    // Beispielhafte Implementierung zur Generierung eines Lobby-Codes
    return (100000..999999).random().toString()
}
