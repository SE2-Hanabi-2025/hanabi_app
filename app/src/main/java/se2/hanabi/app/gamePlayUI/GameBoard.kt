package se2.hanabi.app.gamePlayUI

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import se2.hanabi.app.card.Card
import se2.hanabi.app.card.CardItem

/*
displays common game board elements such as: fuse/hint tokens, discard/draw pile, color stacks
 */
@Composable
fun GameBoardUI(
    stackValues: IntArray,
    numRemainingCards: Int,
    lastDiscardedCard: Card
    ) {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(5.dp)

        ) {
            //FuseTokens()
            RemainingCardsStack(numRemainingCards = numRemainingCards)
            DiscardedCardsStack(lastDiscardedCard = lastDiscardedCard)
            //HintTokens()
        }
        ColorStacks(stackValues = stackValues)
    }
}
@Composable
fun RemainingCardsStack(
    modifier: Modifier = Modifier,
    numRemainingCards: Int
) {
    Box(contentAlignment = Alignment.Center ) {
        CardItem(
            card = Card("red", 1),
            flipCardState = true,
            isPortrait = false,
        )
        Text(
            text = "$numRemainingCards",
            fontFamily = FontFamily.Cursive,
            color = Color(0xFFF2FF90),
            fontSize = 40.sp,
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
    lastDiscardedCard: Card
) {
    CardItem(
        card = Card(lastDiscardedCard.color,lastDiscardedCard.number),
        flipCardState = false,
        isPortrait = false,
    )

}


@Composable
fun EmptyStack(
    modifier: Modifier = Modifier,
    isPortrait: Boolean = true,
) {
    CardItem(
        modifier = Modifier.alpha(0.3f),
        card = Card("red",1),
        flipCardState = true,
        isPortrait = isPortrait,
    )
}

@Composable
fun ColorStacks(
    modifier: Modifier = Modifier,
    stackValues: IntArray,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier.padding(10.dp)
    ) {
        colors.forEachIndexed() {index, color ->
            if (stackValues[index]==0) {
                EmptyStack(isPortrait = false)
            } else {
                CardItem(card = Card(color, stackValues[index]), flipCardState = false, isPortrait = false)
            }
        }
    }
}

