package ru.hse.edu.geoar.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class SensorProvider(
    context: Context,
    private val sensorType: Int,
    protected val alpha: Float = 0.15f,
    private val delay: Int = SensorManager.SENSOR_DELAY_FASTEST
) {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val _raw = MutableStateFlow(0f)
    val raw: StateFlow<Float> = _raw.asStateFlow()

    private val _smoothed = MutableStateFlow(0f)
    val smoothed: StateFlow<Float> = _smoothed.asStateFlow()

    private var emaValue = 0f

    private val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val value = extract(event)
            _raw.value = value
            emaValue = smooth(value, emaValue)
            _smoothed.value = emaValue
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    protected abstract fun extract(event: SensorEvent): Float

    protected open fun smooth(new: Float, prev: Float): Float =
        prev + alpha * (new - prev)

    fun start() {
        val sensor = sensorManager.getDefaultSensor(sensorType) ?: return
        sensorManager.registerListener(listener, sensor, delay)
    }

    fun stop() {
        sensorManager.unregisterListener(listener)
    }
}