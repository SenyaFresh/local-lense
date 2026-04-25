package ru.hse.edu.placemarks.presentation.components.listitem

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Stable
internal class PlacemarkSwipeState(private val scope: CoroutineScope) {

    private val offset = Animatable(0f)
    var contextMenuWidth: Float by mutableFloatStateOf(0f)

    val pixelOffset: IntOffset get() = IntOffset(offset.value.roundToInt(), 0)

    suspend fun syncRevealed(isRevealed: Boolean) {
        offset.animateTo(if (isRevealed) -contextMenuWidth else 0f)
    }

    fun onDrag(dragAmount: Float) {
        scope.launch {
            offset.snapTo((offset.value + dragAmount).coerceIn(-contextMenuWidth, 0f))
        }
    }

    fun onDragEnd() {
        scope.launch {
            val target = if (offset.value <= -contextMenuWidth / 2f) -contextMenuWidth else 0f
            offset.animateTo(target)
        }
    }
}

@Composable
internal fun rememberPlacemarkSwipeState(): PlacemarkSwipeState {
    val scope = rememberCoroutineScope()
    return remember { PlacemarkSwipeState(scope) }
}
