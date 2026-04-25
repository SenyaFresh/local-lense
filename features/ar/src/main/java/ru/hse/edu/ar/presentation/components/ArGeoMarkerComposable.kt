package ru.hse.edu.ar.presentation.components

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.io.File
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

    if (isFar) {
        PinMarker(
            name = arPlacemark.name,
            distanceMeters = distance,
        )
        return
    }

    when (val type = arPlacemark.type) {
        is ArPlacemark.Type.Text -> ContentCard(
            name = arPlacemark.name,
            text = type.text,
        )
        is ArPlacemark.Type.TextPhoto -> PhotoCard(
            name = arPlacemark.name,
            photoPath = type.photoPath,
            caption = type.text,
        )
        is ArPlacemark.Type.Simple,
        is ArPlacemark.Type.Photo,
        is ArPlacemark.Type.Audio -> PinMarker(
            name = arPlacemark.name,
            distanceMeters = distance,
        )
    }
}

@Composable
private fun PinMarker(
    name: String,
    distanceMeters: Double,
) {
    Row(
        modifier = Modifier
            .background(Color.Black.copy(alpha = 0.9f), RoundedCornerShape(20.dp))
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
            .background(Color.Black.copy(alpha = 0.9f), RoundedCornerShape(16.dp))
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

@Composable
private fun PhotoCard(name: String, photoPath: String, caption: String) {
    val bitmap = remember(photoPath) {
        runCatching {
            val file = File(photoPath)
            if (!file.exists()) return@runCatching null
            val opts = BitmapFactory.Options().apply {
                inSampleSize = 2
                inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888
            }
            BitmapFactory.decodeFile(file.absolutePath, opts)?.asImageBitmap()
        }.getOrNull()
    }
    Column(
        modifier = Modifier
            .widthIn(min = 100.dp, max = 140.dp)
            .background(Color.Black.copy(alpha = 0.9f), RoundedCornerShape(12.dp))
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
            .padding(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .background(Color(0xFF7C4DFF), CircleShape),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = name.uppercase(),
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.0.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(Modifier.height(8.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.15f)),
        )

        Spacer(Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4f / 3f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.05f)),
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxWidth().aspectRatio(4f / 3f),
                )
            }
        }

        if (caption.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                text = caption,
                color = Color.White,
                fontSize = 12.sp,
                lineHeight = 16.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
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
            color = Color(0xFF7C4DFF),
            tags = emptyList(),
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
            color = Color(0xFF7C4D00),
            tags = emptyList(),
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
            color = Color(0xFF7C4DFF),
            tags = emptyList(),
            isWallAnchor = false,
        ),
        placementResult = ArGeoObjectPlacementResult(
            distanceMeters = 7.0,
            bearing = 270.0,
            state = PlacedAirState(),
        ),
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF2B2B2B, name = "Card · Close · Photo + caption")
@Composable
private fun PreviewPhotoCard() {
    PhotoCard(
        name = "Любимое место",
        photoPath = "",
        caption = "Здесь мы пили кофе зимним утром.",
    )
}
