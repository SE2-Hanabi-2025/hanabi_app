package se2.hanabi.app.gamePlayUI

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.lifecycle.viewmodel.compose.viewModel
import se2.hanabi.app.R
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

    // Restore original tokenAreaHeight calculation (based on width, not height)
    val tokenAreaHeight = (cardSizeDp.width * 3 + cardSpacing * 3 - boardElementPadding * 2) / 2
    val landscape = when (LocalConfiguration.current.orientation) { Configuration.ORIENTATION_LANDSCAPE -> true else -> false }

    Image(
        painter = painterResource(id = R.drawable.lobbyscreen_bg),
        contentDescription = "Background Image",
        modifier = Modifier.fillMaxSize().alpha(0.8f),
        contentScale = ContentScale.Crop
    )

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
                cardHeight = cardSizeDp.height,
                cardSizeDp = cardSizeDp,
                tokenAreaHeight = tokenAreaHeight,
                numRemaining = viewModel.numRemainingFuseTokens.collectAsState().value
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(cardSpacing)
            ) {
                RemainingCardsStack(
                    cardSizeDp = cardSizeDp,
                    shrinkRatio = shrinkRatio,
                    landscape = landscape,
                    numRemainingCards = viewModel.numRemainingCard.collectAsState().value,
                    modifier = Modifier.onGloballyPositioned { coordinates ->
                        val position = coordinates.localToWindow(androidx.compose.ui.geometry.Offset.Zero)
                        val size = coordinates.size.toSize()
                        android.util.Log.d("HanabiGameBoardUI", "RemainingCardsStack onGloballyPositioned: position=$position, size=$size")
                    }
                )
                DiscardedCardsStack(
                    cardSizeDp = cardSizeDp,
                    lastDiscardedCard = viewModel.lastDiscardedCard.collectAsState().value,
                    onClick = viewModel::onDiscardCardClick,
                    // Drag-and-drop: update discard zone bounds
                    onGloballyPositioned = { coordinates ->
                        val position = coordinates.localToWindow(androidx.compose.ui.geometry.Offset.Zero)
                        val size = coordinates.size.toSize()
                        android.util.Log.d("HanabiGameBoardUI", "DiscardedCardsStack onGloballyPositioned: position=$position, size=$size")
                        viewModel.updateDiscardZoneBounds(androidx.compose.ui.geometry.Rect(position, size))
                    },
                    onDragEnd = { draggedCardId ->
                        android.util.Log.d("HanabiGameBoardUI", "DiscardedCardsStack onDragEnd: draggedCardId=$draggedCardId")
                        if (draggedCardId != null) {
                            viewModel.tryDropOnDiscard(draggedCardId)
                        }
                    }
                )
            }
            HintTokens(
                cardHeight = cardSizeDp.height,
                cardSizeDp = cardSizeDp,
                tokenAreaHeight = tokenAreaHeight,
                numRemaining = viewModel.numRemainingHintTokens.collectAsState().value
            )
        }
        // right column
        ColorStacks(
            cardSizeDp = cardSizeDp,
            cardSpacing = cardSpacing,
            stackValues = viewModel.stackValues.collectAsState().value,
            onColorStackClick = viewModel::onColorStackClick,
            // Drag-and-drop: update color stack bounds and handle drop
            onStackPositioned = { color, coordinates ->
                val position = coordinates.localToWindow(androidx.compose.ui.geometry.Offset.Zero)
                val size = coordinates.size.toSize()
                android.util.Log.d("HanabiGameBoardUI", "ColorStacks onStackPositioned: color=$color, position=$position, size=$size")
                viewModel.updateColorStackBounds(color, androidx.compose.ui.geometry.Rect(position, size))
            },
            onDragEnd = { draggedCardId, color ->
                android.util.Log.d("HanabiGameBoardUI", "ColorStacks onDragEnd: draggedCardId=$draggedCardId, color=$color")
                if (draggedCardId != null) {
                    viewModel.tryDropOnColorStack(draggedCardId, color)
                }
            }
        )
    }
}

