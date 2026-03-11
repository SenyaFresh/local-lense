package ru.hse.edu.ar.presentation.mapkit

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.yandex.mapkit.MapKitFactory
import com.yandex.mapkit.geometry.Point
import com.yandex.mapkit.map.CameraListener
import com.yandex.mapkit.map.CameraPosition
import com.yandex.mapkit.mapview.MapView

@Composable
fun LocationPickerMapView(
    modifier: Modifier = Modifier,
    title: String,
    initialLatitude: Double,
    initialLongitude: Double,
    initialZoom: Float = 14f,
    onConfirm: (latitude: Double, longitude: Double) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val mapView = remember { MapView(context) }
    val map = remember(mapView) { mapView.mapWindow.map }

    var currentLat by remember { mutableDoubleStateOf(initialLatitude) }
    var currentLon by remember { mutableDoubleStateOf(initialLongitude) }

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

        val listener = CameraListener { _, pos, _, _ ->
            currentLat = pos.target.latitude
            currentLon = pos.target.longitude
        }
        map.addCameraListener(listener)
        onDispose { map.removeCameraListener(listener) }
    }

    Column(modifier) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            AndroidView(factory = { mapView }, modifier = Modifier.fillMaxSize())
            MapPin(modifier = Modifier.offset(y = (-22).dp))
        }

        Button(
            onClick = { onConfirm(currentLat, currentLon) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
        ) {
            Text("OK")
        }
    }
}

@Composable
private fun MapPin(modifier: Modifier = Modifier, color: Color = Color.Red) {
    androidx.compose.foundation.Canvas(modifier = modifier.size(32.dp, 44.dp)) {
        val r = size.width / 2
        val center = Offset(r, r)

        drawCircle(color, r - 1.dp.toPx(), center)

        val path = androidx.compose.ui.graphics.Path().apply {
            moveTo(r - r * 0.5f, r + r * 0.2f)
            lineTo(r, size.height - 1.dp.toPx())
            lineTo(r + r * 0.5f, r + r * 0.2f)
            close()
        }
        drawPath(path, color)
        drawCircle(Color.White, r * 0.35f, center)
    }
}