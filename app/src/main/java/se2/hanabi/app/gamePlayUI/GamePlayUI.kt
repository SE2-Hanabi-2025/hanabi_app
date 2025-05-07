package se2.hanabi.app.gamePlayUI

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
//import se2.hanabi.app.screens.Title

// eventually to be linked to Color Enum in backend
val colors = listOf("red","green","yellow","blue","white")

/**
 * GamePlayUI displays screen that will be active in gameplay.
 * This includes:
 * - the cards from all players' hands
 * - fuse/hint tokens, discard/draw pile, color stacks
 * - game title at top
 */
@Composable
fun GamePlayUI() {
    Box(modifier = Modifier
        .fillMaxSize()
        .background(
            Brush.verticalGradient(
                listOf(Color(0xFF282828), Color(0xFF000000))
            )
        ),
        contentAlignment = Alignment.Center
    ) {
        val titleModifier = Modifier
            .align(Alignment.TopCenter)
            .padding(top = 20.dp)
      //  Title(modifier = titleModifier)
        GameBoardUI()
        PlayersCardsUI()
    }
}
