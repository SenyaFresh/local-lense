package ru.hse.edu.locallense.navigation.graphs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import ru.hse.edu.ar.presentation.screens.MapScreen
import ru.hse.edu.ar.presentation.screens.MapScreenMode
import ru.hse.edu.locallense.navigation.MapGraph
import ru.hse.edu.locallense.navigation.MapNavMode
import ru.hse.edu.locallense.navigation.PlacemarksGraph

internal fun NavGraphBuilder.mapGraph(
    navController: NavController,
    initialLatitude: () -> Double?,
    initialLongitude: () -> Double?,
    onActiveChanged: () -> Unit,
) {
    navigation<MapGraph>(
        startDestination = MapGraph.MapScreen(),
    ) {
        composable<MapGraph.MapScreen> { backStackEntry ->
            onActiveChanged()
            val args = backStackEntry.toRoute<MapGraph.MapScreen>()
            MapScreenHost(
                mode = args.toScreenMode(),
                initialLatitude = initialLatitude(),
                initialLongitude = initialLongitude(),
                onPlacemarkAdded = {
                    navController.navigate(PlacemarksGraph.PlacemarksScreen) {
                        popUpTo(PlacemarksGraph.PlacemarksScreen) { inclusive = true }
                    }
                },
            )
        }
    }
}

@Composable
private fun MapScreenHost(
    mode: MapScreenMode,
    initialLatitude: Double?,
    initialLongitude: Double?,
    onPlacemarkAdded: () -> Unit,
) {
    if (initialLatitude == null || initialLongitude == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    MapScreen(
        mode = mode,
        initialLatitude = initialLatitude,
        initialLongitude = initialLongitude,
        onPlacemarkAdded = onPlacemarkAdded,
    )
}

private fun MapGraph.MapScreen.toScreenMode(): MapScreenMode = when (navMode) {
    MapNavMode.VIEW_ALL -> MapScreenMode.ViewAll
    MapNavMode.VIEW_SINGLE -> MapScreenMode.ViewSingle(placemarkId!!)
    MapNavMode.ADD_NEW -> MapScreenMode.AddNew
}
