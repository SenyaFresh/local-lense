package ru.hse.locallense.components.composables.buttons

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

/**
 * A Composable function that creates a customizable secondary button with a label and outlined style.
 *
 * This function wraps the `OutlinedButton` composable to simplify its usage with default outlined button colors,
 * a label for the button text, and an action triggered by the `onClick` lambda. The button is outlined with
 * a semi-bold font for the label, and the text will truncate with ellipsis if it exceeds one line.
 *
 * @param label The text label to be displayed on the button. It will be shown with a semi-bold font weight.
 * @param onClick A lambda function that is executed when the button is clicked. This is where the action
 *                triggered by the button should be handled.
 * @param modifier An optional [Modifier] to customize the appearance or behavior of the button.
 *                 The default is an empty modifier (`Modifier`), meaning no custom modifications are applied.
 */
@Composable
fun DefaultSecondaryButton(label: String, onClick: () -> Unit, modifier: Modifier = Modifier) =
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(),
        modifier = modifier
    ) {
        Text(
            text = label,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }