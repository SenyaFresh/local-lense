package ru.hse.locallense.components.composables

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A Composable function that displays an icon with a label, arranged in a vertical layout.
 * The icon is clickable and performs an action when clicked. The color of the icon and background
 * can be customized, and the text label below the icon provides additional context.
 *
 * @param imageVector The [ImageVector] for the icon to display.
 * @param text The text label displayed below the icon.
 * @param contentColor The color to apply to the icon and text.
 * @param containerColor The background color of the container that holds the icon and text.
 * @param onClick The action to be performed when the icon is clicked.
 * @param modifier A [Modifier] to apply to the outer layout, allowing customization of its appearance.
 */
@Composable
fun ActionIcon(
    imageVector: ImageVector,
    text: String,
    contentColor: Color,
    containerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier
            .clickable { onClick() }
            .background(color = containerColor)
            .width(60.dp)
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = text,
            tint = contentColor
        )

        Text(text = text, color = contentColor, fontSize = 8.sp, textAlign = TextAlign.Center)

    }

}