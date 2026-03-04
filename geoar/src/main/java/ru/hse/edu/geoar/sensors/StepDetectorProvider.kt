package ru.hse.edu.geoar.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class StepDetectorProvider(context: Context) {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val _steps = MutableSharedFlow<Long>(extraBufferCapacity = 1)
    val steps: SharedFlow<Long> = _steps.asSharedFlow()

    var lastStepTime = 0L
        private set

    val timeSinceLastStep: Long
        get() = System.currentTimeMillis() - lastStepTime

    private val listener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            lastStepTime = System.currentTimeMillis()
            _steps.tryEmit(lastStepTime)
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    fun start() {
        val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR) ?: return
        sensorManager.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_FASTEST)
    }

    fun stop() = sensorManager.unregisterListener(listener)
}