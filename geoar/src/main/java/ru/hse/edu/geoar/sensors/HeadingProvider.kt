package ru.hse.edu.geoar.sensors

import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import ru.hse.edu.geoar.location.LocationData
import kotlin.math.abs

class HeadingProvider(
    context: Context,
) : SensorProvider(
    context = context,
    sensorType = Sensor.TYPE_ROTATION_VECTOR,
    alpha = 0.15f,
    sensorDelay = SensorManager.SENSOR_DELAY_GAME
) {

    private val rotationMatrix = FloatArray(9)
    private val remappedRotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    private var declination = 0f

    fun setLocation(location: LocationData) {
        val geoField = GeomagneticField(
            location.latitude.toFloat(),
            location.longitude.toFloat(),
            location.altitude.toFloat(),
            System.currentTimeMillis()
        )
        declination = geoField.declination
    }

    override fun extract(event: SensorEvent): Float {
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
        SensorManager.remapCoordinateSystem(
            rotationMatrix, SensorManager.AXIS_X, SensorManager.AXIS_Z, remappedRotationMatrix
        )
        SensorManager.getOrientation(remappedRotationMatrix, orientationAngles)
        return ((Math.toDegrees(orientationAngles[0].toDouble()) + declination + 360) % 360).toFloat()
    }

    override fun smooth(newValue: Float, previousValue: Float): Float {
        var difference = newValue - previousValue
        if (difference > 180f) difference -= 360f
        if (difference < -180f) difference += 360f

        val adaptiveAlpha = when {
            abs(difference) < 1f -> 0.02f
            abs(difference) < 3f -> 0.05f
            abs(difference) < 10f -> 0.1f
            abs(difference) < 30f -> 0.25f
            else -> 0.5f
        }

        return (previousValue + adaptiveAlpha * difference + 360f) % 360f
    }
}