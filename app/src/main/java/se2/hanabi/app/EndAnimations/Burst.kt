package se2.hanabi.app.EndAnimations

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.endanimations.Particle
import kotlin.math.pow
import kotlin.random.Random

const val PI: Float = 3.141592653589793f

@Composable
fun Burst(
    numParticles: Int,
    maxBlastRange: Dp,
    gravity: Float,
    time: Float,
    startTimeOffset: Float,
    duration: Float,
    startXPos: Dp,
    startYPos:Dp,
    color: Color
) {

    val particles = remember {
        List(numParticles) {

            Particle(
                color = color,
                initialRadius = 3.dp,
                maxEndRadiusScalar = 1.2f,
                startXPos = startXPos,
                startYPos = startYPos,
                maxVerticalDisplacement = maxBlastRange,
                initialAngle = randomRange(0f, 2* PI),
                gravity = gravity,
            )
        }
    }

    var animationProgress: Float
    // animate progress after delay for the given duration
    if (time<startTimeOffset || time>(startTimeOffset+duration)) {

    } else {
        animationProgress = (time-startTimeOffset)/duration
        Canvas(modifier = Modifier
            .fillMaxSize()
        ) {
            // calculate 0.5*a*t^2 once for all particles in burst to save calculating it for many times
            val animProgSquaredHalfGravity = 0.5*gravity*animationProgress.pow(2)
            particles.forEach { particle ->
                particle.update(
                    animationProgress = animationProgress,
                    animProgSquaredHalfGravity = animProgSquaredHalfGravity.toFloat(),
                    )
                drawCircle(
                    color = particle.color,
                    radius = particle.currentRadius.toPx(),
                    alpha = particle.alpha,
                    center = Offset(
                        particle.currentXPos.toPx(),
                        particle.currentYPos.toPx()
                    ),
                )

            }
        }
    }


}
fun randomRange(min:Float, max:Float): Float = min + Random.nextFloat()*max*(max-min)