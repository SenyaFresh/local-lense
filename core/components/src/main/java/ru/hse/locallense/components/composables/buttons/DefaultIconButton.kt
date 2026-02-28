package ru.hse.locallense.components.composables.buttons

import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonColors
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A Composable function that creates a customizable IconButton with default colors and functionality.
 *
 * This function wraps the `IconButton` composable to simplify its usage with default color schemes
 * and optional customizations. It allows you to customize the button's action with the `onClick` lambda,
 * and also lets you modify the appearance and behavior through parameters such as `modifier`, `enabled`,
 * `colors`, and `content`.
 *
 * @param onClick A lambda function that is executed when the IconButton is clicked. This is where the action
 *                triggered by the button should be handled.
 * @param modifier An optional [Modifier] to customize the appearance or behavior of the IconButton.
 * @param enabled A Boolean value that determines whether the IconButton is enabled or disabled.
 * @param colors An optional [IconButtonColors] to customize the color scheme of the IconButton. If not provided,
 *               default colors are used based on the Material theme.
 * @param content A composable lambda function to define the content of the IconButton.
 *                Typically, this would be an icon or other composables to display inside the button.
 */
@Composable
fun DefaultIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors? = null,
    content: @Composable () -> Unit
) = IconButton(
    onClick = onClick,
    colors = colors ?: IconButtonDefaults.iconButtonColors(
        contentColor = MaterialTheme.colorScheme.onPrimary,
        containerColor = MaterialTheme.colorScheme.primary,
        disabledContentColor = MaterialTheme.colorScheme.onPrimary,
        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
    ),
    enabled = enabled,
    modifier = modifier
) {
    content()
}