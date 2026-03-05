package ru.hse.edu.geoar.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager

class HeadingProvider(
    context: Context,
) : SensorProvider(context, Sensor.TYPE_ROTATION_VECTOR) {

    private val rotationMatrix = FloatArray(9)
    private val remapped = FloatArray(9)
    private val orientation = FloatArray(3)

    override fun extract(event: SensorEvent): Float {
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
        SensorManager.remapCoordinateSystem(
            rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, remapped
        )
        SensorManager.getOrientation(remapped, orientation)
        return ((Math.toDegrees(orientation[0].toDouble()) + 360) % 360).toFloat()
    }

    override fun smooth(new: Float, prev: Float): Float {
        var delta = new - prev
        if (delta > 180f) delta -= 360f
        if (delta < -180f) delta += 360f
        return (prev + alpha * delta + 360f) % 360f
    }
}