package ru.hse.edu.locallense.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.collections.immutable.ImmutableList

/**
 * A composable function that displays a navigation bar with tabs for navigating between
 * different sections of the app.
 *
 * @param navigationController The [NavController] used to navigate between the different routes.
 * @param tabs A list of [AppTab] objects representing the tabs to be displayed in the navigation bar.
 */
@Composable
fun AppNavigationBar(
    navigationController: NavController,
    tabs: ImmutableList<AppTab>
) {
    Column {
        HorizontalDivider(thickness = 1.dp)
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            val currentBackStackEntry = navigationController.currentBackStackEntryAsState()
            val closestNavGraph = currentBackStackEntry
                .value
                ?.destination
                ?.hierarchy
                ?.first { it is NavGraph }
                .routeClass()

            val currentTab = tabs.firstOrNull { it.graphRoute::class == closestNavGraph }

            tabs.forEach { tab ->
                NavigationBarItem(
                    selected = currentTab == tab,
                    onClick = {
                        if (currentTab != null) {
                            navigationController.navigate(tab.graphRoute) {
                                popUpTo(currentTab.graphRoute) {
                                    inclusive = true
                                    saveState = true
                                }
                                restoreState = true
                            }
                        }
                    },
                    icon = {
                        Icon(
                            imageVector = tab.imageVector,
                            contentDescription = stringResource(id = tab.titleRes),
                        )
                    }
                )
            }
        }
    }
}