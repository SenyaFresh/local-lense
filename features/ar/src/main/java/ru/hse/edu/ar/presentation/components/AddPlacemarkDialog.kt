package ru.hse.edu.ar.presentation.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import ru.hse.edu.ar.domain.entities.ArPlacemark
import ru.hse.edu.geoar.ar.ArTapResult
import ru.hse.locallense.common.entities.LocationData
import ru.hse.locallense.components.composables.buttons.DefaultPrimaryButton
import ru.hse.locallense.components.composables.buttons.DefaultSecondaryButton
import ru.hse.locallense.components.composables.inputs.DefaultTextField
import kotlin.random.Random

enum class PlacemarkTypeOption(val label: String) {
    SIMPLE("Обычная"),
    TEXT("Текстовая"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddPlacemarkDialog(
    tapResult: ArTapResult,
    onDismiss: () -> Unit,
    onConfirm: (ArPlacemark) -> Unit,
) {
    var selectedType by remember { mutableStateOf(PlacemarkTypeOption.SIMPLE) }
    var name by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }

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
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text = "Новая заметка",
                    style = MaterialTheme.typography.headlineSmall,
                )

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    PlacemarkTypeOption.entries.forEachIndexed { index, option ->
                        SegmentedButton(
                            selected = selectedType == option,
                            onClick = { selectedType = option },
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = PlacemarkTypeOption.entries.size,
                            ),
                        ) {
                            Text(option.label)
                        }
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
                                    locationData = tapResult.locationData,
                                    color = Color(0xFF7C4DFF),
                                    tags = emptyList(),
                                    isWallAnchor = tapResult.isWall,
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

@Preview(showBackground = true)
@Composable
private fun AddPlacemarkDialogPreview() {
    MaterialTheme {
        AddPlacemarkDialog(
            tapResult = ArTapResult(
                locationData = LocationData(
                    latitude = 55.7558,
                    longitude = 37.6173,
                    altitude = 150.0,
                ),
                isWall = false,
            ),
            onDismiss = {},
            onConfirm = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AddPlacemarkDialogFilledPreview() {
    MaterialTheme {
        var shown by remember { mutableStateOf(true) }
        if (shown) {
            AddPlacemarkDialog(
                tapResult = ArTapResult(
                    locationData = LocationData(
                        latitude = 55.7558,
                        longitude = 37.6173,
                        altitude = 150.0,
                    ),
                    isWall = true,
                ),
                onDismiss = { shown = false },
                onConfirm = { shown = false },
            )
        }
    }
}