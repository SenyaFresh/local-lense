package ru.hse.locallense.components.composables.buttons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A Composable function that creates a Floating Action Button (FAB) with an "Add" icon.
 *
 * This function wraps the FloatingActionButton composable and provides an easy way to display
 * a FAB with a default "Add" icon. It allows the user to trigger a custom action when the button
 * is clicked by passing a lambda to the `onClick` parameter. It also supports customization via the
 * `modifier` parameter.
 *
 * @param onClick A lambda function that is executed when the Floating Action Button is clicked.
 *                This is where the action triggered by the FAB should be handled.
 * @param modifier An optional [Modifier] to customize the appearance or behavior of the FAB.
 */
@Composable
fun AddFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) = FloatingActionButton(
    onClick = onClick,
    modifier = modifier
) {
    Icon(imageVector = Icons.Default.Add, contentDescription = null)
}