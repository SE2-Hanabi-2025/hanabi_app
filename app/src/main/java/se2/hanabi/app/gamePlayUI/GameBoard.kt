package se2.hanabi.app.gamePlayUI

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import se2.hanabi.app.card.Card
import se2.hanabi.app.card.CardItem

/*
displays common game board elements such as: fuse/hint tokens, discard/draw pile, color stacks
 */
@Composable
fun GameBoardUI() {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Column {
            //FuseTokens()
            //DrawStacks()
            //DiscardStacks()
            //HintTokens()
        }
        ColorStacks(stackValues = intArrayOf(0,1,4,3,5))
    }
}

@Composable
fun EmptyStack(
    modifier: Modifier = Modifier,
) {
    CardItem(
        Modifier
            .alpha(0.2f),
        Card("red",1),
        flipCardState = true
    )
}

@Composable
fun ColorStacks(
    modifier: Modifier = Modifier,
    stackValues: IntArray,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
            .wrapContentSize()
            .graphicsLayer { rotationZ = -90f }
    ) {
        colors.forEachIndexed() {index, color ->
            if (stackValues[index]==0) {
                EmptyStack()
            } else {
                CardItem(card = Card(color, stackValues[index]), flipCardState = false)
            }
        }
    }
}

