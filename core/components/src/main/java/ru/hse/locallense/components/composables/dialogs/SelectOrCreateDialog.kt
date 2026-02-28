package ru.hse.locallense.components.composables.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.hse.locallense.common.ResultContainer
import ru.hse.locallense.components.composables.buttons.DefaultIconButton
import ru.hse.locallense.components.composables.buttons.DefaultPrimaryButton
import ru.hse.locallense.components.composables.buttons.DefaultSecondaryButton
import ru.hse.locallense.components.composables.inputs.DefaultTextField
import ru.hse.locallense.components.composables.lists.ActionableItemsFlowRow
import ru.hse.locallense.components.entities.ActionableItem
import ru.hse.locallense.presentation.locals.LocalSpacing

/**
 * A Composable function that displays a dialog allowing the user to select an existing item or create a new one.
 *
 * This dialog displays a list of selectable items, with the option to create a new item by entering text in a text field.
 * The user can confirm or cancel the action. If a new item is created, the corresponding callback is triggered.
 * The dialog provides buttons for confirming or canceling the selection and an option to reload the list of items.
 *
 * @param title The title displayed at the top of the dialog.
 * @param items A [ResultContainer] containing the list of [ActionableItem]s to be displayed as selectable options.
 * @param initialItem The item to be selected initially in case no item is chosen by the user.
 * @param textFieldPlaceholder The placeholder text displayed inside the input field for adding new items.
 * @param onReloadItems A lambda function that is called when the user requests to reload the list of items.
 * @param onConfirm A lambda function triggered when the user confirms their selection. It receives the selected [ActionableItem].
 * @param confirmLabel The label displayed on the confirm button.
 * @param onCancel A lambda function that is triggered when the cancel button is clicked or when the dialog is dismissed.
 * @param cancelLabel The label displayed on the cancel button.
 * @param onAddNewItem A lambda function that is called when the user adds a new item via the text field.
 * @param modifier An optional [Modifier] to customize the appearance or behavior of the dialog.
 * @param initialActiveItemId An optional initial item ID that is pre-selected in the dialog. It defaults to null if no initial selection is made.
 */
@Composable
fun SelectOrCreateDialog(
    title: String,
    items: ResultContainer<List<ActionableItem>>,
    initialItem: ActionableItem,
    textFieldPlaceholder: String,
    onReloadItems: () -> Unit,
    onConfirm: (ActionableItem) -> Unit,
    confirmLabel: String,
    onCancel: () -> Unit,
    cancelLabel: String,
    onAddNewItem: (String) -> Unit,
    modifier: Modifier = Modifier,
    initialActiveItemId: Long? = null
) {
    var newItemText by remember { mutableStateOf("") }
    var activeItemId by remember { mutableStateOf(initialActiveItemId) }

    DefaultDialog(
        onDismiss = onCancel,
        title = title,
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(LocalSpacing.current.medium),
            modifier = modifier
                .padding(LocalSpacing.current.extraSmall)
                .fillMaxWidth()
        ) {
            ActionableItemsFlowRow(
                items = items,
                onItemClick = { activeItemId = it },
                onReloadItems = onReloadItems,
                activeItemId = activeItemId,
                modifier = Modifier
                    .heightIn(max = 120.dp)
                    .verticalScroll(rememberScrollState()),
                leadingItem = initialItem
            )

            DefaultTextField(
                text = newItemText,
                onValueChange = { newItemText = it },
                placeholder = { Text(textFieldPlaceholder) },
                trailingIcon = {
                    DefaultIconButton(
                        onClick = {
                            if (newItemText.isNotBlank()) {
                                onAddNewItem(newItemText)
                                newItemText = ""
                            }
                        },
                        enabled = items is ResultContainer.Done
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Add new item",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            )

            // Cancel and Confirm buttons.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(LocalSpacing.current.small)
            ) {
                DefaultSecondaryButton(
                    label = cancelLabel,
                    onClick = onCancel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
                DefaultPrimaryButton(
                    label = confirmLabel,
                    onClick = {
                        onConfirm(
                            getSelectedCategory(items.unwrapOrNull(), activeItemId) ?: initialItem
                        )
                    },
                    enabled = items is ResultContainer.Done,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }
    }
}

private fun getSelectedCategory(categories: List<ActionableItem>?, id: Long?): ActionableItem? {
    return categories?.firstOrNull { it.id == id }
}

@Preview(showSystemUi = true)
@Composable
fun SelectOrCreateDialogPreview() {
    Box(modifier = Modifier.fillMaxSize()) {
        SelectOrCreateDialog(
            title = "Выберите категорию",
            items = ResultContainer.Done(
                (1..10).map {
                    ActionableItem(id = it.toLong(), name = "Category $it")
                }
            ),
            onConfirm = { },
            onCancel = { },
            onReloadItems = { },
            onAddNewItem = { },
            initialItem = ActionableItem(
                id = 0,
                name = "Без категории"
            ),
            textFieldPlaceholder = "Введите название категории",
            confirmLabel = "Продолжить",
            cancelLabel = "Отмена"
        )
    }
}