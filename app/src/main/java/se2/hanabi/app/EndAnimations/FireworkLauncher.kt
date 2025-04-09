package se2.hanabi.app.EndAnimations

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration

@Composable
fun FireworkLauncher(onAnimationEnd: ()-> Unit) {
    val localConfig = LocalConfiguration.current

    val duration = 15f
    val animatedTime = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        animatedTime.animateTo(
            targetValue = duration,
            animationSpec = tween(
                durationMillis = (duration*1000f).toInt(),
                easing = LinearEasing
            )
        )
        onAnimationEnd()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        fireworks(
            numFirworks = 30,
            time = animatedTime.value,
            localConfig = localConfig
        )
    }
}