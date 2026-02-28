package ru.hse.locallense.components.composables.lists

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.hse.locallense.common.ResultContainer
import ru.hse.locallense.components.composables.buttons.AddFloatingActionButton
import ru.hse.locallense.components.composables.dialogs.AddTextDialog
import ru.hse.locallense.components.composables.items.ActionableListItem
import ru.hse.locallense.components.composables.items.LoadingActionableListItem
import ru.hse.locallense.components.entities.ActionableItem
import ru.hse.locallense.presentation.ResultContainerComposable
import ru.hse.locallense.presentation.locals.LocalSpacing

/**
 * A Composable function that displays a list of actionable items with the ability to add new items.
 * If the list is empty, a message is displayed. The function handles displaying items, adding new ones, and
 * deleting items. It also provides feedback for loading states and includes an option for reloading the items.
 *
 * @param items The list of actionable items wrapped in a [ResultContainer], which can represent loading, success, or failure states.
 * @param onReloadItems A lambda to reload the items if necessary, such as when the data fails to load.
 * @param emptyListMessage The message to display when the list of items is empty.
 * @param onDelete A lambda function that is called when an item is deleted, receiving the item's ID.
 * @param addLabel The label to display for the add dialog's title.
 * @param addPlaceholder The placeholder text to show inside the text field when adding a new item.
 * @param onAdd An optional lambda that is called when a new item is added, receiving the item's name.
 */
@Composable
fun ActionableItemsListWithAdding(
    items: ResultContainer<List<ActionableItem>>,
    onReloadItems: () -> Unit,
    emptyListMessage: String,
    onDelete: (Long) -> Unit,
    addLabel: String,
    addPlaceholder: String,
    onAdd: ((String) -> Unit)? = null
) {
    var isAddingNewItem by remember { mutableStateOf(false) }

    ResultContainerComposable(
        container = items,
        onTryAgain = onReloadItems,
        onLoading = {
            Column(
                verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.semiMedium),
                modifier = Modifier.padding(20.dp)
            ) {
                repeat(5) {
                    LoadingActionableListItem(withDelete = true)
                }
            }
        }
    ) {
        if (items.unwrap().isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = emptyListMessage,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(LocalSpacing.current.medium)
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.semiMedium),
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                items(
                    items = items.unwrap(),
                    key = { actionableItem -> actionableItem.id }) { actionableItem ->
                    ActionableListItem(
                        label = actionableItem.name,
                        onDelete = { onDelete(actionableItem.id) },
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }

        if (onAdd != null) {
            if (isAddingNewItem) {
                AddTextDialog(
                    title = addLabel,
                    placeholder = addPlaceholder,
                    onConfirm = { categoryName ->
                        onAdd(categoryName)
                        isAddingNewItem = false
                    },
                    onCancel = { isAddingNewItem = false }
                )
            } else {
                Box(modifier = Modifier.fillMaxSize()) {
                    AddFloatingActionButton(
                        onClick = { isAddingNewItem = true },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(LocalSpacing.current.medium)
                    )
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun CategoriesContentPreview() {
    val items = (1..5).map {
        ActionableItem(it.toLong(), "Category $it")
    }
    ActionableItemsListWithAdding(
        ResultContainer.Done(items),
        addLabel = "Add category",
        addPlaceholder = "Category name",
        onDelete = { },
        onAdd = { },
        emptyListMessage = "List is empty",
        onReloadItems = { },
    )
}

@Preview(showSystemUi = true)
@Composable
fun CategoriesContentLoadingPreview() {
    ActionableItemsListWithAdding(
        ResultContainer.Loading,
        addLabel = "Add category",
        addPlaceholder = "Category name",
        onDelete = { },
        onAdd = { },
        emptyListMessage = "List is empty",
        onReloadItems = { },
    )
}