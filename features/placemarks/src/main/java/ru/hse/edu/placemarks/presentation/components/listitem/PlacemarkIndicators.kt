package ru.hse.edu.placemarks.presentation.components.listitem

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import java.io.File

private val IndicatorSize = 44.dp
private val IndicatorShape = RoundedCornerShape(12.dp)

@Composable
internal fun PlacemarkColorIndicator(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(IndicatorSize)
            .clip(IndicatorShape)
            .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Rounded.LocationOn,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp),
        )
    }
}

@Composable
internal fun PlacemarkPhotoIndicator(
    photoPath: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(IndicatorSize)
            .clip(IndicatorShape)
            .background(accent.copy(alpha = 0.15f))
            .border(1.dp, accent.copy(alpha = 0.4f), IndicatorShape),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = File(photoPath),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(IndicatorSize)
                .clip(IndicatorShape),
        )
    }
}
