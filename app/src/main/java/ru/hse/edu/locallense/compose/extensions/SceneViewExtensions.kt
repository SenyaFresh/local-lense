package ru.hse.edu.locallense.compose.extensions

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.ar.sceneform.rendering.ViewAttachmentManager
import com.google.ar.sceneform.rendering.ViewRenderable
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.node.ViewNode
import kotlinx.coroutines.future.await

suspend fun ARSceneView.createComposeViewNode(
    activity: ComponentActivity,
    content: @Composable () -> Unit
): ViewNode {
    val wrapper = SceneviewComposeWrapper(activity, content)

    val viewAttachmentManager = ViewAttachmentManager(activity, this)
    viewAttachmentManager.bindToLifecycle(activity)

    val node = ViewNode(this.engine, this.modelLoader, viewAttachmentManager)

    val renderable = ViewRenderable.builder()
        .setView(activity, wrapper)
        .build(this.engine)
        .await()
        .apply {
            isShadowCaster = false
            isShadowReceiver = false
        }

    node.setRenderable(renderable)

    return node
}

private fun ViewAttachmentManager.bindToLifecycle(activity: ComponentActivity) {
    activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onResume(owner: LifecycleOwner) = onResume()
        override fun onPause(owner: LifecycleOwner) = onPause()
        override fun onDestroy(owner: LifecycleOwner) = owner.lifecycle.removeObserver(this)
    })
}