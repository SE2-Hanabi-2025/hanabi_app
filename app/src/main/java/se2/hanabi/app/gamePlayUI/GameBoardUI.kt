package se2.hanabi.app.gamePlayUI

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import se2.hanabi.app.model.Card
import se2.hanabi.app.ui.theme.customFont

val cardSpacing = 5.dp
val boardElementPadding = 10.dp

/**
 * GameBoardUI display the features of the game board.
 * These include: fuse/hint tokens, discard/draw pile, and color stacks
 *
 */
@Composable
fun GameBoardUI(
    cardSizeDp: DpSize,
    shrinkRatio: Float
) {
    val viewModel: GamePlayViewModel = viewModel()

    val tokenAreaHeight = ( cardSizeDp.width.times(3) + cardSpacing.times(3) - boardElementPadding.times(2) ).div(2)
    val landscape = when (LocalConfiguration.current.orientation) { Configuration.ORIENTATION_LANDSCAPE -> true else -> false }

    Row(
        modifier = Modifier
            .graphicsLayer {
                rotationZ = when (landscape) {true -> 90f else -> 0f}
            }
            .clip(RoundedCornerShape(boardElementPadding))
            .background(Color(0xFF566290).copy(alpha = 0.5f))
            .padding(boardElementPadding),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(boardElementPadding)
    ) {
        //left column
        Column(
            verticalArrangement = Arrangement.spacedBy(boardElementPadding)
        ) {
            FuseTokens(
                cardSizeDp = cardSizeDp,
                tokenAreaHeight = tokenAreaHeight,
                numRemaining = viewModel.numRemainingFuseTokens.collectAsState().value)
            Column(
                verticalArrangement = Arrangement.spacedBy(cardSpacing)
            ) {
                RemainingCardsStack(
                    cardSizeDp = cardSizeDp,
                    shrinkRatio = shrinkRatio,
                    landscape = landscape,
                    numRemainingCards = viewModel.numRemainingCard.collectAsState().value)
                DiscardedCardsStack(
                    cardSizeDp = cardSizeDp,
                    lastDiscardedCard = viewModel.lastDiscardedCard.collectAsState().value,
                    onClick = viewModel::onDiscardCardClick
                )
            }
            HintTokens(
                cardSizeDp = cardSizeDp,
                tokenAreaHeight = tokenAreaHeight,
                numRemaining = viewModel.numRemainingHintTokens.collectAsState().value)
        }
        // right column
        ColorStacks(
            cardSizeDp = cardSizeDp,
            cardSpacing = cardSpacing,
            stackValues = viewModel.stackValues.collectAsState().value,
            onColorStackClick = viewModel::onColorStackClick,
        )
    }
}

@Composable
fun HintTokens(
    cardSizeDp: DpSize,
    numRemaining: Int,
    tokenAreaHeight: Dp
) {
    val totalNumTokens = 8
    Column(modifier = Modifier
        .size(cardSizeDp.height, tokenAreaHeight),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(){ // tokens 7,8 on top row
            for (tokenIndex in 7..8) {
                Token(
                    cardSizeDp = cardSizeDp,
                    type = TokenType.hint,
                    isFlipped = tokenIndex > numRemaining
                )
            }
        }
        Row(){ // tokens 4,5,6 on mid row
            for (tokenIndex in 4..6) {
                Token(
                    cardSizeDp = cardSizeDp,
                    type = TokenType.hint,
                    isFlipped = tokenIndex > numRemaining
                )
            }
        }
        Row(){ // tokens 1,2,3 on bottom row
            for (tokenIndex in 1..3) {
                Token(
                    cardSizeDp = cardSizeDp,
                    type = TokenType.hint,
                    isFlipped = tokenIndex > numRemaining
                )
            }
        }
    }
}

