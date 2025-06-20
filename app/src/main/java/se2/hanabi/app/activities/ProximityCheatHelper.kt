package se2.hanabi.app.activities

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager

class ProximityCheatHelper(context: Context, private val onProximityDark: () -> Unit, private val onProximityLight: () -> Unit) : SensorEventListener {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val proximitySensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
    var isDark = false
        private set

    fun register() {
        proximitySensor?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun unregister() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            val value = it.values[0]
            // Most proximity sensors return 0 when "covered" (dark/close)
            val dark = value < (proximitySensor?.maximumRange ?: 1f)
            if (dark != isDark) {
                isDark = dark
                if (isDark) onProximityDark() else onProximityLight()
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
