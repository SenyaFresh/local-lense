package ru.hse.edu.locallense.compose.extensions

import android.view.View
import android.widget.FrameLayout
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import ru.hse.edu.locallense.ui.theme.LocalLenseTheme

class SceneviewComposeWrapper(
    private val activity: ComponentActivity,
    content: @Composable () -> Unit
) : FrameLayout(activity) {

    init {
        addView(
            ComposeView(activity).apply {
                setContent { LocalLenseTheme { content() } }
            }.apply {
                scaleX = -1f
            }
        )
    }

    override fun onAttachedToWindow() {
        val root = generateSequence<View>(this) { it.parent as? View }.last()
        root.setViewTreeLifecycleOwner(activity)
        root.setViewTreeViewModelStoreOwner(activity)
        root.setViewTreeSavedStateRegistryOwner(activity)
        super.onAttachedToWindow()
    }
}