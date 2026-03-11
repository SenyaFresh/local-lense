package ru.hse.edu.placemarks.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.hse.locallense.common.ResultContainer
import ru.hse.locallense.common.entities.Tag
import ru.hse.locallense.components.composables.lists.ActionableItemsFlowRow
import ru.hse.locallense.components.entities.ActionableItem
import kotlin.collections.map

@Composable
fun TagsRow(
    tags: ResultContainer<List<Tag>>,
    onTagClick: (Long) -> Unit,
    activeTagsIds: List<Long> = emptyList(),
    modifier: Modifier = Modifier,
    errorModifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE
) {
    ActionableItemsFlowRow(
        items = tags.map { list -> list.map { ActionableItem(it.id, it.name) } },
        onItemClick = onTagClick,
        modifier = modifier,
        errorModifier = errorModifier,
        activeItemsIds = activeTagsIds,
        onReloadItems = { },
        maxLines = maxLines
    )
}