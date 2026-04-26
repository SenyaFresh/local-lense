package ru.hse.locallense.components.composables.lists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ContextualFlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import ru.hse.locallense.common.ResultContainer
import ru.hse.locallense.components.R
import ru.hse.locallense.components.composables.items.ActionableListItem
import ru.hse.locallense.components.composables.items.LoadingActionableListItem
import ru.hse.locallense.components.entities.ActionableItem
import ru.hse.locallense.presentation.ErrorMessage
import ru.hse.locallense.presentation.ResultContent
import ru.hse.locallense.presentation.locals.LocalSpacing

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ActionableItemsFlowRow(
    items: ResultContainer<List<ActionableItem>>,
    onReloadItems: () -> Unit,
    modifier: Modifier = Modifier,
    errorModifier: Modifier = Modifier,
    onItemClick: (Long) -> Unit = {},
    activeItemsIds: List<Long>? = null,
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
    } else {
        ResultContent(
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
            val displayedItems = items.unwrap()
            ContextualFlowRow(
                itemCount = displayedItems.size,
                maxLines = maxLines,
                horizontalArrangement = Arrangement.spacedBy(LocalSpacing.current.medium),
                verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.medium),
                modifier = Modifier
            ) { index ->
                val tag = displayedItems[index]
                val isActive = activeItemsIds?.contains(tag.id) ?: false
                key(tag.id, isActive) {
                    ActionableListItem(
                        label = tag.name,
                        isActive = isActive,
                        onClick = { onItemClick(tag.id) },
                        modifier = Modifier
                    )
                }
            }
        }
    }
}