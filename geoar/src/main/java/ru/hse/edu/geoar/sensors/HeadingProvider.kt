package ru.hse.edu.geoar.sensors

import android.content.Context
import android.hardware.GeomagneticField
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorManager
import ru.hse.locallense.common.entities.LocationData

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
        return ((Math.toDegrees(orientationAngles[0].toDouble()) + declination).mod(360.0)).toFloat()
    }
}