@Composable
fun HintTokens(
    cardHeight: Dp,
    tokenAreaHeight: Dp,
    numRemaining: Int,
    cardSizeDp: DpSize
) {
    val totalNumTokens = 8
    Column(modifier = Modifier
        .size(cardHeight, tokenAreaHeight),
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
    cardHeight: Dp,
    tokenAreaHeight: Dp,
    numRemaining: Int,
    cardSizeDp: DpSize
) {
    val totalNumTokens = 3
    Column(modifier = Modifier
        .size(cardHeight, tokenAreaHeight),
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
    onClick: () -> Unit,
    onGloballyPositioned: ((androidx.compose.ui.layout.LayoutCoordinates) -> Unit)? = null,
    onDragEnd: ((Int?) -> Unit)? = null
) {
    val viewModel: GamePlayViewModel = viewModel()
    val draggedCardId by viewModel.draggedCardId.collectAsState()
    val stackModifier = Modifier
        .size(cardSizeDp.height, cardSizeDp.width)
        .then(
            if (onGloballyPositioned != null) Modifier.onGloballyPositioned {
                android.util.Log.d("HanabiGameBoardUI", "DiscardedCardsStack onGloballyPositioned callback called!")
                onGloballyPositioned(it)
            } else Modifier
        )
        .then(
            if (onDragEnd != null) Modifier.pointerInput(draggedCardId) {
                detectDragGestures(
                    onDragStart = {},
                    onDragEnd = { onDragEnd(draggedCardId) },
                    onDragCancel = {},
                    onDrag = { _, _ -> }
                )
            } else Modifier
        )
    if (lastDiscardedCard == null) {
        EmptyStack(
            cardSizeDp = cardSizeDp, 
            onClick = onClick,
            modifier = stackModifier

        )
    } else {
        CardItem(
            cardSizeDp = cardSizeDp,
            card = Card(lastDiscardedCard.getColor(),lastDiscardedCard.getValue()),
            isFlipped = false,
            isPortrait = false,
            onClick = onClick,
            modifier = stackModifier
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
    android.util.Log.d("HanabiGamePlayVM", "EmptyStack recomposed: color=$color, modifier=$modifier")
    CardItem(
        cardSizeDp = cardSizeDp,
        modifier = modifier.then(Modifier.alpha(0.3f)),
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
    onStackPositioned: ((Card.Color, androidx.compose.ui.layout.LayoutCoordinates) -> Unit)? = null,
    onDragEnd: ((Int?, Card.Color) -> Unit)? = null
) {
    val viewModel: GamePlayViewModel = viewModel()
    val draggedCardId by viewModel.draggedCardId.collectAsState()
    Column(
        verticalArrangement = Arrangement.spacedBy(cardSpacing),
    ) {
        Card.Color.entries.forEach() { color ->
            val stackModifier = Modifier
                .size(cardSizeDp.height, cardSizeDp.width)
                .then(
                    if (onStackPositioned != null) Modifier.onGloballyPositioned { coordinates ->
                        android.util.Log.d("HanabiGameBoardUI", "ColorStacks onGloballyPositioned callback called for color=$color!")
                        onStackPositioned(color, coordinates)
                    } else Modifier
                )
                .then(
                    if (onDragEnd != null) Modifier.pointerInput(draggedCardId) {
                        detectDragGestures(
                            onDragStart = {},
                            onDragEnd = { onDragEnd(draggedCardId, color) },
                            onDragCancel = {},
                            onDrag = { _, _ -> }
                        )
                    } else Modifier
                )
            if (stackValues[color]==0) {
                EmptyStack(
                    cardSizeDp = cardSizeDp,
                    isPortrait = false,
                    onClick = { onColorStackClick(color) },
                    color = color,
                    modifier = stackModifier
                )
            } else {
                CardItem(
                    cardSizeDp = cardSizeDp,
                    card = Card(color, stackValues[color]?:0),
                    isPortrait = false,
                    highlightColor = colorFromColorEnum(color),
                    onClick = { onColorStackClick(color) },
                    modifier = stackModifier
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

