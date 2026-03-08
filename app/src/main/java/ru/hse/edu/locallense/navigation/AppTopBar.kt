package ru.hse.edu.locallense.navigation

import androidx.annotation.StringRes
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource

data class IconAction(val imageVector: ImageVector, val onClick: () -> Unit)

/**
 * Composable function that displays a top app bar with a customizable title and icon actions.
 *
 * The `AppTopBar` is a wrapper around the `TopAppBar` that allows for the addition of a title,
 * a left navigation icon, and a list of right-side icons with corresponding click actions.
 *
 * @param titleRes The resource ID of the string to be displayed as the title (optional).
 * @param leftIconAction The action for the left-side icon (optional). If provided, the icon
 * is displayed in the navigation position.
 * @param rightIconsActions A list of actions for the right-side icons (optional). If provided,
 * multiple icons will be displayed in the actions area.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    @StringRes titleRes: Int? = null,
    leftIconAction: IconAction? = null,
    rightIconsActions: List<IconAction>? = null
) {
    TopAppBar(
        title = {
            titleRes?.let {
                Text(
                    text = stringResource(it),
                    style = MaterialTheme.typography.headlineSmall,
                )
            }
        },
        navigationIcon = {
            leftIconAction?.let {
                IconButton(onClick = it.onClick) {
                    Icon(
                        imageVector = it.imageVector,
                        contentDescription = null
                    )
                }
            }
        },
        actions = {
            rightIconsActions?.forEach {
                IconButton(onClick = it.onClick) {
                    Icon(
                        imageVector = it.imageVector,
                        contentDescription = null
                    )
                }
            }
        }
    )
}