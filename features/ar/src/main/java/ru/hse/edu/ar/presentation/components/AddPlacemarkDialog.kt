package ru.hse.edu.ar.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ru.hse.edu.ar.domain.entities.ArPlacemark
import ru.hse.locallense.common.entities.LocationData
import ru.hse.locallense.common.entities.Tag
import ru.hse.locallense.components.composables.buttons.DefaultPrimaryButton
import ru.hse.locallense.components.composables.buttons.DefaultSecondaryButton
import ru.hse.locallense.components.composables.inputs.DefaultTextField
import kotlin.math.roundToInt
import kotlin.random.Random

enum class PlacemarkTypeOption(val label: String) {
    SIMPLE("Обычная"),
    TEXT("Текстовая"),
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddPlacemarkDialog(
    locationData: LocationData,
    isWallAnchor: Boolean = false,
    availableTags: List<Tag>,
    onDismiss: () -> Unit,
    onConfirm: (ArPlacemark) -> Unit,
    onAddTag: (Tag) -> Unit,
    onDeleteTag: (Long) -> Unit,
) {
    var selectedType by remember { mutableStateOf(PlacemarkTypeOption.SIMPLE) }
    var name by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }

    val defaultColor = Color(0xFF7C4DFF)
    var selectedColor by remember { mutableStateOf(defaultColor) }
    var isCustomColor by remember { mutableStateOf(false) }
    var showColorPicker by remember { mutableStateOf(false) }
    var red by remember { mutableFloatStateOf(defaultColor.red) }
    var green by remember { mutableFloatStateOf(defaultColor.green) }
    var blue by remember { mutableFloatStateOf(defaultColor.blue) }

    var selectedTagIds by remember { mutableStateOf(setOf<Long>()) }
    var showNewTagInput by remember { mutableStateOf(false) }
    var newTagName by remember { mutableStateOf("") }

    val presetColors = remember {
        listOf(
            Color(0xFFEF5350), Color(0xFFFF9800), Color(0xFFFFEB3B),
            Color(0xFF4CAF50), Color(0xFF2196F3), Color(0xFF7C4DFF),
        )
    }

    val isValid = name.isNotBlank() && when (selectedType) {
        PlacemarkTypeOption.SIMPLE -> true
        PlacemarkTypeOption.TEXT -> text.isNotBlank()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Text(
                    text = "Новая заметка",
                    style = MaterialTheme.typography.headlineSmall,
                )

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    PlacemarkTypeOption.entries.forEachIndexed { idx, opt ->
                        SegmentedButton(
                            selected = selectedType == opt,
                            onClick = { selectedType = opt },
                            shape = SegmentedButtonDefaults.itemShape(
                                idx, PlacemarkTypeOption.entries.size,
                            ),
                        ) { Text(opt.label) }
                    }
                }

                DefaultTextField(
                    text = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Название") },
                )

