package ru.hse.edu.geoar.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

abstract class SensorProvider(
    context: Context,
    private val sensorType: Int,
    protected val alpha: Float = 0.15f,
    private val sensorDelay: Int = SensorManager.SENSOR_DELAY_GAME
) {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val _rawValue = MutableStateFlow(0f)
    val rawValue: StateFlow<Float> = _rawValue.asStateFlow()

    private val _smoothedValue = MutableStateFlow(0f)
    val smoothedValue: StateFlow<Float> = _smoothedValue.asStateFlow()

    private val _accuracy = MutableStateFlow(SensorManager.SENSOR_STATUS_ACCURACY_HIGH)
    val accuracy: StateFlow<Int> = _accuracy.asStateFlow()

    private var emaValue = 0f
    private var initialized = false

    private val sensorEventListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            val value = extract(event)
            _rawValue.value = value

            emaValue = if (!initialized) {
                initialized = true
                value
            } else {
                smooth(value, emaValue)
            }

            _smoothedValue.value = emaValue
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            _accuracy.value = accuracy
        }
    }

    protected abstract fun extract(event: SensorEvent): Float

    protected open fun smooth(newValue: Float, previousValue: Float): Float =
        previousValue + alpha * (newValue - previousValue)

    fun start() {
        val sensor = sensorManager.getDefaultSensor(sensorType) ?: return
        sensorManager.registerListener(sensorEventListener, sensor, sensorDelay)
    }

    fun stop() {
        sensorManager.unregisterListener(sensorEventListener)
        initialized = false
        emaValue = 0f
    }
}