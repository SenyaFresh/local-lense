package ru.hse.edu.ar.presentation.components.preparations

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.hse.edu.ar.R
import ru.hse.edu.ar.presentation.components.heading.formatHeading

@Composable
internal fun HeadingInfoCard(
    heading: Float?,
    isCustomHeading: Boolean,
    onChangeClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val accentColor = MaterialTheme.colorScheme.primary

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        tonalElevation = 2.dp,
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .background(
                            color = accentColor.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(14.dp),
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Explore,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(40.dp),
                    )
                }

                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = stringResource(
                            if (isCustomHeading) R.string.ar_preparations_heading_custom_title
                            else R.string.ar_preparations_heading_auto_title
                        ),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = heading?.let(::formatHeading)
                            ?: stringResource(R.string.ar_preparations_heading_auto_value),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = stringResource(
                            if (isCustomHeading) R.string.ar_preparations_heading_locked_hint
                            else R.string.ar_preparations_heading_change_hint
                        ),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            FilledTonalButton(
                onClick = onChangeClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.filledTonalButtonColors(
                    containerColor = accentColor.copy(alpha = 0.12f),
                    contentColor = accentColor,
                ),
            ) {
                Text(
                    text = stringResource(
                        if (isCustomHeading) R.string.ar_preparations_heading_change_action
                        else R.string.ar_preparations_heading_set_action
                    ),
                    fontWeight = FontWeight.Medium,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HeadingInfoCardPreview() {
    MaterialTheme {
        HeadingInfoCard(
            heading = 86f,
            isCustomHeading = true,
            onChangeClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}
