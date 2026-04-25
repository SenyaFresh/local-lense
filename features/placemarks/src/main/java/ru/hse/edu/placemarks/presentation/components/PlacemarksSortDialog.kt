package ru.hse.edu.placemarks.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.sp
import ru.hse.edu.placemarks.R
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
    title = stringResource(R.string.placemarks_sort_dialog_title),
    modifier = modifier
) {
    val labelsByPriority = mapOf(
        SortType.BY_NAME_ASC to R.string.placemarks_sort_by_name_asc,
        SortType.BY_NAME_DESC to R.string.placemarks_sort_by_name_desc,
        SortType.BY_DISTANCE_ASC to R.string.placemarks_sort_by_distance_asc,
        SortType.BY_DISTANCE_DESC to R.string.placemarks_sort_by_distance_desc,
    )

    Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {

        labelsByPriority.forEach { entry ->
            DefaultRadioItem(
                text = stringResource(entry.value),
                selected = sortType == entry.key,
                onClick = {
                    onSortTypeChange(entry.key)
                }
            )
        }

        TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) {
            Text(
                text = stringResource(R.string.placemarks_sort_dialog_ok),
                fontSize = 16.sp
            )
        }
    }
}