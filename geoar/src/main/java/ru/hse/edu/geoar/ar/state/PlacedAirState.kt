package ru.hse.edu.geoar.ar.state

import ru.hse.edu.geoar.ar.ArGeoConfig
import ru.hse.edu.geoar.math.ArMath
import ru.hse.edu.geoar.math.GeoMath.relativeBearingRadians
import kotlin.math.PI
import kotlin.math.abs

class PlacedAirState : ArPlacementState() {

    override fun isValid(parameters: PlacementParameters) =
        parameters.distance > ArGeoConfig.AR_RADIUS

    private var isInitialized = false
    private var lastBearingRadians: Double = Double.NaN
    private var lastDistanceMeters: Double = Double.NaN

    override fun update(parameters: PlacementParameters) {
        val node = parameters.arGeoObject.node

        val relativeBearingRadians = relativeBearingRadians(
            headingDegrees = parameters.initialCameraHeading,
            from = parameters.userLocation,
            to = parameters.arGeoObject
        )

        val isFirstUpdate = lastBearingRadians.isNaN()
        val bearingDelta = if (isFirstUpdate) Double.MAX_VALUE
            else abs(shortestAngleDelta(relativeBearingRadians, lastBearingRadians))
        val distanceDelta = if (isFirstUpdate) Double.MAX_VALUE
            else abs(parameters.distance - lastDistanceMeters)

        val significantChange = bearingDelta >= BEARING_THRESHOLD_RADIANS ||
                distanceDelta >= DISTANCE_THRESHOLD_METERS

        if (!isInitialized || (parameters.distance > ArGeoConfig.AR_RADIUS && significantChange)) {
            val newPosition = ArMath.airPosition(
                cameraPose = parameters.cameraPose,
                relativeBearingRadians = relativeBearingRadians,
                realDistanceMeters = parameters.distance,
                altitudeDifference = parameters.arGeoObject.locationData.altitude - parameters.userLocation.altitude
            )
            node.worldPosition = newPosition
            lastBearingRadians = relativeBearingRadians
            lastDistanceMeters = parameters.distance
        }

        applyBillboardRotation(parameters.cameraPose, node)
    }

    companion object {
        private const val BEARING_THRESHOLD_RADIANS = 0.05 * PI / 180.0
        private const val DISTANCE_THRESHOLD_METERS = 0.05

        fun create(
            parameters: PlacementParameters,
        ): PlacedAirState {
            val state = PlacedAirState()
            state.update(parameters)
            state.isInitialized = true
            return state
        }
    }
}

private fun shortestAngleDelta(a: Double, b: Double): Double {
    val twoPi = 2.0 * PI
    val raw = (a - b) % twoPi
    return when {
        raw > PI -> raw - twoPi
        raw < -PI -> raw + twoPi
        else -> raw
    }
}