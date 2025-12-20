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
    positionPercent: Float,
    onExit: () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp

    // Stripe height is roughly 5% of screen height
    val stripeHeight = screenHeight * 0.05f

    val clampedPercent = positionPercent.coerceIn(0f, 100f)
    val verticalOffset = (screenHeight - stripeHeight) * (clampedPercent / 100f)

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
                .offset {
                    val baseY = verticalOffset.roundToPx() + offsetY
                    IntOffset(offsetX.roundToInt(), baseY.roundToInt())
                }
                .align(Alignment.TopStart)
        ) {
            val width = size.width
            val height = size.height

            val totalMinutes = 24 * 60
            val currentMinutes = now.hour * 60 + now.minute
            val progress = currentMinutes / totalMinutes.toFloat()

            val barWidth = width * progress

            // Huvudprogress-bar för dygnet
            drawRect(
                color = Color(color).copy(alpha = opacity.coerceIn(0f, 1f)),
                topLeft = Offset.Zero,
                size = Size(barWidth, height)
            )

            // --- Dagshållpunkter: sol (08:00) och måne (20:00) ---
            val morningMinutes = 8 * 60
            val eveningMinutes = 20 * 60

            val morningX = width * (morningMinutes / totalMinutes.toFloat())
            val eveningX = width * (eveningMinutes / totalMinutes.toFloat())

            val iconRadius = height * 0.3f

            val baseColor = Color(color).copy(alpha = opacity.coerceIn(0f, 1f))

            // Solikon – enkel cirkel
            drawCircle(
                color = baseColor,
                radius = iconRadius,
                center = Offset(morningX, height / 2f)
            )

            // Månikon – cirkel + utskuren del för crescent
            val moonCenter = Offset(eveningX, height / 2f)
            drawCircle(
                color = baseColor,
                radius = iconRadius,
                center = moonCenter
            )
            // Skapa "halvmåne" genom att rita en andra cirkel i bakgrundsfärgen
            drawCircle(
                color = Color.Black,
                radius = iconRadius * 0.8f,
                center = moonCenter + Offset(iconRadius * 0.4f, 0f)
            )
        }
    }
}
