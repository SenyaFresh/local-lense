package ru.hse.edu.locallense.navigation

import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import kotlinx.serialization.Serializable
import kotlin.reflect.KClass

@Serializable
enum class ArNavMode { VIEW_ALL, VIEW_SINGLE, ADD_NEW }

@Serializable
data object ArGraph {
    @Serializable
    data class ArScreen(
        val navMode: ArNavMode = ArNavMode.VIEW_ALL,
        val placemarkId: Long? = null,
    )

    @Serializable
    data object PreparationsScreen
}

@Serializable
enum class MapNavMode { VIEW_ALL, VIEW_SINGLE, ADD_NEW }

@Serializable
data object MapGraph {
    @Serializable
    data class MapScreen(
        val navMode: MapNavMode = MapNavMode.VIEW_ALL,
        val placemarkId: Long? = null,
    )
}

@Serializable
data object PlacemarksGraph {
    @Serializable
    data object PlacemarksScreen
}

fun NavBackStackEntry?.routeClass(): KClass<*>? {
    return this
        ?.destination
        ?.routeClass()
}

fun NavDestination?.routeClass(): KClass<*>? {
    return this
        ?.route
        ?.split("/")
        ?.first()
        ?.let { className ->
            generateSequence(className, ::replaceLastDotByDollar)
                .mapNotNull(::tryParseClass)
                .firstOrNull()
        }
}

private fun tryParseClass(className: String): KClass<*>? {
    return runCatching { Class.forName(className).kotlin }.getOrNull()
}

private fun replaceLastDotByDollar(input: String): String? {
    val lastDotIndex = input.lastIndexOf('.')
    return if (lastDotIndex != -1) {
        String(input.toCharArray().apply { set(lastDotIndex, '$') })
    } else {
        null
    }
}