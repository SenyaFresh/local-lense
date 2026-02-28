package ru.hse.locallense.components.composables.lists

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ContextualFlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ru.hse.locallense.common.ResultContainer
import ru.hse.locallense.components.R
import ru.hse.locallense.components.composables.items.ActionableListItem
import ru.hse.locallense.components.composables.items.LoadingActionableListItem
import ru.hse.locallense.components.entities.ActionableItem
import ru.hse.locallense.presentation.ErrorMessage
import ru.hse.locallense.presentation.ResultContainerComposable
import ru.hse.locallense.presentation.locals.LocalSpacing

/**
 * A Composable function that displays a row of actionable items in a flow layout. It supports dynamic item loading,
 * displays loading states, and provides a callback for item click events.
 *
 * The items can be passed as a [ResultContainer], allowing for different states like loading, success, and failure.
 * The function optionally supports displaying a "leading item" at the beginning of the list. Each item is rendered as
 * an [ActionableListItem], and the `onItemClick` lambda is triggered when an item is clicked. The layout ensures that
 * items are spaced out evenly and are capable of handling multiple lines of items.
 *
 * @param items The list of actionable items wrapped in a [ResultContainer] that handles loading, success, and failure states.
 * @param onReloadItems A lambda function to reload the items if the data fails to load or requires refreshing.
 * @param modifier A [Modifier] for custom layout and styling of the row.
 * @param onItemClick A lambda function that is called when an item is clicked, passing the item's ID.
 * @param activeItemId The ID of the currently active item, if any. The active item will be styled differently.
 * @param leadingItem An optional item that will be displayed first in the list, before the other items.
 * @param maxLines The maximum number of lines to display the items in. Defaults to [Int.MAX_VALUE] for unlimited lines.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActionableItemsFlowRow(
    items: ResultContainer<List<ActionableItem>>,
    onReloadItems: () -> Unit,
    modifier: Modifier = Modifier,
    errorModifier: Modifier = Modifier,
    onItemClick: (Long) -> Unit = {},
    activeItemId: Long? = null,
    leadingItem: ActionableItem? = null,
    maxLines: Int = Int.MAX_VALUE
) {
    if (items is ResultContainer.Error) {
        Box(
            modifier = errorModifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            ErrorMessage(
                message = items.exception.message ?: stringResource(R.string.unknown_error),
                onClickRetry = onReloadItems,
                modifier = errorModifier
            )
        }
    }
    else{
        ResultContainerComposable(
            container = items,
            onTryAgain = onReloadItems,
            onLoading = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(LocalSpacing.current.medium),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = LocalSpacing.current.small)
                ) {
                    repeat(5) {
                        LoadingActionableListItem()
                    }
                }
            },
            modifier = modifier
        ) {
            val displayedItems = (leadingItem?.let {
                listOf(
                    leadingItem
                )
            } ?: emptyList()) + items.unwrap()
            ContextualFlowRow(
                itemCount = displayedItems.size,
                maxLines = maxLines,
                horizontalArrangement = Arrangement.spacedBy(LocalSpacing.current.medium),
                verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.medium),
                modifier = Modifier
            ) { index ->
                val category = displayedItems[index]
                key(category.id, activeItemId) {
                    ActionableListItem(
                        label = category.name,
                        isActive = category.id == activeItemId,
                        onClick = { onItemClick(category.id) },
                        modifier = Modifier
                    )
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun CategoriesRowPreview() {
    Box(modifier = Modifier.fillMaxSize()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ActionableItemsFlowRow(items = ResultContainer.Done(
                (1..5).map {
                    ActionableItem(it.toLong(), "Category $it")
                }
            ),
                onItemClick = { },
                onReloadItems = { },
                maxLines = 1,
                modifier = Modifier
                    .horizontalScroll(rememberScrollState(0))
            )
        }
    }
}