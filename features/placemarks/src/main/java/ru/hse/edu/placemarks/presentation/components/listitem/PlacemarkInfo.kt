package ru.hse.edu.placemarks.presentation.components.listitem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.hse.edu.placemarks.domain.entities.Placemark
import ru.hse.locallense.common.round

@Composable
internal fun PlacemarkInfo(
    placemark: Placemark,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Text(
            text = placemark.name,
            maxLines = 1,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = formatCoordinates(placemark),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

        if (placemark.tags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                placemark.tags.forEach { tag ->
                    PlacemarkTagChip(name = tag.name)
                }
            }
        }
    }
}

private fun formatCoordinates(placemark: Placemark): String {
    val lat = placemark.locationData.latitude.round(6)
    val lon = placemark.locationData.longitude.round(6)
    return "$lat, $lon"
}
