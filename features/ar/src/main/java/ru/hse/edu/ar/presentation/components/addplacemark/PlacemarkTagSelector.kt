package ru.hse.edu.ar.presentation.components.addplacemark

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import ru.hse.edu.ar.R
import ru.hse.locallense.common.entities.Tag
import ru.hse.locallense.components.composables.inputs.DefaultTextField

@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun PlacemarkTagSelector(
    availableTags: List<Tag>,
    selectedTagIds: Set<Long>,
    onToggleTag: (Long) -> Unit,
    onAddTag: (Tag) -> Unit,
    onDeleteTag: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isInputVisible by remember { mutableStateOf(false) }
    var newTagName by remember { mutableStateOf("") }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        PlacemarkSectionLabel(stringResource(R.string.ar_section_tags))

        if (availableTags.isEmpty() && !isInputVisible) {
            Text(
                text = stringResource(R.string.ar_tags_empty),
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
                    TagChip(
                        tag = tag,
                        isSelected = tag.id in selectedTagIds,
                        onToggle = { onToggleTag(tag.id) },
                        onDelete = { onDeleteTag(tag.id) },
                    )
                }
            }

            if (!isInputVisible) {
                AssistChip(
                    onClick = { isInputVisible = true },
                    label = { Text(stringResource(R.string.ar_action_add)) },
                    leadingIcon = {
                        Icon(Icons.Default.Add, null, Modifier.size(18.dp))
                    },
                )
            }
        }

        AnimatedVisibility(visible = isInputVisible) {
            NewTagInputRow(
                value = newTagName,
                onValueChange = { newTagName = it },
                onConfirm = {
                    if (newTagName.isNotBlank()) {
                        onAddTag(Tag(name = newTagName.trim()))
                        newTagName = ""
                        isInputVisible = false
                    }
                },
                onCancel = {
                    newTagName = ""
                    isInputVisible = false
                },
            )
        }
    }
}

@Composable
private fun TagChip(
    tag: Tag,
    isSelected: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
) {
    FilterChip(
        selected = isSelected,
        onClick = onToggle,
        label = { Text(tag.name) },
        leadingIcon = if (isSelected) {
            { Icon(Icons.Default.Check, null, Modifier.size(18.dp)) }
        } else null,
        trailingIcon = {
            Icon(
                Icons.Default.Close,
                contentDescription = stringResource(R.string.ar_tag_remove_cd, tag.name),
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onDelete)
                    .padding(2.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            )
        },
    )
}

@Composable
private fun NewTagInputRow(
    value: String,
    onValueChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        DefaultTextField(
            text = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text(stringResource(R.string.ar_field_tag_placeholder)) },
        )
        IconButton(
            onClick = onConfirm,
            enabled = value.isNotBlank(),
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = stringResource(R.string.ar_action_confirm),
                tint = if (value.isNotBlank())
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f),
            )
        }
        IconButton(onClick = onCancel) {
            Icon(
                Icons.Default.Close,
                contentDescription = stringResource(R.string.ar_action_cancel),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
