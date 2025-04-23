package se2.hanabi.app.gamePlayUI

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HintSelecter(
    modifier: Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(15.dp))
            .background(Brush.verticalGradient(colors = listOf(Color.Black.copy(alpha = 0.2f),Color.Black.copy(alpha = 0.4f))))
            .padding(15.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // colors hints
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            colors.forEachIndexed() { index, color ->
                HintItem(
                    color = color,
                )
            }
        }
        // number hints
        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            for (i in 1 .. 5) {
                HintItem(
                    value = i,
                )
            }
        }
    }
}

@Composable
fun HintItem(
    modifier: Modifier = Modifier,
    color: String = "",
    value: Int = -1,
    size: Dp = 60.dp

) {
    val colorUsed = if (color!="") colorFromString(color)  else Color(0xFF566290)
    Box(
        modifier = Modifier
            .size(size)
            .clip(CircleShape)
            .background(Brush.verticalGradient(
                colors = listOf(colorUsed,colorUsed.copy(alpha = 0f))
            )),
        contentAlignment = Alignment.Center
    ) {
        if (value in 1..5) {
            Text(
                text = value.toString(),
                fontFamily = FontFamily.Cursive,
                color = Color(0xFFF2FF90),
                fontSize = (size.value*0.75).sp
            )
        }
    }

}