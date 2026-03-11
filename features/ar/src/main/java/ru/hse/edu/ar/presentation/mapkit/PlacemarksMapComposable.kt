package ru.hse.edu.ar.presentation.mapkit

import android.graphics.PointF
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.map.IconStyle
import com.yandex.mapkit.map.MapObjectTapListener
import com.yandex.mapkit.map.PlacemarkMapObject
import com.yandex.mapkit.mapview.MapView
import com.yandex.runtime.image.ImageProvider
import ru.hse.edu.ar.domain.entities.ArPlacemark

@Composable
fun PlacemarksMapComposable (
    modifier: Modifier = Modifier,
    placemarks: List<ArPlacemark> = emptyList(),
    initialLatitude: Double,
    initialLongitude: Double,
    initialZoom: Float = 12f,
    pinSizePx: Int = 96,
    onPlacemarkClick: (ArPlacemark) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val onClickRef = remember { mutableStateOf(onPlacemarkClick) }
    onClickRef.value = onPlacemarkClick

    val mapView = remember { MapView(context) }
    val map = remember(mapView) { mapView.mapWindow.map }
    val collection = remember(map) { map.mapObjects.addCollection() }
    val tapListeners = remember { mutableListOf<MapObjectTapListener>() }
    val lookup = remember { mutableMapOf<PlacemarkMapObject, ArPlacemark>() }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    MapKitFactory.getInstance().onStart()
                    mapView.onStart()
                }

                Lifecycle.Event.ON_STOP -> {
                    mapView.onStop()
                    MapKitFactory.getInstance().onStop()
                }

                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    DisposableEffect(map) {
        map.move(CameraPosition(Point(initialLatitude, initialLongitude), initialZoom, 0f, 0f))
        onDispose {}
    }

    LaunchedEffect(placemarks, pinSizePx) {
        collection.clear()
        lookup.clear()
        tapListeners.clear()

        placemarks.forEach { placemark ->
            val mapObject = collection.addPlacemark().apply {
                geometry = Point(placemark.locationData.latitude, placemark.locationData.longitude)
            }
            val bmp = createPinBitmap(placemark.color.toArgb(), placemark.name, pinSizePx)
            mapObject.setIcon(
                ImageProvider.fromBitmap(bmp),
                IconStyle().setAnchor(PointF(0.5f, 1.0f))
            )
            lookup[mapObject] = placemark

            val listener = MapObjectTapListener { obj, _ ->
                lookup[obj]?.let { onClickRef.value.invoke(it) }
                true
            }
            tapListeners += listener
            mapObject.addTapListener(listener)
        }
    }

    AndroidView(factory = { mapView }, modifier = modifier)
}