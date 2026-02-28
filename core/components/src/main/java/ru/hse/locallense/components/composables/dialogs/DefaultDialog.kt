package ru.hse.locallense.components.composables.dialogs

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A Composable function that creates a dialog with a customizable title and content.
 *
 * This function creates a dialog using the `BasicAlertDialog` composable, which can be customized with a
 * title, content, and a dismiss action. The dialog includes a surface with rounded corners and tonal elevation.
 * The title is displayed at the top with a bold font, and the content is passed as a composable lambda.
 * The dialog can be dismissed when the user taps outside or when the dismiss action is triggered.
 *
 * @param onDismiss A lambda function that is executed when the dialog is dismissed, either by user interaction
 *                  (e.g., tapping outside) or other actions in the UI.
 * @param title The title text displayed at the top of the dialog. It is shown with a bold font and a font size of 18sp.
 * @param modifier An optional [Modifier] to customize the appearance or behavior of the dialog.
 * @param content A composable lambda that defines the content of the dialog, allowing flexibility to include
 *                any UI elements inside the dialog's body.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultDialog(
    onDismiss: () -> Unit,
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    BasicAlertDialog(
        onDismissRequest = onDismiss,
        modifier = modifier,
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            tonalElevation = AlertDialogDefaults.TonalElevation,
            shape = MaterialTheme.shapes.medium
        ) {
            Column(horizontalAlignment = Alignment.Start, modifier = Modifier.padding(14.dp)) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .padding(14.dp)
                        .align(Alignment.CenterHorizontally)
                )
                content()
            }
        }
    }
}