package se2.hanabi.app.gamePlayUI

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

/**
 * BackGlow adds a border of the glow color.
 * This border fades to transparency
 */
@Composable
fun BackGlow(
    modifier: Modifier = Modifier,
    width: Dp,
    height: Dp,
    glowSize: Dp,
    glowColor: Color = Color.White,
) {

    Box() {
        val widthWithHalo = glowSize + width + glowSize
        val xHaloProp = glowSize / widthWithHalo
        val xScaleFact = widthWithHalo / width
        val heightWithHalo = glowSize + height + glowSize
        val yHaloProp = glowSize / heightWithHalo
        val yScaleFact = heightWithHalo / height

        val glowSizePx = with(LocalDensity.current) { glowSize.toPx() }

        // left and right gradients
        Box(
            modifier = Modifier
                .size(width, height)
                .graphicsLayer { scaleX = xScaleFact }
                .background(
                    brush = Brush.horizontalGradient(
                        colorStops = arrayOf(
                            0.0f to glowColor.copy(0f),
                            xHaloProp to glowColor,
                            1 - xHaloProp to glowColor,
                            1f to glowColor.copy(0f)
                        ),
                    ),
                )
        )
        // top and bottom gradients
        Box(
            modifier = Modifier
                .size(width, height)
                .graphicsLayer { scaleY = yScaleFact }
                .background(
                    brush = Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to glowColor.copy(0f),
                            yHaloProp to glowColor,
                            1 - yHaloProp to glowColor,
                            1f to glowColor.copy(0f)
                        ),
                    )
                )
        )
        // corner gradients
        for (i in 0 until 4) {
            Box(
                modifier = Modifier
                    .size(glowSize, glowSize)
                    .offset((width - glowSize).times(i % 2), (height - glowSize).times(i / 2))
                    .graphicsLayer {
                        translationX = glowSize.times((-1 + 2 * (i % 2))).toPx()
                        translationY = glowSizePx * (-1 + 2 * (i / 2))
                    }
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                glowColor,
                                glowColor.copy(0f)
                            ),
                            center = Offset(
                                glowSizePx * (1 - i % 2),
                                glowSizePx * (1 - i / 2)
                            ),
                            radius = glowSizePx
                        )
                    )
            )
        }
    }
}