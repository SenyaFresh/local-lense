package ru.hse.edu.geoar.ar

import com.google.ar.core.Camera
import com.google.ar.core.Plane
import com.google.ar.core.Pose
import com.google.ar.core.Session
import com.google.ar.core.TrackingState
import kotlin.math.abs

class ArDistanceTracker(var maxDistance: Float = 10f) {

    data class Result(
        val plane: Plane,
        val distance: Float,
        val cameraToPlaneVector: List<Float>,
        val closestPointOnPlane: Pose
    )

    fun findClosestVerticalPlane(session: Session, camera: Camera): Result? {
        if (camera.trackingState != TrackingState.TRACKING) return null

        val cameraPos = camera.pose.let {
            floatArrayOf(it.tx(), it.ty(), it.tz())
        }

        var closestPlane: Plane? = null
        var closestDistance = maxDistance

        for (plane in session.getAllTrackables(Plane::class.java)) {
            if (!isTrackingVerticalPlane(plane)) continue

            val distance = distanceToPlane(cameraPos, plane)
            if (distance < closestDistance) {
                closestDistance = distance
                closestPlane = plane
            }
        }

        val plane = closestPlane ?: return null
        val closestPoint = projectOntoPlane(cameraPos, plane)

        return Result(
            plane = plane,
            distance = closestDistance,
            cameraToPlaneVector = listOf(
                closestPoint.tx() - cameraPos[0],
                closestPoint.ty() - cameraPos[1],
                closestPoint.tz() - cameraPos[2]
            ),
            closestPointOnPlane = closestPoint
        )
    }

    private fun isTrackingVerticalPlane(plane: Plane): Boolean =
        plane.type == Plane.Type.VERTICAL
                && plane.trackingState == TrackingState.TRACKING
                && plane.subsumedBy == null

    private fun getPlaneNormal(pose: Pose): FloatArray {
        val m = FloatArray(16)
        pose.toMatrix(m, 0)
        return floatArrayOf(m[4], m[5], m[6])
    }

    private fun signedDistance(point: FloatArray, pose: Pose, normal: FloatArray): Float =
        (point[0] - pose.tx()) * normal[0] +
                (point[1] - pose.ty()) * normal[1] +
                (point[2] - pose.tz()) * normal[2]

    private fun distanceToPlane(point: FloatArray, plane: Plane): Float {
        val pose = plane.centerPose
        return abs(signedDistance(point, pose, getPlaneNormal(pose)))
    }

    private fun projectOntoPlane(point: FloatArray, plane: Plane): Pose {
        val pose = plane.centerPose
        val normal = getPlaneNormal(pose)
        val d = signedDistance(point, pose, normal)
        return Pose.makeTranslation(
            point[0] - d * normal[0],
            point[1] - d * normal[1],
            point[2] - d * normal[2]
        )
    }
}