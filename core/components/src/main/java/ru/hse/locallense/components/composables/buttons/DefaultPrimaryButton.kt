package ru.hse.locallense.components.composables.buttons

import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

/**
 * A Composable function that creates a customizable primary button with a label and default styling.
 *
 * This function wraps the `Button` composable to simplify its usage with default button colors,
 * a label for the button text, and an action triggered by the `onClick` lambda. It also allows
 * further customization via parameters such as `modifier` and `colors`.
 *
 * @param label The text label to be displayed on the button. It will be shown with a semi-bold font weight.
 * @param onClick A lambda function that is executed when the button is clicked. This is where the action
 *                triggered by the button should be handled.
 * @param modifier An optional [Modifier] to customize the appearance or behavior of the button.
 * @param colors An optional [ButtonColors] to customize the color scheme of the button. If not provided,
 *               default colors are used.
 */
@Composable
fun DefaultPrimaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: ButtonColors? = null,
    enabled: Boolean = true
) = Button(
    onClick = onClick,
    colors = colors ?: ButtonDefaults.buttonColors(),
    enabled = enabled,
    modifier = modifier
) {
    Text(
        text = label,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}
