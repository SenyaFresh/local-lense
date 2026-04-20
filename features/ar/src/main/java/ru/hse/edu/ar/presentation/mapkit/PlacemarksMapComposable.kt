package ru.hse.edu.ar.presentation.mapkit

import android.graphics.PointF
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.hse.edu.ar.domain.entities.ArPlacemark

@Composable
fun PlacemarksMapComposable(
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

    val onClickRef = rememberUpdatedState(onPlacemarkClick)

    val mapView = remember { MapView(context) }
    val map = remember(mapView) { mapView.mapWindow.map }
    val collection = remember(map) { map.mapObjects.addCollection() }
    val tapListeners = remember { mutableListOf<MapObjectTapListener>() }
    val lookup = remember { mutableMapOf<PlacemarkMapObject, ArPlacemark>() }

    var pinsLoading by remember { mutableStateOf(false) }

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

        if (placemarks.isEmpty()) return@LaunchedEffect

        pinsLoading = true

        val bitmaps = withContext(Dispatchers.Default) {
            placemarks.map { pm ->
                createPinBitmap(pm.color.toArgb(), pm.name, pinSizePx)
            }
        }

        placemarks.forEachIndexed { index, placemark ->
            val mapObject = collection.addPlacemark().apply {
                geometry = Point(placemark.locationData.latitude, placemark.locationData.longitude)
            }
            mapObject.setIcon(
                ImageProvider.fromBitmap(bitmaps[index]),
                IconStyle().setAnchor(PointF(0.5f, 1.0f)),
            )
            lookup[mapObject] = placemark

            val listener = MapObjectTapListener { obj, _ ->
                lookup[obj]?.let { onClickRef.value.invoke(it) }
                true
            }
            tapListeners += listener
            mapObject.addTapListener(listener)
        }

        pinsLoading = false
    }

    Box(
        modifier = modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(24.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(24.dp),
            ),
    ) {
        AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())

        AnimatedVisibility(
            visible = placemarks.isNotEmpty() && !pinsLoading,
            enter = fadeIn(tween(300)) + scaleIn(
                initialScale = 0.85f,
                animationSpec = tween(300, easing = FastOutSlowInEasing),
            ),
            exit = fadeOut(tween(200)) + scaleOut(targetScale = 0.85f),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp),
        ) {
            MapOverlayChip(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.94f),
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = MaterialTheme.colorScheme.secondary,
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondary,
                        modifier = Modifier.size(14.dp),
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = placemarks.size.toString(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }

        AnimatedVisibility(
            visible = placemarks.isEmpty() && !pinsLoading,
            enter = fadeIn(tween(400)),
            exit = fadeOut(tween(200)),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp),
        ) {
            MapOverlayChip(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.94f),
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .background(
                            color = MaterialTheme.colorScheme.tertiary,
                            shape = CircleShape,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Place,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onTertiary,
                        modifier = Modifier.size(16.dp),
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Нет доступных меток",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                )
            }
        }

        AnimatedVisibility(
            visible = pinsLoading,
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(300)),
            modifier = Modifier.align(Alignment.Center),
        ) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.95f),
                shadowElevation = 6.dp,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(16.dp)
                        .size(28.dp),
                    strokeWidth = 3.dp,
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.12f),
                )
            }
        }
    }
}

@Composable
private fun MapOverlayChip(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
    content: @Composable RowScope.() -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        shadowElevation = 4.dp,
        color = containerColor,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}