package se2.hanabi.app.gamePlayUI

import android.content.res.Configuration
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import se2.hanabi.app.Services.WebSocketService
import se2.hanabi.app.model.Player
import se2.hanabi.app.ui.theme.customFont

const val maxGameBoardHeightProportion = 0.5f

/**
 * GamePlayUI displays screen that will be active in gameplay.
 * This includes:
 * - the cards from all players' hands
 * - fuse/hint tokens, discard/draw pile, color stacks
 *
 */

@Composable
fun GamePlayUI(viewModel: GamePlayViewModel) {
    val configuration = LocalConfiguration.current
    var windowWidthDp = with(LocalDensity.current) {LocalWindowInfo.current.containerSize.width.toDp()}
    var windowHeightDP = with(LocalDensity.current) {LocalWindowInfo.current.containerSize.height.toDp()}


    val landscape = when (configuration.orientation) { Configuration.ORIENTATION_LANDSCAPE -> true else -> false }

    var shrinkRatio = 1f
    var defaultCardWidth = windowWidthDp.times(CARD_PROPORTION_OF_WIDTH)
    var cardWidth: Dp
    var cardHeight: Dp
    if (landscape) {
        val gameBoardVertPaddingElementsSum = boardElementPadding.times(4)
        val availableVertSpace = windowHeightDP.times(maxGameBoardHeightProportion)-gameBoardVertPaddingElementsSum
        cardHeight = availableVertSpace.div(2)
        cardWidth = cardHeight.div(CARD_ASPECT_RATIO)
    } else { // portrait
        cardWidth = windowWidthDp.times(CARD_PROPORTION_OF_WIDTH)
        val gameBoardVertPaddingElementsSum = cardSpacing.times(4) + boardElementPadding.times(2)
        val gameBoardHeight = cardWidth.times(5) + gameBoardVertPaddingElementsSum
        if (gameBoardHeight.div(windowHeightDP) > maxGameBoardHeightProportion) {
            val availableVertSpace =
                windowHeightDP.times(maxGameBoardHeightProportion) - gameBoardVertPaddingElementsSum
            cardWidth = availableVertSpace.div(5)
        }
        cardHeight = cardWidth.times(CARD_ASPECT_RATIO)
    }

    shrinkRatio = cardWidth.div(defaultCardWidth)

    val cardSizeDp = DpSize(
        width = cardWidth,
        height = cardWidth.times(CARD_ASPECT_RATIO)
    )

    Box(modifier = Modifier
        .fillMaxSize()
        .background(
            Brush.verticalGradient(
                listOf(Color(0xFF282828), Color(0xFF000000))
            )
        ),
        contentAlignment = Alignment.Center
    ) {

        val players by viewModel.players.collectAsState()
        val statusMessage by viewModel.statusMessage.collectAsState()
        val connectionState by viewModel.connectionState.collectAsState()
        val isMyTurn by viewModel.isMyTurn.collectAsState()
        val gameOver by viewModel.gameOver.collectAsState()

        // Zeige Ladeanimation wenn noch keine Spieler geladen wurden
        if (players.isEmpty()) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color.White)
                statusMessage?.let {
                    Text(
                        text = it,
                        color = Color.White,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
                
                // Show reconnect button if disconnected
                if (connectionState == WebSocketService.ConnectionState.DISCONNECTED) {
                    androidx.compose.material3.Button(
                        onClick = { viewModel.reconnectWebSocket() },
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text("Reconnect")
                    }
                }
            }
        } else {
            // Show game board and player cards
            GameBoardUI(cardSizeDp = cardSizeDp, shrinkRatio = shrinkRatio)
            PlayersCardsUI(landscape, cardSizeDp)

            Column (modifier = Modifier.align(Alignment.TopCenter).padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally){

                Spacer(modifier = Modifier.height(12.dp))

                InGamePlayerList(players = players)
            }



            // Action buttons at the bottom
            val cardSelected = viewModel.selectedCardId.collectAsState().value >= 0
            if (isMyTurn && !gameOver && cardSelected) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = cardHeight.times(1.1f), start = 16.dp, end = 16.dp)
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val paddingAmount = cardSizeDp.width.times(hintPaddingToHintSelectorProportion)
                    ActionButton(
                        text = "PLAY",
                        width = cardWidth.times(2f),
                        paddingAmount = paddingAmount,
                        onClick = viewModel::onPlayCardClick
                    )
                    ActionButton(
                        text = "DISCARD",
                        width = cardWidth.times(2f),
                        paddingAmount = paddingAmount,
                        onClick = viewModel::onDiscardCardClick
                    )
                }
            }
        }
    }
}

@Composable
fun InGamePlayerList(players: List<Player>,){

    val rows = players.chunked(2)
    val viewModel: GamePlayViewModel = viewModel()
    val currentPlayerID = viewModel.currentPlayer.collectAsState().value

    Column (
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
            .fillMaxWidth(0.5f)
            .heightIn()
            .background(Color.DarkGray.copy(alpha = 0.20f), RoundedCornerShape(40.dp))
            .padding(2.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ){
        rows.forEach{rowPlayers ->
            Row (
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically)

            {
        rowPlayers.forEach{player ->
        Row (
            modifier = Modifier
                .weight(1f).padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically) {

            Box(modifier = Modifier.size(20.dp).clip(CircleShape).background(Color.DarkGray)){
                Image(painter = painterResource(id = player.avatarResID),
                    contentDescription = "${player.name} avatar",
                    modifier = Modifier.fillMaxSize())
            }

            Spacer(modifier = Modifier.width(5.dp))



            Text(text = player.name,
                color = if (player.id==currentPlayerID) Color.Green else Color.White,
                fontSize = 12.sp,
                maxLines = 1)
        }}
                if (rowPlayers.size == 1){
                    Spacer(modifier = Modifier.weight(1f))
                }
    }
}
    }}

@Composable
fun ActionButton(
    modifier: Modifier = Modifier,
    text: String = "",
    paddingAmount: Dp,
    onClick: () -> Unit = {},
    width: Dp
) {
    Box(
        modifier = modifier
            .width(width)
            .clip(RoundedCornerShape(paddingAmount))
            .background(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF282828), Color(0xFF282828).copy(alpha = 0.7f))
                )
            )
            .clickable() { onClick() }
            .padding(paddingAmount),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = (width.value / 5).sp,
            fontFamily = customFont,
            color = Color(0xFFF2FF90),
        )
    }
}