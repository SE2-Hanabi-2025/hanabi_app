package se2.hanabi.app.lobby

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp


@Composable

fun LobbyPlayerItem(name: String) {
    Column (modifier = Modifier.fillMaxWidth().padding(4.dp))
    { Text(text = name,
        style = MaterialTheme.typography.bodyLarge)
    HorizontalDivider()
    }
}