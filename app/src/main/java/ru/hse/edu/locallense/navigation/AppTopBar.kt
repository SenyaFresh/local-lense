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