package ru.hse.edu.geoar.heading

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class HeadingProvider(context: Context) {
    private val sensorManager = context.getSystemService(SensorManager::class.java)

    private val _heading = MutableStateFlow(0f)
    val heading: StateFlow<Float> = _heading.asStateFlow()

    private var smoothed = 0f

    private val listener = object : SensorEventListener {

        override fun onSensorChanged(event: SensorEvent) {
            val rotation = FloatArray(9)
            SensorManager.getRotationMatrixFromVector(rotation, event.values)

            val remapped = FloatArray(9)
            SensorManager.remapCoordinateSystem(
                rotation,
                SensorManager.AXIS_X,
                SensorManager.AXIS_Z,
                remapped
            )

            val orientation = FloatArray(3)
            SensorManager.getOrientation(remapped, orientation)

            val raw = ((Math.toDegrees(orientation[0].toDouble()) + 360) % 360).toFloat()
            _heading.value = smooth(raw)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    fun start() {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
            ?: return
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_FASTEST)
    }

    fun stop() = sensorManager.unregisterListener(listener)

    private fun smooth(raw: Float, alpha: Float = 0.15f): Float {
        var delta = raw - smoothed
        if (delta > 180f) delta -= 360f
        if (delta < -180f) delta += 360f
        smoothed = (smoothed + alpha * delta + 360f) % 360f
        return smoothed
    }
}