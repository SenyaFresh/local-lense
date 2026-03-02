package ru.hse.edu.locallense

import android.view.View
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.google.ar.sceneform.rendering.ViewAttachmentManager
import com.google.ar.sceneform.rendering.ViewRenderable
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.node.ViewNode
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun ARSceneView.createComposeViewNode(
    activity: ComponentActivity,
    content: @Composable () -> Unit
): ViewNode {

    val composeView = ComposeView(activity).apply {
        setContent {
            MaterialTheme {
                content()
            }
        }
    }

    val wrapper = object : FrameLayout(activity) {
        override fun onAttachedToWindow() {
            var root: View = this
            while (root.parent is View) root = root.parent as View
            root.setViewTreeLifecycleOwner(activity)
            root.setViewTreeViewModelStoreOwner(activity)
            root.setViewTreeSavedStateRegistryOwner(activity)
            super.onAttachedToWindow()
        }
    }
    wrapper.addView(composeView)

    val viewAttachmentManager = ViewAttachmentManager(activity, this)

    activity.lifecycle.addObserver(object : DefaultLifecycleObserver {
        override fun onResume(owner: LifecycleOwner) = viewAttachmentManager.onResume()
        override fun onPause(owner: LifecycleOwner) = viewAttachmentManager.onPause()
        override fun onDestroy(owner: LifecycleOwner) = activity.lifecycle.removeObserver(this)
    })

    val node = ViewNode(this.engine, this.modelLoader, viewAttachmentManager)

    val renderable = suspendCancellableCoroutine<ViewRenderable> { cont ->
        ViewRenderable.builder()
            .setView(activity, wrapper)
            .build(this.engine)
            .thenAccept { renderable ->
                cont.resume(renderable)
            }
            .exceptionally { throwable ->
                cont.resumeWithException(throwable ?: RuntimeException("ViewRenderable build failed"))
                null
            }
    }

    renderable.isShadowCaster = false
    renderable.isShadowReceiver = false
    node.setRenderable(renderable)

    return node
}