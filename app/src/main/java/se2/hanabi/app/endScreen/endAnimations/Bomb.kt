package se2.hanabi.app.endScreen.endAnimations

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlin.math.min
import kotlin.random.Random

@Composable
fun Bomb(
    time: Float,
    localConfig: Configuration
) {
    val bombStartDiamater = 20
    val numBurst = 15

    for (i in 0 until numBurst) {
        val startTimeOffset by remember { mutableFloatStateOf(Random.nextFloat() * 2f) }
        val duration by remember { mutableFloatStateOf(Random.nextFloat() * 5f+1) }
        val color by remember { mutableStateOf( listOf(Color.Red.copy(alpha = Random.nextFloat()*0.5f+0.49f), Color.White.copy(alpha = Random.nextFloat()*0.5f+0.49f)).random()) }
        val blastRangeProportion by remember { mutableFloatStateOf(Random.nextFloat()*0.5f + 0.5f) }
        val numParticles by remember { mutableIntStateOf((100*blastRangeProportion).toInt()) }
        val maxBlastRange by remember {
            mutableStateOf(min(localConfig.screenWidthDp, localConfig.screenHeightDp)*(blastRangeProportion)*0.5f)  } // have blast range from a third of the smallest dimension to the
        val startXpos by remember { mutableFloatStateOf( (-bombStartDiamater/2)+(Random.nextFloat()*bombStartDiamater)+localConfig.screenWidthDp/2.toFloat()) }
        val startYpos by remember { mutableFloatStateOf( (-bombStartDiamater/2)+(Random.nextFloat()*bombStartDiamater)+localConfig.screenHeightDp/2.toFloat()) }

        Burst(
            numParticles = numParticles,
            maxBlastRange = maxBlastRange.dp,
            time = time,
            startTimeOffset = startTimeOffset,
            duration = duration,
            gravity = 500f,
            startXPos = startXpos.dp,
            startYPos = startYpos.dp,
            color = color
        )
    }
}