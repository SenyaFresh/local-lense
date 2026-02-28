package ru.hse.locallense.components.composables.environment

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview

/**
 * A Composable function that displays a search bar with text input and cancel functionality.
 *
 * This search bar allows the user to enter search text, provides a search icon on the left,
 * and a cancel icon on the right that clears the text. The search bar is customizable with
 * the label and can be customized further using a [Modifier].
 *
 * @param text The current text value of the search input field.
 * @param onValueChange A lambda function that is called when the text value changes. It receives the new text value.
 * @param onCancelClick A lambda function that is triggered when the cancel icon is clicked, usually to clear the text.
 * @param label The label displayed inside the text field to guide the user.
 * @param modifier An optional [Modifier] to customize the appearance or behavior of the search bar.
 */
@Composable
fun SearchBar(
    text: String,
    onValueChange: (String) -> Unit,
    onCancelClick: () -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {

    TextField(
        value = text,
        onValueChange = onValueChange,
        leadingIcon = {
            Icon(imageVector = Icons.Default.Search, contentDescription = null)
        },
        trailingIcon = {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = null,
                modifier = Modifier.clickable {
                    onCancelClick()
                }
            )
        },
        label = { Text(label) },
        shape = CircleShape,
        colors = TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Preview
@Composable
fun SearchBarPreview() {
    SearchBar(text = "", onValueChange = {}, onCancelClick = {}, label = "Поиск")
}