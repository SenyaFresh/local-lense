package ru.hse.edu.locallense.navigation.state

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import io.github.sceneview.ar.ARSceneView
import kotlinx.coroutines.CoroutineScope
import ru.hse.edu.ar.domain.entities.ArPlacemark
import ru.hse.edu.geoar.ar.ArGeoEngine

@Stable
class ArSceneController internal constructor(private val scope: CoroutineScope) {

    var sceneView: ARSceneView? by mutableStateOf(null)
        private set

    var engine: ArGeoEngine? by mutableStateOf(null)
        private set

    var compassMarkers: List<ArPlacemark> by mutableStateOf(emptyList())

    fun mount(view: ARSceneView) {
        if (sceneView != null) return
        sceneView = view
        engine = ArGeoEngine(sceneView = view, scope = scope)
    }
}

@Composable
fun rememberArSceneController(): ArSceneController {
    val scope = rememberCoroutineScope()
    return remember { ArSceneController(scope) }
}
