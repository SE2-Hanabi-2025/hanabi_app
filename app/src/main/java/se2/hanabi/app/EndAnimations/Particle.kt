package se2.hanabi.app.EndAnimations

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

class Particle (
    val color: Color,
    val initialRadius: Dp,
    val maxEndRadiusScalar: Float,
    val startXPos: Dp,
    val startYPos: Dp,
    val maxVerticalDisplacement: Dp, // max vertical displacement
    val initialAngle: Float,
    val gravity: Float
) {
    var currentXPos = 0.dp //
    var currentYPos = 0.dp

    val maxInitialVerticalVelocity = -sqrt(2*gravity*maxVerticalDisplacement.value)
    val blastRangeProportion = Random.nextFloat()

    val initialParticleVelocity = blastRangeProportion*maxInitialVerticalVelocity

    val initialHorizontalVelocity = initialParticleVelocity * cos(initialAngle)
    val initialVerticalVelocity = initialParticleVelocity * sin(initialAngle)

    val directionTowards = (Random.nextFloat()<0.5)
    val endRadiusScalar = if (directionTowards) {
        maxEndRadiusScalar*(1-blastRangeProportion)
    } else {
        (1/maxEndRadiusScalar)*(1-blastRangeProportion)
    }

    var alpha = 0f

    var currentRadius = 0.dp


    fun update(
        animationProgress: Float,
        animProgSquaredHalfGravity: Float,
    ) {
        currentXPos = startXPos + (initialHorizontalVelocity*animationProgress).dp
        currentYPos = startYPos + (initialVerticalVelocity*animationProgress + animProgSquaredHalfGravity).dp

        currentRadius = initialRadius*(( animationProgress * (endRadiusScalar-1) +1))

        alpha = 1f -animationProgress;

    }
}

