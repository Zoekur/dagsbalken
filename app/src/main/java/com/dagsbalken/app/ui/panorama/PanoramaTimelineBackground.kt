package com.dagsbalken.app.ui.panorama

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.SvgDecoder
import coil.request.ImageRequest
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sin

@Composable
fun PanoramaTimelineBackground(
    viewportCenterMinute: Int,
    visibleStartMinute: Int,
    visibleEndMinute: Int,
    visibleDurationMinutes: Int,
    isZoomed: Boolean,
    style: PanoramaStyle = PanoramaStyle.Nordic,
    focusLaneFraction: Float = 0.7f,
    modifier: Modifier = Modifier,
    scene: PanoramaScene = DagsbalkenPanoramaScene
) {
    val context = LocalContext.current
    val imageLoader = remember(context) {
        ImageLoader.Builder(context)
            .components { add(SvgDecoder.Factory()) }
            .build()
    }
    val viewportProgress = (viewportCenterMinute.coerceIn(0, 24 * 60) / (24f * 60f)).coerceIn(0f, 1f)
    val atmosphere = remember(viewportProgress, style) { panoramaAtmosphereFor(viewportProgress, style) }
    val styleProfile = remember(style) { panoramaStyleProfile(style) }
    val zoomProgress = remember(visibleDurationMinutes, isZoomed) {
        if (!isZoomed) 0f else ((24 * 60 - visibleDurationMinutes.coerceIn(6 * 60, 24 * 60)).toFloat() / (18 * 60f)).coerceIn(0f, 1f)
    }

    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val sceneWidthDp = remember(maxWidth, maxHeight, scene.aspectRatio, zoomProgress, style) {
            val widthFillFactor = if (maxWidth > maxHeight) 1.9f else 1.42f
            val zoomScale = when (style) {
                PanoramaStyle.Storybook -> 1f + (zoomProgress * 0.16f)
                PanoramaStyle.Nordic -> 1f + (zoomProgress * 0.14f)
                PanoramaStyle.Arcade -> 1f + (zoomProgress * 0.18f)
            }
            maxOf(maxWidth * (widthFillFactor + zoomProgress * 0.08f), maxHeight * scene.aspectRatio * zoomScale)
        }
        val viewportWidthPx = with(density) { maxWidth.toPx() }
        val sceneWidthPx = with(density) { sceneWidthDp.toPx() }
        val availableParallaxPx = (sceneWidthPx - viewportWidthPx).coerceAtLeast(0f)
        val centeredOffsetPx = -availableParallaxPx / 2f
        val sceneHeightPx = with(density) { maxHeight.toPx() }

        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    val isWide = size.width > size.height
                    val horizonY = if (isWide) size.height * 0.66f else size.height * 0.5f
                    val foothillY = if (isWide) size.height * 0.8f else size.height * 0.72f
                    val focusCenter = Offset(
                        x = size.width * ((visibleStartMinute + visibleEndMinute) / (2f * 24f * 60f)),
                        y = size.height * focusLaneFraction - size.height * if (isWide) 0.04f else 0.08f
                    )
                    val celestialCenter = Offset(
                        x = size.width * viewportProgress,
                        y = size.height * (if (isWide) 0.22f else 0.18f + ((1f - atmosphere.daylight) * 0.12f))
                    )
                    val farSilhouette = Path().apply {
                        moveTo(-size.width * 0.1f, horizonY)
                        cubicTo(
                            size.width * 0.06f, horizonY - size.height * 0.12f,
                            size.width * 0.2f, horizonY - size.height * 0.05f,
                            size.width * 0.34f, horizonY - size.height * 0.14f
                        )
                        cubicTo(
                            size.width * 0.5f, horizonY - size.height * 0.02f,
                            size.width * 0.68f, horizonY - size.height * 0.16f,
                            size.width * 0.84f, horizonY - size.height * 0.06f
                        )
                        cubicTo(
                            size.width * 0.96f, horizonY - size.height * 0.02f,
                            size.width * 1.05f, horizonY - size.height * 0.08f,
                            size.width * 1.12f, horizonY - size.height * 0.03f
                        )
                        lineTo(size.width * 1.12f, size.height)
                        lineTo(-size.width * 0.1f, size.height)
                        close()
                    }
                    val midSilhouette = Path().apply {
                        moveTo(-size.width * 0.08f, foothillY)
                        cubicTo(
                            size.width * 0.08f, foothillY - size.height * 0.08f,
                            size.width * 0.24f, foothillY - size.height * 0.02f,
                            size.width * 0.38f, foothillY - size.height * 0.1f
                        )
                        cubicTo(
                            size.width * 0.54f, foothillY - size.height * 0.01f,
                            size.width * 0.72f, foothillY - size.height * 0.12f,
                            size.width * 0.9f, foothillY - size.height * 0.04f
                        )
                        cubicTo(
                            size.width * 1.0f, foothillY,
                            size.width * 1.08f, foothillY - size.height * 0.03f,
                            size.width * 1.14f, foothillY - size.height * 0.01f
                        )
                        lineTo(size.width * 1.14f, size.height)
                        lineTo(-size.width * 0.08f, size.height)
                        close()
                    }
                    val foregroundBase = Path().apply {
                        moveTo(-size.width * 0.06f, size.height * 0.9f)
                        cubicTo(
                            size.width * 0.16f, size.height * 0.84f,
                            size.width * 0.34f, size.height * 0.92f,
                            size.width * 0.52f, size.height * 0.86f
                        )
                        cubicTo(
                            size.width * 0.7f, size.height * 0.82f,
                            size.width * 0.88f, size.height * 0.9f,
                            size.width * 1.08f, size.height * 0.85f
                        )
                        lineTo(size.width * 1.08f, size.height)
                        lineTo(-size.width * 0.06f, size.height)
                        close()
                    }
                    val farSilhouetteColor = when (style) {
                        PanoramaStyle.Storybook -> Color(0xFF7D86B0).copy(alpha = 0.46f)
                        PanoramaStyle.Nordic -> Color(0xFF7C89B7).copy(alpha = 0.48f)
                        PanoramaStyle.Arcade -> Color(0xFF667DCE).copy(alpha = 0.5f)
                    }
                    val midSilhouetteColor = when (style) {
                        PanoramaStyle.Storybook -> Color(0xFF516682).copy(alpha = 0.54f)
                        PanoramaStyle.Nordic -> Color(0xFF596A82).copy(alpha = 0.58f)
                        PanoramaStyle.Arcade -> Color(0xFF405C87).copy(alpha = 0.6f)
                    }
                    val foregroundBaseBrush = Brush.verticalGradient(
                        colors = when (style) {
                            PanoramaStyle.Storybook -> listOf(Color(0xFFB7B06E).copy(alpha = 0.5f), Color(0xFF394A5A).copy(alpha = 0.72f))
                            PanoramaStyle.Nordic -> listOf(Color(0xFF8AA15A).copy(alpha = 0.42f), Color(0xFF36465D).copy(alpha = 0.72f))
                            PanoramaStyle.Arcade -> listOf(Color(0xFF78B26C).copy(alpha = 0.46f), Color(0xFF213E5C).copy(alpha = 0.78f))
                        },
                        startY = foothillY,
                        endY = size.height
                    )
                    val skyBrush = Brush.verticalGradient(
                        0f to atmosphere.topTint,
                        0.5f to atmosphere.midTint,
                        1f to atmosphere.bottomTint
                    )
                    val horizonBrush = Brush.verticalGradient(
                        0f to Color.Transparent,
                        0.62f to Color.Transparent,
                        0.82f to atmosphere.horizonGlow,
                        1f to Color.Black.copy(alpha = 0.08f + atmosphere.night * 0.04f)
                    )
                    val celestialBrush = Brush.radialGradient(
                        colors = listOf(
                            atmosphere.celestialGlow,
                            atmosphere.celestialGlow.copy(alpha = atmosphere.celestialGlow.alpha * 0.2f),
                            Color.Transparent
                        ),
                        center = celestialCenter,
                        radius = size.minDimension * 0.16f
                    )
                    val duskBrush = Brush.radialGradient(
                        colors = listOf(atmosphere.duskBand, Color.Transparent),
                        center = Offset(size.width * viewportProgress, horizonY),
                        radius = size.width * 0.32f
                    )
                    val focusBrush = Brush.radialGradient(
                        colors = listOf(
                            when (style) {
                                PanoramaStyle.Storybook -> Color(0xFFFFE8B8).copy(alpha = 0.08f + zoomProgress * 0.03f)
                                PanoramaStyle.Nordic -> Color.White.copy(alpha = 0.05f + zoomProgress * 0.02f)
                                PanoramaStyle.Arcade -> Color(0xFF8CF7FF).copy(alpha = 0.1f + zoomProgress * 0.04f)
                            },
                            Color.Transparent
                        ),
                        center = focusCenter,
                        radius = size.minDimension * (0.22f + zoomProgress * 0.04f)
                    )

                    onDrawBehind {
                        drawRect(brush = skyBrush, size = Size(size.width, size.height))
                        drawRect(brush = duskBrush, size = Size(size.width, size.height))
                        drawRect(brush = celestialBrush, size = Size(size.width, size.height))
                        drawRect(brush = horizonBrush, size = Size(size.width, size.height))
                        drawPath(path = farSilhouette, color = farSilhouetteColor)
                        drawPath(path = midSilhouette, color = midSilhouetteColor)
                        drawPath(path = foregroundBase, brush = foregroundBaseBrush)
                        drawRect(brush = focusBrush, size = Size(size.width, size.height))
                    }
                }
        )

        scene.layers.forEach { layer ->
            val layerName = layer.assetPath.substringAfterLast('/')
            val parallaxMultiplier = when (layerName) {
                "foreground.svg" -> 1.14f * styleProfile.parallaxBoost
                "mountains_mid.svg" -> 1.05f * styleProfile.parallaxBoost
                else -> 1f
            }
            val layerScale = when (layerName) {
                "foreground.svg" -> 1.18f + zoomProgress * 0.06f
                "mountains_mid.svg" -> 1.12f + zoomProgress * 0.05f
                else -> 1.08f + zoomProgress * 0.04f
            }
            val layerScaleX = layerScale * layer.stretchX
            val renderedWidthPx = sceneWidthPx * layerScaleX
            val rawTranslationX = centeredOffsetPx -
                ((viewportProgress - 0.5f) * availableParallaxPx * layer.parallaxFactor * parallaxMultiplier)
            val minTranslationX = (viewportWidthPx - renderedWidthPx).coerceAtMost(0f)
            val translationX = if (renderedWidthPx <= viewportWidthPx) {
                (viewportWidthPx - renderedWidthPx) / 2f
            } else {
                rawTranslationX.coerceIn(minTranslationX, 0f)
            }
            val translationY = sceneHeightPx * when (layerName) {
                "mountains_far.svg" -> -0.015f
                "mountains_mid.svg" -> 0f
                "foreground.svg" -> 0.03f
                else -> 0f
            }
            val layerAlpha = (layer.alpha * atmosphere.layerAlphaMultiplier(layerName)).coerceIn(0f, 1f)

            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(layer.assetPath)
                    .crossfade(false)
                    .build(),
                imageLoader = imageLoader,
                contentDescription = null,
                alignment = Alignment.BottomStart,
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxHeight()
                    .width(sceneWidthDp)
                    .graphicsLayer {
                        this.translationX = translationX
                        this.translationY = translationY
                        scaleX = layerScaleX
                        scaleY = layerScale
                        alpha = layerAlpha
                    }
            )
        }

        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .drawWithCache {
                    onDrawBehind {
                        drawRect(
                            color = styleProfile.globalTint.copy(alpha = styleProfile.globalTint.alpha * 0.35f),
                            size = Size(size.width, size.height)
                        )
                    }
                }
        )
    }
}

