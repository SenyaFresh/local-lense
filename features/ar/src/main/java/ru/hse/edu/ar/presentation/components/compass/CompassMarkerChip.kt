package ru.hse.edu.ar.presentation.components.compass

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.hse.locallense.components.composables.MapPin

@Composable
internal fun CompassMarkerChip(
    data: CompassMarkerData,
    accentColor: Color,
    pinWidthDp: Dp,
    pinHeightDp: Dp,
    modifier: Modifier = Modifier,
) {
    val verticalIcon = altitudeIcon(
        altitudeDelta = data.altitudeDelta,
        distanceMeters = data.distance,
    )
    Row(
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.85f), RoundedCornerShape(10.dp))
            .padding(start = ChipStartPadding, end = 6.dp, top = 1.dp, bottom = 1.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        MapPin(
            color = data.color,
            dotColor = Color.White,
            width = pinWidthDp,
            height = pinHeightDp,
            drawDot = true,
        )
        Spacer(Modifier.width(3.dp))
        if (verticalIcon != null) {
            Icon(
                imageVector = verticalIcon,
                contentDescription = null,
                modifier = Modifier.size(11.dp),
                tint = accentColor,
            )
            Spacer(Modifier.width(1.dp))
        }
        Text(
            text = formatDistance(data.distance),
            style = TextStyle(
                color = Color.White,
                fontSize = 10.sp,
                lineHeight = 12.sp,
                fontWeight = FontWeight.SemiBold,
            ),
            maxLines = 1,
            softWrap = false,
        )
    }
}
