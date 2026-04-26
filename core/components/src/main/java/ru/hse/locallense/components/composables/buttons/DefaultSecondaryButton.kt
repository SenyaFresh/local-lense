package ru.hse.locallense.components.composables.buttons

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

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