package ru.hse.edu.locallense.navigation

import android.content.Intent
import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import androidx.navigation.toRoute
import kotlinx.coroutines.delay
import ru.hse.edu.ar.presentation.screens.ArScreen
import ru.hse.edu.ar.presentation.screens.ArScreenMode
import ru.hse.edu.ar.presentation.screens.MapScreen
import ru.hse.edu.ar.presentation.screens.MapScreenMode
import ru.hse.edu.ar.presentation.screens.PreparationsScreen
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
    LaunchedEffect(Unit) {
        ArGeoFactory.init(context, coroutineScope)
        ArGeoFactory.locationTracker.locationState.collect {
            initialLatitude = it?.latitude
            initialLongitude = it?.longitude
        }
    }

    val navController = rememberNavController()
    val currentBackStackEntry = navController.currentBackStackEntryAsState()

    // Determine the current screen's title based on the route of the current navigation entry.
    val titleRes: Int? = when (currentBackStackEntry.value.routeClass()) {
        PlacemarksGraph.PlacemarksScreen::class -> R.string.placemarks
        ArGraph.ArScreen::class -> R.string.ar
        else -> null
    }

    var placemarksScreenSearchEnabled by remember { mutableStateOf(false) }

    // Left icon action: handle navigation back if there is a previous screen.
    val leftIconAction: IconAction? = if (navController.previousBackStackEntry == null) {
        null
    } else {
        IconAction(Icons.AutoMirrored.Filled.KeyboardArrowLeft) { navController.popBackStack() }
    }

    var exit by remember { mutableStateOf(false) }

    // Handle exit logic with a 2-second delay on back button press.
    LaunchedEffect(key1 = exit) {
        if (exit) {
            delay(2000)
            exit = false
        }
    }

    // Back button press handler.
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

    // Define right icon actions based on the current screen.
    val rightIconsActions: List<IconAction>? = when (currentBackStackEntry.value.routeClass()) {
        PlacemarksGraph.PlacemarksScreen::class -> listOf(
            IconAction(
                imageVector = if (!placemarksScreenSearchEnabled) Icons.Default.Search else Icons.Default.SearchOff,
                onClick = { placemarksScreenSearchEnabled = !placemarksScreenSearchEnabled }
            ),
        )
        else -> null
    }

    // Scaffold the layout with the top bar, bottom bar, and navigation host.
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
        NavHost(
            navController = navController,
            startDestination = PlacemarksGraph,
            enterTransition = { fadeIn(tween(200)) },
            exitTransition = { fadeOut(tween(200)) },
            popEnterTransition = { fadeIn(tween(200)) },
            popExitTransition = { fadeOut(tween(200)) },
            modifier = Modifier.padding(padding)
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
                        onAddNewPlacemark = {
                            navController.navigate(ArGraph.ArScreen(
                                navMode = ArNavMode.ADD_NEW,
                            ))
                        }
                    )
                }
            }

            navigation<ArGraph>(
                startDestination = ArGraph.PreparationsScreen
            ) {
                composable<ArGraph.PreparationsScreen> {
                    PreparationsScreen(
                        initialLatitude = initialLatitude,
                        initialLongitude = initialLongitude,
                        onContinue = { lat, lng ->
                            ArGeoFactory.locationTracker.setExactLocation(lat, lng)
                            navController.navigate(ArGraph.ArScreen(
                                navMode = ArNavMode.VIEW_ALL,
                            ))
                        }
                    )
                }
                composable<ArGraph.ArScreen> { backStackEntry ->
                    val args = backStackEntry.toRoute<ArGraph.ArScreen>()
                    val mode = when (args.navMode) {
                        ArNavMode.VIEW_ALL -> ArScreenMode.ViewAll
                        ArNavMode.VIEW_SINGLE -> ArScreenMode.ViewSingle(args.placemarkId!!)
                        ArNavMode.ADD_NEW -> ArScreenMode.AddNew
                    }
                    ArScreen(mode = mode)
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
                        )
                    } else {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }
                }
            }
        }
    }
}