private data class PanoramaStyleProfile(
    val parallaxBoost: Float,
    val globalTint: Color
)

private data class PanoramaAtmosphere(
    val daylight: Float,
    val night: Float,
    val topTint: Color,
    val midTint: Color,
    val bottomTint: Color,
    val horizonGlow: Color,
    val celestialGlow: Color,
    val duskBand: Color
) {
    fun layerAlphaMultiplier(layerName: String): Float = when (layerName) {
        "mountains_far.svg" -> 0.84f + daylight * 0.16f
        "mountains_mid.svg" -> 0.9f + daylight * 0.1f
        "foreground.svg" -> 0.94f + daylight * 0.06f
        else -> 1f
    }
}

private fun panoramaStyleProfile(style: PanoramaStyle): PanoramaStyleProfile = when (style) {
    PanoramaStyle.Storybook -> PanoramaStyleProfile(
        parallaxBoost = 1.04f,
        globalTint = Color(0xFFB56F58).copy(alpha = 0.04f)
    )
    PanoramaStyle.Nordic -> PanoramaStyleProfile(
        parallaxBoost = 1f,
        globalTint = Color.Transparent
    )
    PanoramaStyle.Arcade -> PanoramaStyleProfile(
        parallaxBoost = 1.12f,
        globalTint = Color(0xFF10365D).copy(alpha = 0.05f)
    )
}

