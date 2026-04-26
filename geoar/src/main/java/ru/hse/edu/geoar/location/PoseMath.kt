package ru.hse.edu.geoar.location

import com.google.ar.core.Pose
import ru.hse.edu.geoar.math.GeoConstants
import ru.hse.locallense.common.entities.LocationData
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

internal fun extractYawDegrees(pose: Pose): Float {
    val qx = pose.qx().toDouble()
    val qy = pose.qy().toDouble()
    val qz = pose.qz().toDouble()
    val qw = pose.qw().toDouble()
    val sinYaw = 2.0 * (qx * qz + qw * qy)
    val cosYaw = 1.0 - 2.0 * (qx * qx + qy * qy)
    return Math.toDegrees(atan2(sinYaw, cosYaw)).toFloat()
}

internal fun signedAngleDelta(target: Float, current: Float): Float =
    ((target - current + 540f).mod(360f)) - 180f

internal fun poseToLocation(
    pose: Pose,
    initialPose: Pose,
    initialHeading: Float,
    initialLocation: LocationData,
): LocationData {
    val dx = (pose.tx() - initialPose.tx()).toDouble()
    val dy = (pose.ty() - initialPose.ty()).toDouble()
    val dz = (pose.tz() - initialPose.tz()).toDouble()
    val forward = -dz
    val right = dx
    val headingRad = Math.toRadians(initialHeading.toDouble())
    val sinH = sin(headingRad)
    val cosH = cos(headingRad)
    val eastMeters = forward * sinH + right * cosH
    val northMeters = forward * cosH - right * sinH

    val deltaLat = northMeters / GeoConstants.metersPerDegreeLatitude()
    val metersPerDegLon = GeoConstants.metersPerDegreeLongitude(initialLocation.latitude)
    val deltaLon = if (metersPerDegLon != 0.0) eastMeters / metersPerDegLon else 0.0

    return LocationData(
        latitude = initialLocation.latitude + deltaLat,
        longitude = initialLocation.longitude + deltaLon,
        altitude = initialLocation.altitude + dy,
    )
}
