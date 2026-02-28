package ru.hse.locallense.components.composables.inputs

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/**
 * A Composable function that displays a radio button with associated text.
 *
 * This component combines a radio button and a label in a row. The radio button can be selected or unselected,
 * and it allows for custom click behavior. The radio button's color and text color are customizable.
 *
 * @param text The text label displayed next to the radio button.
 * @param selected A boolean value that determines whether the radio button is selected.
 * @param onClick A lambda function that is called when the radio button is clicked. It should update the selection state.
 * @param textColor The color of the text label. The default is the inverse surface color from the theme.
 * @param selectedColor The color of the radio button when it is selected. The default is the primary color from the theme.
 * @param unselectedColor The color of the radio button when it is unselected. The default is the inverse surface color from the theme.
 */
@Composable
fun DefaultRadioItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.inverseSurface,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    unselectedColor: Color = MaterialTheme.colorScheme.inverseSurface
) = Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
        .fillMaxWidth()
        .clickable { onClick() }
) {
    RadioButton(
        selected = selected,
        colors = RadioButtonDefaults.colors(
            selectedColor = selectedColor,
            unselectedColor = unselectedColor
        ),
        onClick = { onClick() }
    )
    Text(text = text, color = textColor)
}