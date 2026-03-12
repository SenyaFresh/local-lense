package ru.hse.edu.placemarks.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import ru.hse.locallense.presentation.locals.LocalSpacing

@Composable
fun AddPlacemarkMethodDialog(
    onDismiss: () -> Unit,
    onAddOnMap: () -> Unit,
    onAddInAr: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Добавить метку") },
        text = { Text("Выберите способ добавления") },
        confirmButton = {
            Row(
                horizontalArrangement = Arrangement.spacedBy(LocalSpacing.current.small)
            ) {
                TextButton(onClick = {
                    onDismiss()
                    onAddOnMap()
                }) {
                    Icon(Icons.Default.Map, contentDescription = null)
                    Spacer(Modifier.width(LocalSpacing.current.extraSmall))
                    Text("На карте")
                }
                TextButton(onClick = {
                    onDismiss()
                    onAddInAr()
                }) {
                    Icon(Icons.Default.ViewInAr, contentDescription = null)
                    Spacer(Modifier.width(LocalSpacing.current.extraSmall))
                    Text("В AR")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Отмена")
            }
        }
    )
}