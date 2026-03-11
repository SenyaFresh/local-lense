package ru.hse.edu.placemarks.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import ru.hse.edu.placemarks.presentation.entities.SortType
import ru.hse.locallense.components.composables.dialogs.DefaultDialog
import ru.hse.locallense.components.composables.inputs.DefaultRadioItem

@Composable
fun PlacemarksSortDialog(
    onDismiss: () -> Unit,
    onSortTypeChange: (SortType) -> Unit,
    sortType: SortType?,
    modifier: Modifier = Modifier
) = DefaultDialog(
    onDismiss = onDismiss,
    title = "Выберите тип сортировки",
    modifier = modifier
) {
    val labelsByPriority = mapOf(
        SortType.BY_NAME_ASC to "По названию (А-Я)",
        SortType.BY_NAME_DESC to "По названию (Я-А)",
        SortType.BY_DISTANCE_ASC to "По удалённости (ближайшие)",
        SortType.BY_DISTANCE_DESC to "По удалённости (далее)",
    )

    Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {

        labelsByPriority.forEach { entry ->
            DefaultRadioItem(
                text = entry.value,
                selected = sortType == entry.key,
                onClick = {
                    onSortTypeChange(entry.key)
                }
            )
        }

        TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
            Text(
                text = "Ок",
                fontSize = 16.sp
            )
        }
    }
}