package com.dagsbalken.app.ui.aod

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.time.LocalTime
import kotlin.math.roundToInt

@Composable
fun AodScreen(
    color: Int,
    opacity: Float,
    onExit: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    // Stripe height is roughly 5% of screen height
    val stripeHeight = screenHeight * 0.05f

    // Burn-in protection state
    var offsetY by remember { mutableStateOf(0f) }
    var offsetX by remember { mutableStateOf(0f) }

    // Time state for progress
    var now by remember { mutableStateOf(LocalTime.now()) }

    // Timer loop for time updates and burn-in protection
    LaunchedEffect(Unit) {
        while (true) {
            now = LocalTime.now()

            // Shift slightly every minute to prevent burn-in
            // Shift Y between 0 and 10 pixels
            // Shift X between -5 and 5 pixels (though X is handled by width, shifting start helps too)
            offsetY = (Math.random() * 20 - 5).toFloat() // Small vertical jitter
            offsetX = (Math.random() * 10 - 5).toFloat() // Small horizontal jitter

            delay(60_000L) // Update every minute
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = { onExit() }
                )
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(stripeHeight)
                .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) } // Apply burn-in offset
                .align(Alignment.TopStart)
        ) {
            val width = size.width
            val height = size.height

            val totalMinutes = 24 * 60
            val currentMinutes = now.hour * 60 + now.minute
            val progress = currentMinutes / totalMinutes.toFloat()

            val barWidth = width * progress

            drawRect(
                color = Color(color).copy(alpha = opacity),
                topLeft = Offset.Zero,
                size = Size(barWidth, height)
            )
        }
    }
}
