package ru.hse.edu.locallense.navigation.graphs

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navigation
import androidx.navigation.toRoute
import ru.hse.edu.ar.presentation.screens.ArScreen
import ru.hse.edu.ar.presentation.screens.ArScreenMode
import ru.hse.edu.ar.presentation.screens.PreparationsScreen
import ru.hse.edu.geoar.ar.ArGeoFactory
import ru.hse.edu.locallense.navigation.ArGraph
import ru.hse.edu.locallense.navigation.ArNavMode
import ru.hse.edu.locallense.navigation.PlacemarksGraph
import ru.hse.edu.locallense.navigation.state.ArSceneController

internal fun NavGraphBuilder.arGraph(
    navController: NavController,
    sceneController: ArSceneController,
    initialLatitude: () -> Double?,
    initialLongitude: () -> Double?,
    sensorHeading: () -> Float?,
    onArScreenActive: (Boolean) -> Unit,
) {
    navigation<ArGraph>(
        startDestination = ArGraph.PreparationsScreen,
    ) {
        composable<ArGraph.PreparationsScreen> {
            onArScreenActive(false)
            PreparationsScreen(
                initialLatitude = initialLatitude(),
                initialLongitude = initialLongitude(),
                initialHeading = sensorHeading(),
                onHeadingChange = { heading ->
                    val tracker = sceneController.engine?.arPoseLocationTracker
                    if (heading == null) tracker?.unlockHeading()
                    else tracker?.forceHeading(heading)
                },
                onContinue = { lat, lng ->
                    ArGeoFactory.locationTracker.setExactLocation(lat, lng)
                    navController.navigate(ArGraph.ArScreen(navMode = ArNavMode.VIEW_ALL))
                },
            )
        }

        composable<ArGraph.ArScreen> { backStackEntry ->
            val args = backStackEntry.toRoute<ArGraph.ArScreen>()
            val engine = sceneController.engine
            val sceneView = sceneController.sceneView
            if (engine != null && sceneView != null) {
                ArScreen(
                    mode = args.toScreenMode(),
                    arGeoEngine = engine,
                    arSceneView = sceneView,
                    onPlacemarkAdded = {
                        navController.navigate(PlacemarksGraph.PlacemarksScreen) {
                            popUpTo(PlacemarksGraph.PlacemarksScreen) { inclusive = true }
                        }
                    },
                    onCompassMarkersChange = { sceneController.compassMarkers = it },
                )
                onArScreenActive(true)
            }
        }
    }
}

private fun ArGraph.ArScreen.toScreenMode(): ArScreenMode = when (navMode) {
    ArNavMode.VIEW_ALL -> ArScreenMode.ViewAll
    ArNavMode.VIEW_SINGLE -> ArScreenMode.ViewSingle(placemarkId!!)
    ArNavMode.ADD_NEW -> ArScreenMode.AddNew
}
