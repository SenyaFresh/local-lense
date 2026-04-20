package ru.hse.edu.locallense.navigation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
    val currentBackStackEntry = navigationController.currentBackStackEntryAsState()
    val closestNavGraph = currentBackStackEntry
        .value
        ?.destination
        ?.hierarchy
        ?.first { it is NavGraph }
        .routeClass()

    val currentTab = tabs.firstOrNull { it.graphRoute::class == closestNavGraph }

    Surface(
        shadowElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            tabs.forEach { tab ->
                val isSelected = currentTab == tab
                val animatedWeight by animateFloatAsState(
                    targetValue = if (isSelected) 1.4f else 1f,
                    animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                    label = "tabWeight",
                )

                Box(
                    modifier = Modifier
                        .weight(animatedWeight)
                        .clip(RoundedCornerShape(16.dp))
                        .background(
                            color = if (isSelected)
                                MaterialTheme.colorScheme.secondaryContainer
                            else
                                Color.Transparent,
                        )
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                        ) {
                            if (currentTab != null) {
                                navigationController.navigate(tab.graphRoute) {
                                    popUpTo(currentTab.graphRoute) {
                                        inclusive = true
                                        saveState = true
                                    }
                                    restoreState = true
                                }
                            }
                        }
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                    ) {
                        Icon(
                            imageVector = tab.imageVector,
                            contentDescription = stringResource(id = tab.titleRes),
                            tint = if (isSelected)
                                MaterialTheme.colorScheme.onSecondaryContainer
                            else
                                MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(24.dp),
                        )

                        AnimatedVisibility(
                            visible = isSelected,
                            enter = fadeIn(tween(200)) + expandHorizontally(
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            ),
                            exit = fadeOut(tween(150)) + shrinkHorizontally(
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
                            ),
                        ) {
                            Row {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(id = tab.titleRes),
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}