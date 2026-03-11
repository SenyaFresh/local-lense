package ru.hse.edu.placemarks.presentation.components

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.LocationOn
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import ru.hse.edu.placemarks.domain.entities.Placemark
import ru.hse.locallense.common.ResultContainer
import ru.hse.locallense.common.entities.LocationData
import ru.hse.locallense.common.entities.Tag
import ru.hse.locallense.common.round
import ru.hse.locallense.components.composables.ActionIcon
import ru.hse.locallense.presentation.locals.LocalSpacing
import kotlin.collections.emptyList
import kotlin.math.roundToInt

@Composable
fun PlacemarkListItem(
    placemark: Placemark,
    onPlacemarkDelete: () -> Unit,
    modifier: Modifier = Modifier,
    isActionsRevealed: Boolean = false
) {
    var contextMenuWidth by remember { mutableFloatStateOf(0f) }
    val offset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    val itemShape = CardDefaults.shape

    LaunchedEffect(isActionsRevealed, contextMenuWidth) {
        if (isActionsRevealed) {
            offset.animateTo(-contextMenuWidth)
        } else {
            offset.animateTo(0f)
        }
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        shape = itemShape,
        elevation = CardDefaults.cardElevation(1.dp)
    ) {
        PlacemarkCardContent(
            placemark = placemark,
            onPlacemarkDelete = onPlacemarkDelete,
            offset = offset,
            contextMenuWidth = contextMenuWidth,
            onSizeChanged = { contextMenuWidth = it.width.toFloat() },
            shape = itemShape,
            scope = scope
        )
    }
}

@Composable
private fun PlacemarkCardContent(
    placemark: Placemark,
    onPlacemarkDelete: () -> Unit,
    offset: Animatable<Float, *>,
    contextMenuWidth: Float,
    onSizeChanged: (IntSize) -> Unit,
    shape: Shape,
    scope: CoroutineScope
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(if (placemark.tags.isEmpty()) 70.dp else 90.dp)
    ) {
        SecondaryPlacemarkActions(
            onSizeChanged = onSizeChanged,
            onPlacemarkDelete = onPlacemarkDelete,
            shape = shape
        )

        PlacemarkContent(
            placemark = placemark,
            offset = offset,
            contextMenuWidth = contextMenuWidth,
            scope = scope,
            shape = shape,
        )
    }
}

@Composable
private fun BoxScope.SecondaryPlacemarkActions(
    onSizeChanged: (IntSize) -> Unit,
    onPlacemarkDelete: () -> Unit,
    shape: Shape,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .align(Alignment.CenterEnd)
            .onSizeChanged { size -> onSizeChanged(size) }
            .padding(1.dp)
            .clip(shape),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ActionIcon(
            imageVector = Icons.Default.Delete,
            text = "Удалить",
            contentColor = MaterialTheme.colorScheme.onError,
            containerColor = MaterialTheme.colorScheme.error,
            modifier = Modifier.fillMaxHeight(),
            onClick = onPlacemarkDelete
        )
    }
}

@Composable
private fun PlacemarkContent(
    placemark: Placemark,
    offset: Animatable<Float, *>,
    contextMenuWidth: Float,
    scope: CoroutineScope,
    shape: Shape,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = shape,
        modifier = Modifier
            .offset { IntOffset(offset.value.roundToInt(), 0) }
            .pointerInput(contextMenuWidth) {
                detectHorizontalDragGestures(
                    onHorizontalDrag = { _, dragAmount ->
                        scope.launch {
                            val newOffset =
                                (offset.value + dragAmount).coerceIn(-contextMenuWidth, 0f)
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
        Row(modifier = modifier.fillMaxSize()) {
            Spacer(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(4.dp)
                    .background(placemark.color)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(
                        start = 12.dp,
                        end = 12.dp,
                        top = 10.dp,
                        bottom = 10.dp
                    )
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = placemark.name,
                        maxLines = 1,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        overflow = TextOverflow.Ellipsis,
                    )

                    Spacer(modifier = Modifier.height(3.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(13.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${placemark.locationData.latitude.round(4)}, ${placemark.locationData.longitude.round(4)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (placemark.tags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            placemark.tags.forEach { tag ->
                                PlacemarkTag(tag = tag.name)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .width(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
            }
        }
    }
}

@Composable
private fun PlacemarkTag(tag: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(
                MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.7f)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = "#$tag",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
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
    )
}