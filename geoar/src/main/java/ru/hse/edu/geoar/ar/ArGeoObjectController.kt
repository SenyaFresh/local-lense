package ru.hse.edu.geoar.ar

import com.google.ar.core.Frame
import com.google.ar.core.Pose
import ru.hse.edu.geoar.geo.GeoMath
import ru.hse.edu.geoar.geo.GeoObject
import ru.hse.edu.geoar.location.LocationData

class ArGeoObjectController(val geoObject: GeoObject) {

    private var state: ArPlacementState = SearchingState

    fun update(
        userLocation: LocationData,
        userHeading: Float,
        frame: Frame,
        cameraPose: Pose,
        wallFinder: ArGeoWallFinder
    ) {
        val distance = GeoMath.distanceMeters(userLocation, geoObject)

        if (distance > ArGeoConfig.MAX_DISTANCE_METERS) {
            geoObject.node.isVisible = false
            return
        }

        geoObject.node.scale = ArMath.calculateScale(distance)

        val params = PlacementParams(
            geoObject = geoObject,
            userLocation = userLocation,
            userHeading = userHeading,
            frame = frame,
            cameraPose = cameraPose,
            wallFinder = wallFinder,
            distance = distance
        )

        val nextState = state.update(params)
        if (nextState !== state) {
            state.release()
            state = nextState
        }
    }

    fun detach() {
        state.release()
        state = SearchingState
    }
}