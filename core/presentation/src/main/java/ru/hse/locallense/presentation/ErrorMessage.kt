package ru.hse.locallense.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow

/**
 * Displays an error message with a retry button that triggers an action when clicked.
 *
 * This composable allows showing a custom error message along with a button for retrying the action.
 * The button, when clicked, triggers the [onClickRetry] callback to handle the retry action.
 *
 * @param message The error message text to be displayed.
 * @param onClickRetry A callback function that is invoked when the retry button is clicked.
 * @param modifier Modifier to be applied to the composable.
 */
@Composable
fun ErrorMessage(
    message: String,
    onClickRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = message)
        Button(
            onClick = onClickRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ),
            modifier = Modifier
        ) {
            Text(
                text = stringResource(R.string.try_again),
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}