private fun panoramaAtmosphereFor(progress: Float, style: PanoramaStyle): PanoramaAtmosphere {
    val normalized = progress.coerceIn(0f, 1f)
    val dawn = triangularPeak(normalized, 0.24f, 0.11f)
    val dusk = triangularPeak(normalized, 0.76f, 0.12f)
    val twilight = max(dawn, dusk)
    val daylight = smooth01(sin(((normalized - 0.25f) * Math.PI * 2).toFloat()).coerceAtLeast(0f))
    val night = (1f - (daylight * 0.92f + twilight * 0.55f)).coerceIn(0f, 1f)

    val dayTop = when (style) {
        PanoramaStyle.Storybook -> Color(0xFF8FDBFF)
        PanoramaStyle.Nordic -> Color(0xFF73C9FF)
        PanoramaStyle.Arcade -> Color(0xFF58D6FF)
    }
    val dayMid = when (style) {
        PanoramaStyle.Storybook -> Color(0xFFFFE1C1)
        PanoramaStyle.Nordic -> Color(0xFFB8E4FF)
        PanoramaStyle.Arcade -> Color(0xFF9BE6FF)
    }
    val dayBottom = when (style) {
        PanoramaStyle.Storybook -> Color(0xFFFFC68F)
        PanoramaStyle.Nordic -> Color(0xFFF3D8A0)
        PanoramaStyle.Arcade -> Color(0xFF8BF8E5)
    }
    val nightTop = when (style) {
        PanoramaStyle.Storybook -> Color(0xFF130E28)
        PanoramaStyle.Nordic -> Color(0xFF081120)
        PanoramaStyle.Arcade -> Color(0xFF030711)
    }
    val nightMid = when (style) {
        PanoramaStyle.Storybook -> Color(0xFF2A2147)
        PanoramaStyle.Nordic -> Color(0xFF10203A)
        PanoramaStyle.Arcade -> Color(0xFF0B1730)
    }
    val nightBottom = when (style) {
        PanoramaStyle.Storybook -> Color(0xFF563458)
        PanoramaStyle.Nordic -> Color(0xFF1B2942)
        PanoramaStyle.Arcade -> Color(0xFF11274A)
    }
    val duskTop = when (style) {
        PanoramaStyle.Storybook -> Color(0x90A457A5)
        PanoramaStyle.Nordic -> Color(0x805B3C8A)
        PanoramaStyle.Arcade -> Color(0x907349FF)
    }
    val duskMid = when (style) {
        PanoramaStyle.Storybook -> Color(0x90FF8C6A)
        PanoramaStyle.Nordic -> Color(0x80E07A6A)
        PanoramaStyle.Arcade -> Color(0x9056A8FF)
    }
    val duskBottom = when (style) {
        PanoramaStyle.Storybook -> Color(0x90FFD28A)
        PanoramaStyle.Nordic -> Color(0x80F2B56B)
        PanoramaStyle.Arcade -> Color(0x9040FFD5)
    }

    val topTint = lerp(lerp(nightTop, dayTop, daylight), duskTop, twilight * 0.7f)
    val midTint = lerp(lerp(nightMid, dayMid, daylight), duskMid, twilight * 0.8f)
    val bottomTint = lerp(lerp(nightBottom, dayBottom, daylight), duskBottom, twilight * 0.88f)

    return PanoramaAtmosphere(
        daylight = daylight,
        night = night,
        topTint = topTint.copy(alpha = 0.54f),
        midTint = midTint.copy(alpha = 0.42f),
        bottomTint = bottomTint.copy(alpha = 0.34f),
        horizonGlow = when (style) {
            PanoramaStyle.Storybook -> Color(0xFFFFC27E).copy(alpha = 0.18f + twilight * 0.08f)
            PanoramaStyle.Nordic -> Color.White.copy(alpha = 0.08f + twilight * 0.06f)
            PanoramaStyle.Arcade -> Color(0xFF6CD9FF).copy(alpha = 0.12f + twilight * 0.06f)
        },
        celestialGlow = when (style) {
            PanoramaStyle.Storybook -> Color(0xFFFFE1A2).copy(alpha = 0.08f + daylight * 0.08f + night * 0.04f)
            PanoramaStyle.Nordic -> Color(0xFFBED8FF).copy(alpha = 0.06f + daylight * 0.04f + night * 0.08f)
            PanoramaStyle.Arcade -> Color(0xFF87F7FF).copy(alpha = 0.08f + daylight * 0.04f + night * 0.1f)
        },
        duskBand = when (style) {
            PanoramaStyle.Storybook -> Color(0xFFFFBA7A).copy(alpha = twilight * 0.16f)
            PanoramaStyle.Nordic -> Color(0xFFFFAA73).copy(alpha = twilight * 0.1f)
            PanoramaStyle.Arcade -> Color(0xFF63C8FF).copy(alpha = twilight * 0.12f)
        }
    )
}

private fun triangularPeak(value: Float, center: Float, width: Float): Float =
    (1f - (abs(value - center) / width)).coerceIn(0f, 1f)

private fun smooth01(value: Float): Float {
    val clamped = value.coerceIn(0f, 1f)
    return clamped * clamped * (3f - 2f * clamped)
}

