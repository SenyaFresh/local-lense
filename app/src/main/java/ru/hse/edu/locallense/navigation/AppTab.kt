package ru.hse.edu.locallense.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.collections.immutable.persistentListOf
import ru.hse.edu.locallense.R

data class AppTab(
    val imageVector: ImageVector,
    @StringRes val titleRes: Int,
    val graphRoute: Any
)

val MainTabs = persistentListOf(
    AppTab(
        imageVector = Icons.Default.ViewInAr,
        titleRes = R.string.ar,
        graphRoute = ArGraph
    ),
    AppTab(
        imageVector = Icons.Default.Map,
        titleRes = R.string.map,
        graphRoute = MapGraph
    ),
    AppTab(
        imageVector = Icons.AutoMirrored.Filled.FormatListBulleted,
        titleRes = R.string.placemarks,
        graphRoute = PlacemarksGraph
    )
)