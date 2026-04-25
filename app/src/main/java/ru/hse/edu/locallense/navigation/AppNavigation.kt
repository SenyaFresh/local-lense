package ru.hse.edu.locallense.navigation

import android.view.View
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.github.sceneview.ar.ARSceneView
import ru.hse.edu.ar.presentation.components.ArCompassMarkerData
import ru.hse.edu.ar.presentation.components.ArCompassOverlay
import ru.hse.edu.geoar.ar.ArGeoFactory
import ru.hse.edu.locallense.R
import ru.hse.edu.locallense.navigation.graphs.arGraph
import ru.hse.edu.locallense.navigation.graphs.mapGraph
import ru.hse.edu.locallense.navigation.graphs.placemarksGraph
import ru.hse.edu.locallense.navigation.state.ArSceneController
import ru.hse.edu.locallense.navigation.state.BackToExitConfirmation
import ru.hse.edu.locallense.navigation.state.rememberArSceneController

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val sceneController = rememberArSceneController()

    var initialLatitude: Double? by remember { mutableStateOf(null) }
    var initialLongitude: Double? by remember { mutableStateOf(null) }
    var isPlacemarksSearchEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        ArGeoFactory.locationTracker.locationState.collect {
            initialLatitude = it?.latitude
            initialLongitude = it?.longitude
        }
    }

    val sensorHeading by ArGeoFactory.headingProvider.smoothedValue.collectAsState()
    var isArScreenActive by remember { mutableStateOf(false) }

    BackToExitConfirmation()

    Scaffold(
        topBar = {
            AppTopBar(
                titleRes = currentBackStackEntry.titleRes(),
                leftIconAction = navController.backIconAction(),
                rightIconsActions = currentBackStackEntry.rightIcons(
                    isSearchEnabled = isPlacemarksSearchEnabled,
                    onSearchToggle = { isPlacemarksSearchEnabled = !isPlacemarksSearchEnabled },
                ),
            )
        },
        bottomBar = {
            Column { AppNavigationBar(navigationController = navController, tabs = MainTabs) }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            NavHost(
                navController = navController,
                startDestination = PlacemarksGraph,
                enterTransition = { fadeIn(tween(200)) },
                exitTransition = { fadeOut(tween(200)) },
                popEnterTransition = { fadeIn(tween(200)) },
                popExitTransition = { fadeOut(tween(200)) },
                modifier = Modifier.fillMaxSize(),
            ) {
                placemarksGraph(
                    navController = navController,
                    isSearchEnabled = { isPlacemarksSearchEnabled },
                    onSearchEnabledChange = { isPlacemarksSearchEnabled = it },
                    onActiveChanged = { isArScreenActive = false },
                )
                arGraph(
                    navController = navController,
                    sceneController = sceneController,
                    initialLatitude = { initialLatitude },
                    initialLongitude = { initialLongitude },
                    sensorHeading = { sensorHeading },
                    onArScreenActive = { active -> isArScreenActive = active },
                )
                mapGraph(
                    navController = navController,
                    initialLatitude = { initialLatitude },
                    initialLongitude = { initialLongitude },
                    onActiveChanged = { isArScreenActive = false },
                )
            }

            AndroidView(
                factory = { ctx ->
                    ARSceneView(ctx).also { view -> sceneController.mount(view) }
                },
                modifier = Modifier.fillMaxSize(),
                update = { view ->
                    view.visibility = if (isArScreenActive) View.VISIBLE else View.INVISIBLE
                },
            )

            if (isArScreenActive) {
                ArCompassOverlay(
                    markers = rememberCompassMarkerData(sceneController),
                    userHeading = rememberCompassHeading(sceneController, sensorHeading),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 4.dp, start = 12.dp, end = 12.dp),
                )
            }
        }
    }
}

@Composable
private fun rememberCompassHeading(
    sceneController: ArSceneController,
    sensorHeading: Float?,
): Float {
    val effectiveHeading by produceState<Float?>(null, sceneController.engine) {
        sceneController.engine?.arPoseLocationTracker?.effectiveUserHeading?.collect { value = it }
    }
    return effectiveHeading ?: sensorHeading ?: 0f
}

@Composable
private fun rememberCompassMarkerData(
    sceneController: ArSceneController,
): List<ArCompassMarkerData> {
    val placedMarkers by produceState(emptyList(), sceneController.engine) {
        sceneController.engine?.placedMarkers?.collect { value = it }
    }
    return remember(sceneController.compassMarkers, placedMarkers) {
        if (placedMarkers.isEmpty()) return@remember emptyList()
        val byId = placedMarkers.associateBy { it.id }
        sceneController.compassMarkers.mapNotNull { placemark ->
            val snap = byId[placemark.id] ?: return@mapNotNull null
            ArCompassMarkerData(
                id = placemark.id,
                color = placemark.color,
                distanceMeters = snap.distanceMeters,
                screenBearingDegrees = snap.screenBearingDegrees.toFloat(),
                altitudeDeltaMeters = snap.altitudeDifferenceMeters,
            )
        }
    }
}

private fun NavBackStackEntry?.titleRes(): Int? = when (routeClass()) {
    PlacemarksGraph.PlacemarksScreen::class -> R.string.placemarks
    ArGraph.ArScreen::class -> R.string.ar
    MapGraph.MapScreen::class -> R.string.map
    else -> null
}

private fun NavController.backIconAction(): IconAction? =
    if (previousBackStackEntry == null) null
    else IconAction(Icons.AutoMirrored.Filled.KeyboardArrowLeft) { popBackStack() }

private fun NavBackStackEntry?.rightIcons(
    isSearchEnabled: Boolean,
    onSearchToggle: () -> Unit,
): List<IconAction>? = when (routeClass()) {
    PlacemarksGraph.PlacemarksScreen::class -> listOf(
        IconAction(
            imageVector = if (isSearchEnabled) Icons.Default.SearchOff else Icons.Default.Search,
            onClick = onSearchToggle,
        )
    )
    else -> null
}