                AnimatedVisibility(visible = selectedType == PlacemarkTypeOption.TEXT) {
                    DefaultTextField(
                        text = text,
                        onValueChange = { text = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Текст заметки") },
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionLabel("Цвет")

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        presetColors.forEach { color ->
                            ColorDot(
                                color = color,
                                isSelected = !isCustomColor && selectedColor == color,
                                onClick = {
                                    selectedColor = color
                                    isCustomColor = false
                                    showColorPicker = false
                                    red = color.red
                                    green = color.green
                                    blue = color.blue
                                },
                            )
                        }
                        RainbowDot(
                            activeColor = if (isCustomColor) selectedColor else null,
                            onClick = {
                                showColorPicker = !showColorPicker
                                if (showColorPicker) isCustomColor = true
                            },
                        )
                    }

                    AnimatedVisibility(visible = showColorPicker) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(36.dp)
                                        .clip(MaterialTheme.shapes.small)
                                        .background(Color(red, green, blue)),
                                )
                                ChannelSlider(
                                    "R",
                                    red,
                                    { red = it; selectedColor = Color(red, green, blue) },
                                    Color(0xFFF44336)
                                )
                                ChannelSlider(
                                    "G",
                                    green,
                                    { green = it; selectedColor = Color(red, green, blue) },
                                    Color(0xFF4CAF50)
                                )
                                ChannelSlider(
                                    "B",
                                    blue,
                                    { blue = it; selectedColor = Color(red, green, blue) },
                                    Color(0xFF2196F3)
                                )
                            }
                        }
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionLabel("Теги")

                    if (availableTags.isEmpty() && !showNewTagInput) {
                        Text(
                            text = "Нет доступных тегов",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        )
                    }

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        availableTags.forEach { tag ->
                            key(tag.id) {
                                val selected = tag.id in selectedTagIds
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        selectedTagIds = if (selected) selectedTagIds - tag.id
                                        else selectedTagIds + tag.id
                                    },
                                    label = { Text(tag.name) },
                                    leadingIcon = if (selected) {
                                        {
                                            Icon(
                                                Icons.Default.Check, null,
                                                Modifier.size(18.dp),
                                            )
                                        }
                                    } else null,
                                    trailingIcon = {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Удалить тег ${tag.name}",
                                            modifier = Modifier
                                                .size(18.dp)
                                                .clip(CircleShape)
                                                .clickable {
                                                    selectedTagIds = selectedTagIds - tag.id
                                                    onDeleteTag(tag.id)
                                                }
                                                .padding(2.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                .copy(alpha = 0.6f),
                                        )
                                    },
                                )
                            }
                        }

                        if (!showNewTagInput) {
                            AssistChip(
                                onClick = { showNewTagInput = true },
                                label = { Text("Добавить") },
                                leadingIcon = {
                                    Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                                },
                            )
                        }
                    }

                    AnimatedVisibility(visible = showNewTagInput) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            DefaultTextField(
                                text = newTagName,
                                onValueChange = { newTagName = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("Тег") },
                            )
                            IconButton(
                                onClick = {
                                    if (newTagName.isNotBlank()) {
                                        onAddTag(Tag(name = newTagName.trim()))
                                        newTagName = ""
                                        showNewTagInput = false
                                    }
                                },
                                enabled = newTagName.isNotBlank(),
                            ) {
                                Icon(
                                    Icons.Default.Check, "Подтвердить",
                                    tint = if (newTagName.isNotBlank())
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
                                )
                            }
                            IconButton(
                                onClick = { newTagName = ""; showNewTagInput = false },
                            ) {
                                Icon(
                                    Icons.Default.Close, "Отмена",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    DefaultSecondaryButton(
                        label = "Отмена",
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                    )
                    DefaultPrimaryButton(
                        label = "Добавить",
                        onClick = {
                            val type = when (selectedType) {
                                PlacemarkTypeOption.SIMPLE -> ArPlacemark.Type.Simple
                                PlacemarkTypeOption.TEXT -> ArPlacemark.Type.Text(text)
                            }
                            onConfirm(
                                ArPlacemark(
                                    id = Random.nextLong(),
                                    name = name,
                                    type = type,
                                    locationData = locationData,
                                    color = selectedColor,
                                    tags = availableTags.filter { it.id in selectedTagIds },
                                    isWallAnchor = isWallAnchor,
                                )
                            )
                        },
                        modifier = Modifier.weight(1f),
                        enabled = isValid,
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

@Composable
private fun ColorDot(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(color)
            .then(
                if (isSelected) Modifier.border(
                    2.5.dp, MaterialTheme.colorScheme.onSurface, CircleShape,
                ) else Modifier
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (color.luminance() > 0.5f) Color.Black else Color.White,
            )
        }
    }
}

@Composable
private fun RainbowDot(
    activeColor: Color?,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .then(if (activeColor != null) Modifier.background(activeColor) else Modifier)
            .border(
                width = 2.dp,
                brush = Brush.sweepGradient(
                    listOf(
                        Color.Red, Color.Yellow, Color.Green,
                        Color.Cyan, Color.Blue, Color.Magenta, Color.Red,
                    )
                ),
                shape = CircleShape,
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (activeColor != null) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = if (activeColor.luminance() > 0.5f) Color.Black else Color.White,
            )
        }
    }
}

@Composable
private fun ChannelSlider(
    label: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    color: Color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.width(14.dp),
        )
        Slider(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(
                thumbColor = color,
                activeTrackColor = color,
                inactiveTrackColor = color.copy(alpha = 0.24f),
            ),
        )
        Text(
            text = "${(value * 255).roundToInt()}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(28.dp),
            textAlign = TextAlign.End,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddPlacemarkDialogPreview() {
    MaterialTheme {
        AddPlacemarkDialog(
            locationData = LocationData(
                latitude = 55.7558,
                longitude = 37.6173,
                altitude = 150.0,
            ),
            isWallAnchor = false,
            onDismiss = {},
            onConfirm = {},
            onAddTag = {},
            onDeleteTag = {},
            availableTags = emptyList(),
        )
    }
}