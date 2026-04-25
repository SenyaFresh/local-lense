package ru.hse.edu.locallense.navigation.graphs

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import ru.hse.edu.locallense.navigation.ArGraph
import ru.hse.edu.locallense.navigation.ArNavMode
import ru.hse.edu.locallense.navigation.MapGraph
import ru.hse.edu.locallense.navigation.MapNavMode
import ru.hse.edu.locallense.navigation.PlacemarksGraph
import ru.hse.edu.placemarks.presentation.screens.PlacemarksScreen

internal fun NavGraphBuilder.placemarksGraph(
    navController: NavController,
    isSearchEnabled: () -> Boolean,
    onSearchEnabledChange: (Boolean) -> Unit,
    onActiveChanged: () -> Unit,
) {
    navigation<PlacemarksGraph>(
        startDestination = PlacemarksGraph.PlacemarksScreen,
    ) {
        composable<PlacemarksGraph.PlacemarksScreen> {
            onActiveChanged()
            PlacemarksScreen(
                searchEnabled = isSearchEnabled(),
                onSearchEnabledChange = onSearchEnabledChange,
                onPlacemarkOpenOnMap = { id ->
                    navController.navigate(
                        MapGraph.MapScreen(
                            navMode = MapNavMode.VIEW_SINGLE,
                            placemarkId = id,
                        )
                    )
                },
                onPlacemarkOpenInAr = { id ->
                    navController.navigate(
                        ArGraph.ArScreen(
                            navMode = ArNavMode.VIEW_SINGLE,
                            placemarkId = id,
                        )
                    )
                },
                onAddNewPlacemarkOnMap = {
                    navController.navigate(MapGraph.MapScreen(navMode = MapNavMode.ADD_NEW))
                },
                onAddNewPlacemarkInAr = {
                    navController.navigate(ArGraph.ArScreen(navMode = ArNavMode.ADD_NEW))
                },
            )
        }
    }
}
