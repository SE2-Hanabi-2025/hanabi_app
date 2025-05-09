package se2.hanabi.app.gamePlayUI

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import se2.hanabi.app.model.Card
import se2.hanabi.app.model.Hint

@Composable
fun HintSelector(
    onHintClick: (Hint) -> Unit,
    selectedHint: Hint? = null
) {
    val viewModel: GamePlayViewModel = viewModel()
    val hintItemSize = 60.dp
    val paddingAmount = 15.dp
    Column() {
        Row(
            modifier = Modifier
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
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card.Color.entries.forEach() { color ->
                    HintItem(
                        colorIn = color,
                        size = hintItemSize,
                        isSelected = (selectedHint!=null) && color==selectedHint.getColor() ,
                        onClick = { onHintClick(Hint(color)) }
                    )
                }
            }
            // number hints
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (i in 1..5) {
                    HintItem(
                        value = i,
                        size = hintItemSize,
                        isSelected = selectedHint!=null && selectedHint.getValue()==i,
                        onClick = { onHintClick(Hint(i)) }
                    )
                }
            }
        }
       GiveHintButton(
           width = hintItemSize.times(2) + paddingAmount.times(3),
           height = hintItemSize,
           modifier = Modifier
               .clip(RoundedCornerShape(paddingAmount)),
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
    val backgroundBrush = Brush.verticalGradient(
        if (isAvailable) {
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
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Give hint",
            fontSize = (width.value/5).sp,
            fontFamily = FontFamily.Cursive,
            color = if (!isAvailable) Color(0x566290FF) else Color(0xFFF2FF90),
        )
    }
}

@Composable
fun HintItem(
    modifier: Modifier = Modifier,
    colorIn: Card.Color? = null,
    value: Int = -1,
    size: Dp = 60.dp,
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
                    fontFamily = FontFamily.Cursive,
                    color = Color(0xFFF2FF90),
                    fontSize = (size.value * 0.75).sp
                )
            }
        }
    }

}