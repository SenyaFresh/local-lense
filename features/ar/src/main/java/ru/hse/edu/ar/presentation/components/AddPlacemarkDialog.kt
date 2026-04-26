package ru.hse.edu.ar.presentation.components

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import ru.hse.edu.ar.R
import ru.hse.edu.ar.domain.entities.ArPlacemark
import ru.hse.edu.ar.presentation.components.addplacemark.PlacemarkColorPicker
import ru.hse.edu.ar.presentation.components.addplacemark.PlacemarkPhotoPicker
import ru.hse.edu.ar.presentation.components.addplacemark.PlacemarkTagSelector
import ru.hse.edu.ar.presentation.components.addplacemark.PlacemarkTypeOption
import ru.hse.edu.ar.presentation.components.addplacemark.PlacemarkTypeSelector
import ru.hse.edu.ar.presentation.components.addplacemark.copyImageUriToInternal
import ru.hse.locallense.common.entities.LocationData
import ru.hse.locallense.common.entities.Tag
import ru.hse.locallense.components.composables.buttons.DefaultPrimaryButton
import ru.hse.locallense.components.composables.buttons.DefaultSecondaryButton
import ru.hse.locallense.components.composables.inputs.DefaultTextField
import ru.hse.locallense.components.theme.PlacemarkPalette
import kotlin.random.Random

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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var selectedType by remember { mutableStateOf(PlacemarkTypeOption.SIMPLE) }
    var name by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    var photoPath by remember { mutableStateOf<String?>(null) }

    var selectedColor by remember { mutableStateOf(PlacemarkPalette.Default) }
    var isCustomColor by remember { mutableStateOf(false) }
    var isCustomPanelOpen by remember { mutableStateOf(false) }
    var red by remember { mutableFloatStateOf(PlacemarkPalette.Default.red) }
    var green by remember { mutableFloatStateOf(PlacemarkPalette.Default.green) }
    var blue by remember { mutableFloatStateOf(PlacemarkPalette.Default.blue) }

    var selectedTagIds by remember { mutableStateOf(emptySet<Long>()) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri ->
        if (uri != null) {
            scope.launch {
                val path = withContext(Dispatchers.IO) {
                    copyImageUriToInternal(context, uri)
                }
                if (path != null) photoPath = path
            }
        }
    }

    val isValid = name.isNotBlank() && when (selectedType) {
        PlacemarkTypeOption.SIMPLE -> true
        PlacemarkTypeOption.TEXT -> text.isNotBlank()
        PlacemarkTypeOption.PHOTO_TEXT -> text.isNotBlank() && photoPath != null
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
                    text = stringResource(R.string.ar_dialog_new_note_title),
                    style = MaterialTheme.typography.headlineSmall,
                )

                PlacemarkTypeSelector(
                    selected = selectedType,
                    onSelect = { selectedType = it },
                )

                DefaultTextField(
                    text = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text(stringResource(R.string.ar_field_name_placeholder)) },
                )

                AnimatedVisibility(visible = selectedType == PlacemarkTypeOption.TEXT) {
                    DefaultTextField(
                        text = text,
                        onValueChange = { text = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.ar_field_text_placeholder)) },
                    )
                }

                AnimatedVisibility(visible = selectedType == PlacemarkTypeOption.PHOTO_TEXT) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        PlacemarkPhotoPicker(
                            photoPath = photoPath,
                            onPick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            },
                            onClear = { photoPath = null },
                        )
                        DefaultTextField(
                            text = text,
                            onValueChange = { text = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text(stringResource(R.string.ar_field_photo_caption_placeholder)) },
                        )
                    }
                }

                PlacemarkColorPicker(
                    selectedColor = selectedColor,
                    isCustomColor = isCustomColor,
                    isCustomPanelOpen = isCustomPanelOpen,
                    red = red,
                    green = green,
                    blue = blue,
                    onPresetSelect = { color ->
                        selectedColor = color
                        isCustomColor = false
                        isCustomPanelOpen = false
                        red = color.red
                        green = color.green
                        blue = color.blue
                    },
                    onCustomToggle = {
                        isCustomPanelOpen = !isCustomPanelOpen
                        if (isCustomPanelOpen) isCustomColor = true
                    },
                    onChannelChange = { r, g, b ->
                        red = r
                        green = g
                        blue = b
                        selectedColor = Color(r, g, b)
                    },
                )

                PlacemarkTagSelector(
                    availableTags = availableTags,
                    selectedTagIds = selectedTagIds,
                    onToggleTag = { id ->
                        selectedTagIds = if (id in selectedTagIds) selectedTagIds - id
                        else selectedTagIds + id
                    },
                    onAddTag = onAddTag,
                    onDeleteTag = { id ->
                        selectedTagIds = selectedTagIds - id
                        onDeleteTag(id)
                    },
                )

                ConfirmationButtons(
                    isValid = isValid,
                    onCancel = onDismiss,
                    onConfirm = {
                        onConfirm(
                            buildPlacemark(
                                name = name,
                                text = text,
                                photoPath = photoPath,
                                type = selectedType,
                                color = selectedColor,
                                locationData = locationData,
                                isWallAnchor = isWallAnchor,
                                selectedTagIds = selectedTagIds,
                                availableTags = availableTags,
                            )
                        )
                    },
                )
            }
        }
    }
}

@Composable
private fun ConfirmationButtons(
    isValid: Boolean,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        DefaultSecondaryButton(
            label = stringResource(R.string.ar_action_cancel),
            onClick = onCancel,
            modifier = Modifier.weight(1f),
        )
        DefaultPrimaryButton(
            label = stringResource(R.string.ar_action_add),
            onClick = onConfirm,
            modifier = Modifier.weight(1f),
            enabled = isValid,
        )
    }
}

private fun buildPlacemark(
    name: String,
    text: String,
    photoPath: String?,
    type: PlacemarkTypeOption,
    color: Color,
    locationData: LocationData,
    isWallAnchor: Boolean,
    selectedTagIds: Set<Long>,
    availableTags: List<Tag>,
): ArPlacemark {
    val placemarkType = when (type) {
        PlacemarkTypeOption.SIMPLE -> ArPlacemark.Type.Simple
        PlacemarkTypeOption.TEXT -> ArPlacemark.Type.Text(text)
        PlacemarkTypeOption.PHOTO_TEXT -> ArPlacemark.Type.TextPhoto(
            text = text,
            photoPath = photoPath!!,
        )
    }
    return ArPlacemark(
        id = Random.nextLong(),
        name = name,
        type = placemarkType,
        locationData = locationData,
        color = color,
        tags = availableTags.filter { it.id in selectedTagIds },
        isWallAnchor = isWallAnchor,
    )
}
