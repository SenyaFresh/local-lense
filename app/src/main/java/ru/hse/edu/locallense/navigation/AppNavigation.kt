package ru.hse.edu.locallense.navigation

import android.content.Intent
import android.view.View
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import androidx.navigation.toRoute
import io.github.sceneview.ar.ARSceneView
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import ru.hse.edu.ar.presentation.screens.ArScreen
import ru.hse.edu.ar.presentation.screens.ArScreenMode
import ru.hse.edu.ar.presentation.screens.MapScreen
import ru.hse.edu.ar.presentation.screens.MapScreenMode
import ru.hse.edu.ar.presentation.screens.PreparationsScreen
import ru.hse.edu.geoar.ar.ArGeoEngine
import ru.hse.edu.geoar.ar.ArGeoFactory
import ru.hse.edu.locallense.R
import ru.hse.edu.placemarks.presentation.screens.PlacemarksScreen
import ru.hse.locallense.common.Core

@Composable
fun AppNavigation() {
    var initialLatitude: Double? by remember { mutableStateOf(null) }
    var initialLongitude: Double? by remember { mutableStateOf(null) }

    val context = LocalContext.current.applicationContext
    val coroutineScope = rememberCoroutineScope()

    var arSceneView by remember { mutableStateOf<ARSceneView?>(null) }
    var arGeoEngine by remember { mutableStateOf<ArGeoEngine?>(null) }

    LaunchedEffect(Unit) {
        ArGeoFactory.locationTracker.locationState.collect {
            initialLatitude = it?.latitude
            initialLongitude = it?.longitude
        }
    }

    val navController = rememberNavController()
    val currentBackStackEntry = navController.currentBackStackEntryAsState()

    val titleRes: Int? = when (currentBackStackEntry.value.routeClass()) {
        PlacemarksGraph.PlacemarksScreen::class -> R.string.placemarks
        ArGraph.ArScreen::class -> R.string.ar
        MapGraph.MapScreen::class -> R.string.map
        else -> null
    }

    var placemarksScreenSearchEnabled by remember { mutableStateOf(false) }

    val leftIconAction: IconAction? = if (navController.previousBackStackEntry == null) {
        null
    } else {
        IconAction(Icons.AutoMirrored.Filled.KeyboardArrowLeft) { navController.popBackStack() }
    }

    var exit by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = exit) {
        if (exit) {
            delay(2000)
            exit = false
        }
    }

    BackHandler {
        if (exit) {
            context.startActivity(Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            })
        } else {
            exit = true
            Core.toaster.showToast("Нажмите еще раз, чтобы выйти")
        }
    }

    val rightIconsActions: List<IconAction>? = when (currentBackStackEntry.value.routeClass()) {
        PlacemarksGraph.PlacemarksScreen::class -> listOf(
            IconAction(
                imageVector = if (!placemarksScreenSearchEnabled) Icons.Default.Search else Icons.Default.SearchOff,
                onClick = { placemarksScreenSearchEnabled = !placemarksScreenSearchEnabled }
            ),
        )
        else -> null
    }

    var isArScreenActive by remember { mutableStateOf(false) }

    val initialHeading by ArGeoFactory.headingProvider.smoothedValue.collectAsState()


    Scaffold(
        topBar = {
            AppTopBar(
                titleRes = titleRes,
                leftIconAction = leftIconAction,
                rightIconsActions = rightIconsActions
            )
        },
        bottomBar = {
            Column {
                AppNavigationBar(
                    navigationController = navController,
                    tabs = MainTabs
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            NavHost(
                navController = navController,
                startDestination = PlacemarksGraph,
                enterTransition = { fadeIn(tween(200)) },
                exitTransition = { fadeOut(tween(200)) },
                popEnterTransition = { fadeIn(tween(200)) },
                popExitTransition = { fadeOut(tween(200)) },
                modifier = Modifier.fillMaxSize()
            ) {
                navigation<PlacemarksGraph>(
                    startDestination = PlacemarksGraph.PlacemarksScreen
                ) {
                    composable<PlacemarksGraph.PlacemarksScreen> {
                        PlacemarksScreen(
                            searchEnabled = placemarksScreenSearchEnabled,
                            onSearchEnabledChange = { placemarksScreenSearchEnabled = it },
                            onPlacemarkOpenOnMap = { id ->
                                navController.navigate(MapGraph.MapScreen(
                                    navMode = MapNavMode.VIEW_SINGLE,
                                    placemarkId = id,
                                ))
                            },
                            onPlacemarkOpenInAr = { id ->
                                navController.navigate(ArGraph.ArScreen(
                                    navMode = ArNavMode.VIEW_SINGLE,
                                    placemarkId = id,
                                ))
                            },
                            onAddNewPlacemarkOnMap = {
                                navController.navigate(MapGraph.MapScreen(
                                    navMode = MapNavMode.ADD_NEW,
                                ))
                            },
                            onAddNewPlacemarkInAr = {
                                navController.navigate(ArGraph.ArScreen(
                                    navMode = ArNavMode.ADD_NEW,
                                ))
                            },
                        )
                        isArScreenActive = false
                    }
                }

                navigation<ArGraph>(
                    startDestination = ArGraph.PreparationsScreen
                ) {
                    composable<ArGraph.PreparationsScreen> {
                        PreparationsScreen(
                            initialLatitude = initialLatitude,
                            initialLongitude = initialLongitude,
                            initialHeading = initialHeading,
                            onHeadingChange = { heading ->
                                if (heading == null) {
//                                    arGeoEngine?.arPoseLocationTracker?.unlockHeading()
                                } else {
                                    arGeoEngine?.arPoseLocationTracker?.forceHeading(heading)
                                }
                            },
                            onContinue = { lat, lng ->
                                ArGeoFactory.locationTracker.setExactLocation(lat, lng)
                                navController.navigate(
                                    ArGraph.ArScreen(
                                        navMode = ArNavMode.VIEW_ALL,
                                    )
                                )
                            }
                        )
                        isArScreenActive = false
                    }
                    composable<ArGraph.ArScreen> { backStackEntry ->
                        val args = backStackEntry.toRoute<ArGraph.ArScreen>()
                        val mode = when (args.navMode) {
                            ArNavMode.VIEW_ALL -> ArScreenMode.ViewAll
                            ArNavMode.VIEW_SINGLE -> ArScreenMode.ViewSingle(args.placemarkId!!)
                            ArNavMode.ADD_NEW -> ArScreenMode.AddNew
                        }
                        val engine = arGeoEngine
                        val sceneView = arSceneView
                        if (engine != null && sceneView != null) {
                            ArScreen(
                                mode = mode,
                                arGeoEngine = engine,
                                arSceneView = sceneView,
                                onPlacemarkAdded = {
                                    navController.navigate(PlacemarksGraph.PlacemarksScreen) {
                                        popUpTo(PlacemarksGraph.PlacemarksScreen) { inclusive = true }
                                    }
                                },
                            )
                            isArScreenActive = true
                        }
                    }
                }

                navigation<MapGraph>(
                    startDestination = MapGraph.MapScreen()
                ) {
                    composable<MapGraph.MapScreen> { backStackEntry ->
                        val args = backStackEntry.toRoute<MapGraph.MapScreen>()
                        val mode = when (args.navMode) {
                            MapNavMode.VIEW_ALL -> MapScreenMode.ViewAll
                            MapNavMode.VIEW_SINGLE -> MapScreenMode.ViewSingle(args.placemarkId!!)
                            MapNavMode.ADD_NEW -> MapScreenMode.AddNew
                        }
                        val lat = initialLatitude
                        val lng = initialLongitude
                        if (lat != null && lng != null) {
                            MapScreen(
                                mode = mode,
                                initialLatitude = lat,
                                initialLongitude = lng,
                                onPlacemarkAdded = {
                                    navController.navigate(PlacemarksGraph.PlacemarksScreen) {
                                        popUpTo(PlacemarksGraph.PlacemarksScreen) { inclusive = true }
                                    }
                                },
                            )
                        } else {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator()
                            }
                        }
                        isArScreenActive = false
                    }
                }
            }

            AndroidView(
                factory = { ctx ->
                    ARSceneView(ctx).also { sceneView ->
                        arSceneView = sceneView
                        arGeoEngine = ArGeoEngine(
                            sceneView = sceneView,
                            scope = coroutineScope,
                        )
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = { sceneView ->
                    sceneView.visibility = if (isArScreenActive) {
                        View.VISIBLE
                    } else {
                        View.INVISIBLE
                    }
                },
            )
        }
    }
}