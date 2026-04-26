package ru.hse.edu.placemarks.presentation.components

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import ru.hse.edu.placemarks.domain.entities.Placemark
import ru.hse.edu.placemarks.presentation.components.listitem.DeleteActionReveal
import ru.hse.edu.placemarks.presentation.components.listitem.PlacemarkColorIndicator
import ru.hse.edu.placemarks.presentation.components.listitem.PlacemarkInfo
import ru.hse.edu.placemarks.presentation.components.listitem.PlacemarkPhotoIndicator
import ru.hse.edu.placemarks.presentation.components.listitem.PlacemarkQuickActions
import ru.hse.edu.placemarks.presentation.components.listitem.rememberPlacemarkSwipeState

@Composable
fun PlacemarkListItem(
    placemark: Placemark,
    onPlacemarkDelete: () -> Unit,
    modifier: Modifier = Modifier,
    isActionsRevealed: Boolean = false,
    onOpenOnMap: () -> Unit = {},
    onOpenInAr: () -> Unit = {},
) {
    val swipe = rememberPlacemarkSwipeState()
    val cardShape = RoundedCornerShape(16.dp)

    LaunchedEffect(isActionsRevealed, swipe.contextMenuWidth) {
        swipe.syncRevealed(isActionsRevealed)
    }

    Card(
        modifier = modifier,
        shape = cardShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min),
        ) {
            DeleteActionReveal(
                onDelete = onPlacemarkDelete,
                onSizeChanged = { swipe.contextMenuWidth = it.width.toFloat() },
                shape = cardShape,
            )

            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = cardShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { swipe.pixelOffset }
                    .pointerInput(swipe.contextMenuWidth) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { _, dragAmount -> swipe.onDrag(dragAmount) },
                            onDragEnd = { swipe.onDragEnd() },
                        )
                    },
            ) {
                PlacemarkRow(
                    placemark = placemark,
                    onOpenOnMap = onOpenOnMap,
                    onOpenInAr = onOpenInAr,
                )
            }
        }
    }
}

@Composable
private fun PlacemarkRow(
    placemark: Placemark,
    onOpenOnMap: () -> Unit,
    onOpenInAr: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        when (val type = placemark.type) {
            is Placemark.Type.TextPhoto -> PlacemarkPhotoIndicator(
                photoPath = type.photoPath,
                accent = placemark.color,
            )
            else -> PlacemarkColorIndicator(color = placemark.color)
        }

        Spacer(modifier = Modifier.width(12.dp))

        PlacemarkInfo(
            placemark = placemark,
            modifier = Modifier.weight(1f),
        )

        Spacer(modifier = Modifier.width(8.dp))

        PlacemarkQuickActions(
            onOpenOnMap = onOpenOnMap,
            onOpenInAr = onOpenInAr,
        )
    }
}
