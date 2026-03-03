package ru.hse.edu.geoar.main

import com.google.ar.core.Frame
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.core.Pose
import com.google.ar.core.TrackingState

class ArGeoWallFinder {
    fun raycastWall(frame: Frame, cameraPose: Pose, dirX: Float, dirZ: Float): HitResult? {
        val origin = floatArrayOf(cameraPose.tx(), cameraPose.ty(), cameraPose.tz())
        val direction = floatArrayOf(dirX, 0f, dirZ)

        return frame.hitTest(origin, 0, direction, 0)
            .firstOrNull { hit ->
                val trackable = hit.trackable
                trackable is Plane &&
                        trackable.type == Plane.Type.VERTICAL &&
                        trackable.trackingState == TrackingState.TRACKING &&
                        hit.distance <= ArGeoConfig.MAX_WALL_DETECTION_DISTANCE
            }
    }
}