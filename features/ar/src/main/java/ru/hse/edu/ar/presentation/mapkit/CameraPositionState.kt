package ru.hse.edu.ar.presentation.mapkit

import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.setValue
import com.yandex.mapkit.Animation
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.Map
import ru.hse.locallense.common.entities.LocationData

@Stable
class CameraPositionState(
    initialLatitude: Double,
    initialLongitude: Double,
    initialZoom: Float = 10f,
) {
    internal var map: Map? = null

    var latitude by mutableDoubleStateOf(initialLatitude)
        internal set
    var longitude by mutableDoubleStateOf(initialLongitude)
        internal set
    var zoom by mutableFloatStateOf(initialZoom)
        internal set

    fun move(
        latitude: Double,
        longitude: Double,
        zoom: Float = this.zoom,
        azimuth: Float = 0f,
        tilt: Float = 0f,
        animate: Boolean = true,
        durationSeconds: Float = 0.5f,
    ) {
        val pos = CameraPosition(Point(latitude, longitude), zoom, azimuth, tilt)
        val m = map ?: return
        if (animate) {
            m.move(pos, Animation(Animation.Type.SMOOTH, durationSeconds), null)
        } else {
            m.move(pos)
        }
    }

    fun move(point: Point, zoom: Float = this.zoom, animate: Boolean = true) =
        move(point.latitude, point.longitude, zoom, animate = animate)

    fun move(location: LocationData, zoom: Float = this.zoom, animate: Boolean = true) =
        move(location.latitude, location.longitude, zoom, animate = animate)
}