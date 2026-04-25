package ru.hse.edu.placemarks.presentation.screens

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import ru.hse.edu.placemarks.R
import ru.hse.edu.placemarks.di.PlacemarksDiContainer
import ru.hse.edu.placemarks.di.rememberPlacemarksDiContainer
import ru.hse.edu.placemarks.domain.entities.Placemark
import ru.hse.edu.placemarks.presentation.components.AddPlacemarkMethodDialog
import ru.hse.edu.placemarks.presentation.components.PlacemarksColumn
import ru.hse.edu.placemarks.presentation.components.PlacemarksSortDialog
import ru.hse.edu.placemarks.presentation.components.TagsRow
import ru.hse.edu.placemarks.presentation.entities.SortType
import ru.hse.edu.placemarks.presentation.events.PlacemarkEvent
import ru.hse.edu.placemarks.presentation.viewmodels.PlacemarksViewModel
import ru.hse.locallense.common.ResultContainer
import ru.hse.locallense.common.entities.Tag
import ru.hse.locallense.components.composables.buttons.AddFloatingActionButton
import ru.hse.locallense.components.composables.environment.SearchBar
import ru.hse.locallense.presentation.locals.LocalSpacing

@Composable
fun PlacemarksScreen(
    diContainer: PlacemarksDiContainer = rememberPlacemarksDiContainer(),
    viewModel: PlacemarksViewModel = viewModel(factory = diContainer.viewModelFactory),
    searchEnabled: Boolean,
    onSearchEnabledChange: (Boolean) -> Unit,
    onPlacemarkOpenOnMap: (Long) -> Unit,
    onPlacemarkOpenInAr: (Long) -> Unit,
    onAddNewPlacemarkOnMap: () -> Unit,
    onAddNewPlacemarkInAr: () -> Unit,
) {
    val placemarks by viewModel.placemarks.collectAsState()
    val tags by viewModel.tags.collectAsState()
    val selectedTagIds by viewModel.selectedTagIds.collectAsState()
    val selectedSortType by viewModel.sortType.collectAsState()
    val currentSearchText by viewModel.searchQuery.collectAsState()

    var showAddMethodDialog by remember { mutableStateOf(false) }

    if (showAddMethodDialog) {
        AddPlacemarkMethodDialog(
            onDismiss = { showAddMethodDialog = false },
            onAddOnMap = onAddNewPlacemarkOnMap,
            onAddInAr = onAddNewPlacemarkInAr,
        )
    }

    PlacemarksContent(
        placemarks = placemarks,
        tags = tags,
        selectedTagIds = selectedTagIds,
        selectedSortType = selectedSortType,
        searchEnabled = searchEnabled,
        currentSearchText = currentSearchText,
        onSearchEnabledChange = onSearchEnabledChange,
        onPlacemarkOpenOnMap = onPlacemarkOpenOnMap,
        onPlacemarkOpenInAr = onPlacemarkOpenInAr,
        onPlacemarkEvent = viewModel::onEvent,
    )

    Box(modifier = Modifier.fillMaxSize()) {
        AddFloatingActionButton(
            onClick = { showAddMethodDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(LocalSpacing.current.medium)
        )
    }
}

@Composable
fun PlacemarksContent(
    searchEnabled: Boolean,
    currentSearchText: String,
    onSearchEnabledChange: (Boolean) -> Unit,
    placemarks: ResultContainer<List<Placemark>>,
    tags: ResultContainer<List<Tag>>,
    selectedTagIds: List<Long>,
    selectedSortType: SortType,
    onPlacemarkOpenOnMap: (Long) -> Unit,
    onPlacemarkOpenInAr: (Long) -> Unit,
    onPlacemarkEvent: (PlacemarkEvent) -> Unit,
) {

    var showSortTypeDialog by remember { mutableStateOf(false) }

    if (showSortTypeDialog) {
        PlacemarksSortDialog(
            onDismiss = { showSortTypeDialog = false },
            onSortTypeChange = {
                onPlacemarkEvent(PlacemarkEvent.SortBy(it))
            },
            sortType = selectedSortType
        )
    }
    Column {
        Crossfade(
            targetState = searchEnabled,
            label = stringResource(R.string.placemarks_search_show_label),
            animationSpec = tween(100),
            modifier = Modifier.animateContentSize()
        ) { state ->
            if (state) {
                SearchBar(
                    text = currentSearchText,
                    onValueChange = { onPlacemarkEvent(PlacemarkEvent.SearchByName(it)) },
                    onCancelClick = {
                        onSearchEnabledChange(false)
                        onPlacemarkEvent(PlacemarkEvent.SearchByName(""))
                    },
                    label = stringResource(R.string.placemarks_search_label),
                    modifier = Modifier.padding(LocalSpacing.current.small)
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        enabled = tags is ResultContainer.Done,
                        onClick = { showSortTypeDialog = true },
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = null
                        )
                    }

                    TagsRow(
                        tags = tags,
                        activeTagsIds = selectedTagIds,
                        onTagClick = { onPlacemarkEvent(PlacemarkEvent.SelectTag(it)) },
                        maxLines = 1,
                        errorModifier = Modifier.height(80.dp),
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState())
                            .padding(end = LocalSpacing.current.small)
                    )
                }
            }
        }


        Spacer(modifier = Modifier.height(LocalSpacing.current.extraSmall))

        PlacemarksColumn(
            placemarks = placemarks,
            onPlacemarkDelete = { id -> onPlacemarkEvent(PlacemarkEvent.DeletePlacemark(id)) },
            onPlacemarkOpenOnMap = onPlacemarkOpenOnMap,
            onPlacemarkOpenInAr = onPlacemarkOpenInAr,
        )
    }
}