@Composable
fun FuseTokens(
    cardSizeDp: DpSize,
    numRemaining: Int,
    tokenAreaHeight: Dp
) {
    val totalNumTokens = 3
    Column(modifier = Modifier
        .size(cardSizeDp.height, tokenAreaHeight),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Token( // fuse token num 3 on top row
            cardSizeDp = cardSizeDp,
            type = TokenType.fuse,
            isFlipped = 3 > numRemaining
        )
        Row() { // fuse token num 1 and 2 on bottom row
            for (tokenIndex in 1..2) {
                Token(
                    cardSizeDp = cardSizeDp,
                    type = TokenType.fuse,
                    isFlipped = tokenIndex > numRemaining
                )
            }
        }
    }
}

@Composable
fun RemainingCardsStack(
    cardSizeDp: DpSize,
    shrinkRatio: Float,
    landscape: Boolean,
    modifier: Modifier = Modifier,
    numRemainingCards: Int
) {
    Box(contentAlignment = Alignment.Center ) {
        if (numRemainingCards==0) {
            EmptyStack(
                cardSizeDp = cardSizeDp,
            )
        } else {
            CardItem(
                cardSizeDp = cardSizeDp,
                card = Card(Card.Color.RED, 1),
                isFlipped = true,
                isPortrait = false,
            )
        }
        Text(
            modifier = Modifier
                .alpha(if (numRemainingCards==0) 0.5f else 1f)
                .graphicsLayer {
                    rotationZ = if (landscape) -90f else 0f
                },
            text = "$numRemainingCards",
            fontFamily = customFont,
            color = Color(0xFFF2FF90),
            fontSize = (40f*shrinkRatio).sp,
            fontWeight = FontWeight.Bold,
            style = TextStyle(
                shadow = Shadow(
                    color = Color.Black.copy(alpha = 0.5f),
                    blurRadius = 50f
                )
            )
        )
    }
}

@Composable
fun DiscardedCardsStack(
    cardSizeDp: DpSize,
    lastDiscardedCard: Card?,
    onClick: () -> Unit
) {
    if (lastDiscardedCard == null) {
        EmptyStack(
            cardSizeDp = cardSizeDp
        )
    } else {
        CardItem(
            cardSizeDp = cardSizeDp,
            card = Card(lastDiscardedCard.color,lastDiscardedCard.value),
            isFlipped = false,
            isPortrait = false,
            onClick = onClick
        )
    }
}


@Composable
fun EmptyStack(
    cardSizeDp: DpSize,
    modifier: Modifier = Modifier,
    isPortrait: Boolean = false,
    onClick: () -> Unit = {},
    color: Card.Color = Card.Color.WHITE
) {
    CardItem(
        cardSizeDp = cardSizeDp,
        modifier = Modifier.alpha(0.3f),
        card = Card(color,1),
        isFlipped = true,
        isPortrait = isPortrait,
        onClick = onClick,
        colorHint = color
    )
}

@Composable
fun ColorStacks(
    cardSizeDp: DpSize,
    cardSpacing: Dp,
    modifier: Modifier = Modifier,
    stackValues: Map<Card.Color, Int>,
    onColorStackClick: (Card.Color) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(cardSpacing),
    ) {
        Card.Color.entries.forEach() { color ->
            if (stackValues[color]==0) {
                EmptyStack(
                    cardSizeDp = cardSizeDp,
                    isPortrait = false,
                    onClick = { onColorStackClick(color) },
                    color = color,
                )
            } else {
                CardItem(
                    cardSizeDp = cardSizeDp,
                    card = Card(color,
                    stackValues[color]?:0),
                    isPortrait = false,
                    highlightColor = colorFromColorEnum(color),
                    onClick = { onColorStackClick(color) },
                )
            }
        }
    }
}

fun colorFromColorEnum(colorIn: Card.Color): Color {
    return when (colorIn) {
        Card.Color.RED -> Color.Red
        Card.Color.GREEN -> Color.Green
        Card.Color.YELLOW -> Color.Yellow
        Card.Color.BLUE -> Color.Cyan
        Card.Color.WHITE -> Color.White
//        else -> Color.White // White by default
    }
}

