package ru.hse.edu.ar.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.hse.edu.ar.domain.entities.ArPlacemark
import ru.hse.edu.geoar.ar.ArGeoConfig
import ru.hse.edu.geoar.ar.ArGeoObjectPlacementResult
import ru.hse.edu.geoar.ar.state.InitialState
import ru.hse.edu.geoar.ar.state.PlacedAirState
import ru.hse.locallense.common.entities.LocationData

@Composable
fun ArGeoMarkerComposable(
    arPlacemark: ArPlacemark,
    placementResult: ArGeoObjectPlacementResult?,
) {
    if (placementResult == null || placementResult.state == InitialState) return

    val distance = placementResult.distanceMeters
    val isFar = distance > ArGeoConfig.AR_RADIUS

    if (isFar || arPlacemark.type is ArPlacemark.Type.Simple) {
        PinMarker(
            name = arPlacemark.name,
            distanceMeters = distance,
        )
    } else {
        val text = (arPlacemark.type as ArPlacemark.Type.Text).text
        ContentCard(name = arPlacemark.name, text = text)
    }
}

@Composable
private fun PinMarker(
    name: String,
    distanceMeters: Double,
) {
    Row(
        modifier = Modifier
            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(Color(0xFF7C4DFF), CircleShape),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = name,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = "${distanceMeters.toInt()} м",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 11.sp,
        )
    }
}

@Composable
private fun ContentCard(name: String, text: String) {
    Column(
        modifier = Modifier
            .widthIn(min = 160.dp, max = 260.dp)
            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp))
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(Color(0xFF7C4DFF), CircleShape),
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = name.uppercase(),
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.2.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(Modifier.height(10.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.15f)),
        )

        Spacer(Modifier.height(10.dp))

        Text(
            text = text,
            color = Color.White,
            fontSize = 15.sp,
            lineHeight = 21.sp,
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF2B2B2B, name = "Pin · Far")
@Composable
private fun PreviewPinMarker() {
    ArGeoMarkerComposable(
        arPlacemark = ArPlacemark(
            id = 1L,
            name = "Кафе «Место»",
            type = ArPlacemark.Type.Simple,
            locationData = LocationData(55.7558, 37.6173, altitude = 200.0),
            isWallAnchor = false,
        ),
        placementResult = ArGeoObjectPlacementResult(
            distanceMeters = 42.0,
            bearing = 90.0,
            state = PlacedAirState(),
        ),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF2B2B2B, name = "Card · Close · Text (short)")
@Composable
private fun PreviewContentCardShort() {
    ArGeoMarkerComposable(
        arPlacemark = ArPlacemark(
            id = 3L,
            name = "Заметка",
            type = ArPlacemark.Type.Text("Привет! Здесь был я!"),
            locationData = LocationData(55.7558, 37.6173, altitude = 200.0),
            isWallAnchor = false,
        ),
        placementResult = ArGeoObjectPlacementResult(
            distanceMeters = 3.0,
            bearing = 45.0,
            state = PlacedAirState(),
        ),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF2B2B2B, name = "Card · Close · Text (long)")
@Composable
private fun PreviewContentCardLong() {
    ArGeoMarkerComposable(
        arPlacemark = ArPlacemark(
            id = 4L,
            name = "Историческая справка",
            type = ArPlacemark.Type.Text(
                "Этот дом был построен в 1893 году архитектором Ф. О. Шехтелем. " +
                        "Является объектом культурного наследия федерального значения."
            ),
            locationData = LocationData(55.7558, 37.6173, altitude = 200.0),
            isWallAnchor = false,
        ),
        placementResult = ArGeoObjectPlacementResult(
            distanceMeters = 7.0,
            bearing = 270.0,
            state = PlacedAirState(),
        ),
    )
}
