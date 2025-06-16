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

    // Set this to true for emulator testing, false for real device
    private val isEmulator = true // Change to false for real device

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
        val x = it.values[0]
        val z = it.values[2]
        val pitch = Math.toDegrees(Math.atan2(-x.toDouble(), z.toDouble())).toFloat()

        android.util.Log.d("TiltCheatSensor", "Pitch: $pitch")

        if (isEmulator) {
            // Trigger only when face down, not on both tilt directions
            isTilted.value = pitch > 40f
        } else {
            // For real device, allow strong tilt in either direction
            isTilted.value = kotlin.math.abs(pitch) > 70f
        }
    }
}

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
