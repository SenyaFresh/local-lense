package ru.hse.locallense.components.composables.items

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import ru.hse.locallense.components.R
import ru.hse.locallense.components.composables.ActionIcon
import ru.hse.locallense.components.composables.shimmerEffect

/**
 * A Composable function that represents an actionable list item, which can be clicked and optionally deleted.
 *
 * The list item is displayed as a [Card] with a label and a delete icon. The background color changes depending on whether
 * the item is active or not. The delete icon is shown only if the `onDelete` lambda is provided. Clicking the list item
 * triggers the `onClick` action, and clicking the delete icon triggers the `onDelete` action if provided.
 *
 * @param label The label text to be displayed in the list item.
 * @param modifier A [Modifier] for custom layout and styling.
 * @param onClick A lambda that gets called when the list item is clicked.
 * @param onDelete A lambda that gets called when the delete icon is clicked, or null if no delete action is needed.
 * @param isActive A boolean value indicating whether the item is active. This alters the background color of the item.
 */
@Composable
fun ActionableListItem(
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = { },
    onDelete: (() -> Unit)? = null,
    isActive: Boolean = false,
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = if (!isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.primaryContainer),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(1.dp),
        onClick = onClick,
        modifier = modifier,
    ) {
        Row(
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label.uppercase(),
                color = if (!isActive) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 8.dp
                )
            )

            onDelete?.let {
                Spacer(modifier = Modifier.weight(1f))
                ActionIcon(
                    imageVector = Icons.Default.Delete,
                    text = stringResource(R.string.delete),
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.height(60.dp),
                    onClick = onDelete
                )
            }
        }
    }
}

@Composable
fun LoadingActionableListItem(withDelete: Boolean = false) {
    val modifier = if (withDelete) Modifier.fillMaxWidth() else Modifier
    Box(
        modifier = modifier
            .clip(shape = MaterialTheme.shapes.large)
            .height(
                if (withDelete) 50.dp else 30.dp
            )
            .widthIn(min = 72.dp)
            .shimmerEffect()
    ) {
        if (withDelete) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(color = MaterialTheme.colorScheme.onSurfaceVariant)
                    .align(Alignment.CenterEnd)
            )
        }
    }
}