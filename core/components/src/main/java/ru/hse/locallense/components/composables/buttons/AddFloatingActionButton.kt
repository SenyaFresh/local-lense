package ru.hse.locallense.components.composables.buttons

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

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