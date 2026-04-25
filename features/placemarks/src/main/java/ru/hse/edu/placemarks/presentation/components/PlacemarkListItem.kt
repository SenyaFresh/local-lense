package ru.hse.edu.placemarks.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.ViewInAr
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import java.io.File
import kotlinx.coroutines.launch
import ru.hse.edu.placemarks.domain.entities.Placemark
import ru.hse.locallense.common.entities.LocationData
import ru.hse.locallense.common.entities.Tag
import ru.hse.locallense.common.round
import kotlin.math.roundToInt

private val tagColorPalette = listOf(
    Color(0xFF1565C0),
    Color(0xFF2E7D32),
    Color(0xFFE65100),
    Color(0xFF6A1B9A),
    Color(0xFF00838F),
    Color(0xFFC62828),
    Color(0xFFF9A825),
    Color(0xFF37474F),
)

@Composable
fun PlacemarkListItem(
    placemark: Placemark,
    onPlacemarkDelete: () -> Unit,
    modifier: Modifier = Modifier,
    isActionsRevealed: Boolean = false,
    onOpenOnMap: () -> Unit = {},
    onOpenInAr: () -> Unit = {}
) {
    var contextMenuWidth by remember { mutableFloatStateOf(0f) }
    val offset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val cardShape = RoundedCornerShape(16.dp)

    LaunchedEffect(isActionsRevealed, contextMenuWidth) {
        if (isActionsRevealed) {
            offset.animateTo(-contextMenuWidth)
        } else {
            offset.animateTo(0f)
        }
    }

    Card(
        modifier = modifier,
        shape = cardShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            DeleteActionReveal(
                onDelete = onPlacemarkDelete,
                onSizeChanged = { contextMenuWidth = it.width.toFloat() },
                shape = cardShape
            )

            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = cardShape,
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(offset.value.roundToInt(), 0) }
                    .pointerInput(contextMenuWidth) {
                        detectHorizontalDragGestures(
                            onHorizontalDrag = { _, dragAmount ->
                                scope.launch {
                                    val newOffset = (offset.value + dragAmount)
                                        .coerceIn(-contextMenuWidth, 0f)
                                    offset.snapTo(newOffset)
                                }
                            },
                            onDragEnd = {
                                scope.launch {
                                    if (offset.value <= -contextMenuWidth / 2f) {
                                        offset.animateTo(-contextMenuWidth)
                                    } else {
                                        offset.animateTo(0f)
                                    }
                                }
                            }
                        )
                    }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val type = placemark.type
                    if (type is Placemark.Type.TextPhoto) {
                        PlacemarkPhotoIndicator(
                            photoPath = type.photoPath,
                            accent = placemark.color,
                        )
                    } else {
                        PlacemarkColorIndicator(color = placemark.color)
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    PlacemarkInfo(
                        placemark = placemark,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    PlacemarkQuickActions(
                        onOpenOnMap = onOpenOnMap,
                        onOpenInAr = onOpenInAr,
                    )
                }
            }
        }
    }
}

@Composable
private fun BoxScope.DeleteActionReveal(
    onDelete: () -> Unit,
    onSizeChanged: (IntSize) -> Unit,
    shape: Shape
) {
    Row(
        modifier = Modifier
            .align(Alignment.CenterEnd)
            .fillMaxHeight()
            .onSizeChanged(onSizeChanged)
            .clip(shape),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.error)
                .clickable(onClick = onDelete)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onError,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    text = "Удалить",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onError
                )
            }
        }
    }
}

@Composable
private fun PlacemarkColorIndicator(
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.15f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.LocationOn,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
private fun PlacemarkPhotoIndicator(
    photoPath: String,
    accent: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(accent.copy(alpha = 0.15f))
            .border(1.dp, accent.copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = File(photoPath),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(44.dp)
                .clip(RoundedCornerShape(12.dp)),
        )
    }
}

@Composable
private fun PlacemarkInfo(
    placemark: Placemark,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = placemark.name,
            maxLines = 1,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(3.dp))

        Text(
            text = "${placemark.locationData.latitude.round(6)}, ${
                placemark.locationData.longitude.round(
                    6
                )
            }",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        if (placemark.tags.isNotEmpty()) {
            Spacer(modifier = Modifier.height(8.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                placemark.tags.forEach { tag ->
                    PlacemarkTag(tag = tag.name)
                }
            }
        }
    }
}

@Composable
private fun PlacemarkQuickActions(
    onOpenOnMap: () -> Unit,
    onOpenInAr: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        PlacemarkActionButton(
            icon = Icons.Rounded.Map,
            contentDescription = "Открыть на карте",
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            onClick = onOpenOnMap
        )
        PlacemarkActionButton(
            icon = Icons.Rounded.ViewInAr,
            contentDescription = "Открыть в AR",
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            onClick = onOpenInAr
        )
    }
}

@Composable
private fun PlacemarkActionButton(
    icon: ImageVector,
    contentDescription: String,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(34.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(containerColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = contentColor,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun PlacemarkTag(tag: String) {
    val color = remember(tag) {
        tagColorPalette[(tag.hashCode() and 0x7FFFFFFF) % tagColorPalette.size]
    }
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.1f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(
            text = "#$tag",
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Preview
@Composable
fun PlacemarkListItemWithTagsPreview() {
    PlacemarkListItem(
        placemark = Placemark(
            id = 4L,
            name = "Историческая справка",
            type = Placemark.Type.Text(
                "Этот дом был построен в 1893 году архитектором Ф. О. Шехтелем. " +
                        "Является объектом культурного наследия федерального значения."
            ),
            locationData = LocationData(55.7558, 37.6173, altitude = 200.0),
            color = Color(0xFF7C4DFF),
            tags = listOf(
                Tag(0L, "Архитектура"),
                Tag(1L, "История"),
            ),
        ),
        onPlacemarkDelete = {},
        onOpenOnMap = {},
        onOpenInAr = {}
    )
}

@Preview
@Composable
fun PlacemarkListItemWithoutTagsPreview() {
    PlacemarkListItem(
        placemark = Placemark(
            id = 4L,
            name = "Историческая справка",
            type = Placemark.Type.Text(
                "Этот дом был построен в 1893 году архитектором Ф. О. Шехтелем. " +
                        "Является объектом культурного наследия федерального значения."
            ),
            locationData = LocationData(55.7558, 37.6173, altitude = 200.0),
            color = Color(0xFF7C4DFF),
            tags = emptyList(),
        ),
        onPlacemarkDelete = {},
        onOpenOnMap = {},
        onOpenInAr = {}
    )
}