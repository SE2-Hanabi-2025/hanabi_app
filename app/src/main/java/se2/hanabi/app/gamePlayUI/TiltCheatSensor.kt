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

    // Automatically detect if running on emulator or device
    private val isEmulator: Boolean =
        android.os.Build.FINGERPRINT.contains("generic") ||
        android.os.Build.MODEL.contains("Emulator") ||
        android.os.Build.MODEL.contains("Android SDK built for x86")

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
            val z = it.values[2]
            val wasTilted = isTilted.value
            val isFaceDown = z < -6f
            isTilted.value = isFaceDown
            if (!wasTilted && isTilted.value) {
                android.util.Log.d("TiltCheatSensor", "Tilt detected! (isEmulator=$isEmulator)")
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
