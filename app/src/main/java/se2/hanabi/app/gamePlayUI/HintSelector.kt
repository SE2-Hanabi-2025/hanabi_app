package se2.hanabi.app.gamePlayUI

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import se2.hanabi.app.model.Card
import se2.hanabi.app.model.Hint
import se2.hanabi.app.ui.theme.customFont

const val hintSelecterToCardWidthProportion = 0.9f
const val hintPaddingToHintSelectorProportion = 0.25f

@Composable
fun HintSelector(
    landscape: Boolean,
    cardSizeDp: DpSize,
    onHintClick: (Hint) -> Unit,
    selectedHint: Hint? = null
) {
    val viewModel: GamePlayViewModel = viewModel()
    val hintItemSize = cardSizeDp.width.times(hintSelecterToCardWidthProportion)
    val paddingAmount = cardSizeDp.width.times(hintPaddingToHintSelectorProportion)

    val hintSelectorHeight = hintItemSize.times(5) + paddingAmount.times(6)
    val hintSelectorWidth = hintItemSize.times(2) + paddingAmount.times(3)

    val layoutWidth = if (landscape) hintSelectorHeight else hintSelectorWidth
    val layoutHeight = if (landscape) hintSelectorWidth else hintSelectorHeight

    Column(

    ) {
        Box(
            modifier = Modifier
                .size(layoutWidth, layoutHeight)
        ) {
            Row(
                modifier = Modifier
                    .graphicsLayer {
                        rotationZ = if (landscape) -90f else 0f
                    }
                    .requiredSize(hintSelectorWidth, hintSelectorHeight)
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(paddingAmount))
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.5f),
                                Color.Black.copy(alpha = 0.7f)
                            )
                        )
                    )
                    .padding(paddingAmount),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(paddingAmount)
            ) {
                // colors hints
                Column(
                    verticalArrangement = Arrangement.spacedBy(paddingAmount)
                ) {
                    Card.Color.entries.forEach() { color ->
                        HintItem(
                            colorIn = color,
                            size = hintItemSize,
                            rotationAmountZ = if (landscape) 90f else 0f,
                            isSelected = (selectedHint != null) && color == selectedHint.getColor(),
                            onClick = { onHintClick(Hint(color)) }
                        )
                    }
                }
                // number hints
                Column(
                    verticalArrangement = Arrangement.spacedBy(paddingAmount)
                ) {
                    for (i in 1..5) {
                        HintItem(
                            value = i,
                            size = hintItemSize,
                            rotationAmountZ = if (landscape) 90f else 0f,
                            isSelected = selectedHint != null && selectedHint.getValue() == i,
                            onClick = { onHintClick(Hint(i)) }
                        )
                    }
                }
            }
        }


        GiveHintButton(
            width = hintSelectorWidth,
            height = hintItemSize,
            modifier = Modifier
                .clip(RoundedCornerShape(paddingAmount))
                .align(Alignment.CenterHorizontally),
            isAvailable = viewModel.isValidHint.collectAsState().value,
            onClick = viewModel::onGiveHintClick
        )
    }
}

@Composable
fun GiveHintButton(
    modifier: Modifier = Modifier,
    width: Dp,
    height: Dp,
    isAvailable: Boolean = false,
    onClick: () -> Unit
) {
    val viewModel: GamePlayViewModel = viewModel()
    val isMyTurn by viewModel.isMyTurn.collectAsState()
    val numRemainingHintTokens by viewModel.numRemainingHintTokens.collectAsState()
    
    // The hint is only truly available if:
    // 1. The hint is valid
    // 2. It's the player's turn
    // 3. There are hint tokens available
    val isTrulyAvailable = isAvailable && isMyTurn && numRemainingHintTokens > 0
    
    val backgroundBrush = Brush.verticalGradient(
        if (isTrulyAvailable) {
            listOf(Color(0xFF282828), Color(0xFF282828).copy(alpha = 0.9f))
        } else {
            listOf(Color(0xFF282828).copy(alpha = 0.7f), Color(0xFF282828).copy(alpha = 0.6f))
        }
    )
    
    Box(
        modifier = modifier
            .width(width)
            .height(height)
            .background(
                brush = backgroundBrush
            )
            .clickable(enabled = isTrulyAvailable) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "GIVE HINT",
            fontSize = (width.value/5).sp,
            fontFamily = customFont,
            color = if (!isTrulyAvailable) Color(0x566290FF) else Color(0xFFF2FF90),
        )
    }
}

@Composable
fun HintItem(
    modifier: Modifier = Modifier,
    colorIn: Card.Color? = null,
    value: Int = -1,
    size: Dp = 60.dp,
    rotationAmountZ: Float = 0f,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}

) {
    var color = if (colorIn==null) Color.White else colorFromColorEnum(colorIn)// default is white
    val haloSize = 15.dp
    val haloProp = haloSize.div(size)
    Box() {
        // highlighting
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(size)
                    .graphicsLayer {
                        scaleX = 1 + haloProp
                        scaleY = 1 + haloProp
                    }
                    .background(
                        brush = Brush.radialGradient(
                            colorStops = arrayOf(
                                1 - haloProp to color,
                                1f to color.copy(0f)
                            )
                        )
                    )
            )
        }

        if (colorIn == null) {
            color = Color(0xFF566290)
        }
        Box(
            modifier = Modifier
                .size(size)
                .graphicsLayer{
                    rotationZ = rotationAmountZ
                }
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(color, color.copy(alpha = 0f))
                    )
                )
                .selectable(
                    selected = isSelected,
                    onClick = onClick,
                    role = Role.RadioButton,
                ),
            contentAlignment = Alignment.Center
        ) {
            if (value in 1..5) {
                Text(
                    text = value.toString(),
                    fontFamily = customFont,
                    color = Color(0xFFF2FF90),
                    fontSize = (size.value * 0.75).sp
                )
            }
        }
    }

}