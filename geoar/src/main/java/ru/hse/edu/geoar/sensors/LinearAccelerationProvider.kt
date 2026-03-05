package ru.hse.edu.geoar.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import kotlin.math.sqrt

class LinearAccelerationProvider(
    context: Context,
) : SensorProvider(context, Sensor.TYPE_LINEAR_ACCELERATION) {

    override fun extract(event: SensorEvent): Float = sqrt(
        event.values[0] * event.values[0] +
                event.values[1] * event.values[1] +
                event.values[2] * event.values[2]
    )
}