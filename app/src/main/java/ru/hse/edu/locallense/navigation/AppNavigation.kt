package ru.hse.edu.locallense.navigation

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import kotlinx.coroutines.delay
import ru.hse.edu.ar.presentation.screens.ArScreen
import ru.hse.edu.geoar.ar.ArGeoFactory
import ru.hse.edu.locallense.R
import ru.hse.edu.placemarks.presentation.screens.PlacemarksScreen
import ru.hse.locallense.common.Core

@Composable
fun AppNavigation() {
    val context = LocalContext.current.applicationContext

    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        ArGeoFactory.init(context, coroutineScope)
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
                        onSearchEnabledChange = { placemarksScreenSearchEnabled = it }
                    )
                }
            }

            navigation<ArGraph>(
                startDestination = ArGraph.ArScreen
            ) {
                composable<ArGraph.ArScreen> {
                    ArScreen()
                }
            }
        }
    }
}