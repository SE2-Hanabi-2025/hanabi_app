package se2.hanabi.app.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable

@Composable
fun MuteButton(isMuted: Boolean, onToggle: () -> Unit) {
    IconButton(onClick = onToggle) {
        Icon(
            imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
            contentDescription = if (isMuted) "Unmute" else "Mute"
        )
    }
}
