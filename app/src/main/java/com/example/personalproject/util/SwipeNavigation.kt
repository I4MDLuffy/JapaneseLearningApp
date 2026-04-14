package app.kotori.japanese.util

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput

/**
 * Adds horizontal swipe-to-navigate to any composable.
 * Swipe left  → [onSwipeLeft]  (next item)
 * Swipe right → [onSwipeRight] (previous item)
 * Passing null for either disables that direction.
 */
fun Modifier.swipeToNavigate(
    onSwipeLeft: (() -> Unit)?,
    onSwipeRight: (() -> Unit)?,
    thresholdPx: Float = 60f,
): Modifier = this.pointerInput(onSwipeLeft, onSwipeRight) {
    var totalDrag = 0f
    detectHorizontalDragGestures(
        onDragStart = { totalDrag = 0f },
        onDragCancel = { totalDrag = 0f },
        onDragEnd = {
            when {
                totalDrag < -thresholdPx -> onSwipeLeft?.invoke()
                totalDrag > thresholdPx  -> onSwipeRight?.invoke()
            }
            totalDrag = 0f
        },
        onHorizontalDrag = { change, dragAmount ->
            change.consume()
            totalDrag += dragAmount
        },
    )
}
