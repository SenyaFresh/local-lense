package ru.hse.locallense.components.composables.inputs

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle

/**
 * A Composable function that creates a customizable Outlined TextField with various features like placeholder, icons, and color.
 *
 * This TextField allows customization of the text style, container color, leading and trailing icons, and placeholder content.
 * It also handles text input and change events.
 *
 * @param text The current text value displayed in the text field.
 * @param onValueChange A lambda function that is called when the text field value changes. It should update the `text` state.
 * @param modifier A [Modifier] to apply custom layout and styling to the TextField.
 * @param containerColor The background color of the text field container. Defaults to the surface container color.
 * @param textStyle The style applied to the text. Defaults to the default `TextStyle`.
 * @param placeholder A composable for the placeholder text shown when the text field is empty. Can be null.
 * @param leadingIcon A composable for a leading icon displayed inside the text field. Can be null.
 * @param trailingIcon A composable for a trailing icon displayed inside the text field. Can be null.
 */
@Composable
fun DefaultTextField(
    text: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surfaceContainerLow,
    textStyle: TextStyle = TextStyle.Default,
    placeholder: @Composable (() -> Unit)? = null,
    leadingIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null
) = OutlinedTextField(
    value = text,
    onValueChange = onValueChange,
    colors = OutlinedTextFieldDefaults.colors(
        focusedContainerColor = containerColor,
        focusedBorderColor = Color.Transparent,
        unfocusedContainerColor = containerColor,
        unfocusedBorderColor = Color.Transparent,
    ),
    textStyle = textStyle,
    placeholder = placeholder,
    leadingIcon = leadingIcon,
    trailingIcon = trailingIcon,
    modifier = modifier
)