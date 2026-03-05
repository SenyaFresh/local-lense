package ru.hse.edu.geoar.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager

class HeadingProvider(
    context: Context,
) : SensorProvider(context, Sensor.TYPE_ROTATION_VECTOR) {

    private val rotationMatrix = FloatArray(9)
    private val remappedRotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)

    override fun extract(event: SensorEvent): Float {
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
        SensorManager.remapCoordinateSystem(
            rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, remappedRotationMatrix
        )
        SensorManager.getOrientation(remappedRotationMatrix, orientationAngles)
        return ((Math.toDegrees(orientationAngles[0].toDouble()) + 360) % 360).toFloat()
    }

    override fun smooth(newValue: Float, previousValue: Float): Float {
        var difference = newValue - previousValue
        if (difference > 180f) difference -= 360f
        if (difference < -180f) difference += 360f
        return (previousValue + alpha * difference + 360f) % 360f
    }
}