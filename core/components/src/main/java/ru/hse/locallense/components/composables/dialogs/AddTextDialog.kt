package ru.hse.locallense.components.composables.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import ru.hse.locallense.components.R
import ru.hse.locallense.components.composables.buttons.DefaultPrimaryButton
import ru.hse.locallense.components.composables.buttons.DefaultSecondaryButton
import ru.hse.locallense.components.composables.inputs.DefaultTextField
import ru.hse.locallense.presentation.locals.LocalSpacing

/**
 * A Composable function that displays a dialog with a text input field and confirm/cancel actions.
 *
 * This function creates a dialog that allows the user to input text, providing a confirm and cancel option.
 * It uses the `DefaultDialog` to render the dialog's framework, and includes a `DefaultTextField` for input and
 * two buttons (`DefaultSecondaryButton` for cancel and `DefaultPrimaryButton` for confirm).
 * The input text is validated before triggering the confirm action.
 *
 * @param title The title displayed at the top of the dialog.
 * @param placeholder The placeholder text displayed within the input field.
 * @param onConfirm A lambda function that is triggered when the confirm button is clicked, passing the entered text.
 *                  This function will only be called if the input text is not blank.
 * @param onCancel A lambda function that is triggered when the cancel button is clicked or when the dialog is dismissed.
 * @param modifier An optional [Modifier] to customize the appearance or behavior of the dialog.
 */
@Composable
fun AddTextDialog(
    title: String,
    placeholder: String,
    onConfirm: (String) -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var textInput by remember { mutableStateOf("") }

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
            // New category input.
            DefaultTextField(
                text = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text(placeholder) }
            )

            // Cancel and Confirm buttons.
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(LocalSpacing.current.small)
            ) {
                DefaultSecondaryButton(
                    label = stringResource(R.string.cancel),
                    onClick = onCancel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
                DefaultPrimaryButton(
                    label = stringResource(R.string.confirm),
                    onClick = {
                        if (textInput.isNotBlank()) {
                            onConfirm(textInput)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun AddTextDialogPreview() {
    Box(modifier = Modifier.fillMaxSize()) {
        AddTextDialog(
            title = "Добавить что-то",
            placeholder = "Введите что-то сюда",
            onConfirm = {},
            onCancel = {}
        )
    }
}