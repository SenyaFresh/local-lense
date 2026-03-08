package ru.hse.locallense.components.composables.sceneview

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.node.ViewNode2

fun ARSceneView.createComposeViewNode(
    activity: ComponentActivity,
    content: @Composable () -> Unit
): ViewNode2 {
    val sceneView = this
    val windowManager = ViewNode2.WindowManager(activity)

    val node = ViewNode2(
        engine = sceneView.engine,
        windowManager = windowManager,
        materialLoader = sceneView.materialLoader,
        unlit = true,

        content = content
    )

    activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onResume(owner: LifecycleOwner) = windowManager.resume(sceneView)

        override fun onPause(owner: LifecycleOwner) = windowManager.pause()

        override fun onDestroy(owner: LifecycleOwner) = windowManager.destroy()
    })

    return node
}
