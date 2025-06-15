package se2.hanabi.app.gamePlayUI

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf

/**
 * Listens for device tilt to the right. When tilt is detected, sets [isTilted] to true.
 */
class TiltCheatSensor(context: Context) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    val isTilted: MutableState<Boolean> = mutableStateOf(false)

    fun start() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stop() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val y = it.values[1]
            val z = it.values[2]
            // Calculate horizontal rotation (roll) in degrees
            val roll = Math.toDegrees(Math.atan2(y.toDouble(), z.toDouble())).toFloat()
            // Cheat active if roll exceeds 40 degrees (either direction)
            isTilted.value = kotlin.math.abs(roll) > 40f
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
