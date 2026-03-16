package com.dagsbalken.app.ui.panorama

import kotlin.math.abs

enum class LodLevel {
    FAR,
    MID,
    NEAR,
    DETAIL
}

enum class ActivitySceneType {
    Home,
    Commute,
    School,
    Workout,
    Meal,
    Rest,
    FreeTime
}

data class ActivityScene(
    val type: ActivitySceneType,
    val assetKey: String = type.name.lowercase(),
    val detailsEnabled: Boolean = false
)

data class HourZone(
    val hour: Int,
    val startMinute: Int,
    val endMinute: Int,
    val scene: ActivityScene
)

data class CameraState(
    val centerMinute: Float,
    val zoom: Float,
    val lodLevel: LodLevel
)

data class DayWorld(
    val zones: List<HourZone>
) {
    fun zoneAt(minute: Int): HourZone? = zones.firstOrNull { minute in it.startMinute until it.endMinute }

    companion object {
        fun default(): DayWorld = DayWorld(
            zones = (0 until 24).map { hour ->
                val sceneType = when (hour) {
                    in 0..5 -> ActivitySceneType.Rest
                    in 6..8 -> ActivitySceneType.Home
                    in 9..15 -> ActivitySceneType.School
                    16, 17 -> ActivitySceneType.Commute
                    18 -> ActivitySceneType.Meal
                    19, 20 -> ActivitySceneType.Workout
                    else -> ActivitySceneType.FreeTime
                }
                HourZone(
                    hour = hour,
                    startMinute = hour * 60,
                    endMinute = (hour + 1) * 60,
                    scene = ActivityScene(type = sceneType)
                )
            }
        )
    }
}

fun lodLevelForZoom(zoom: Float): LodLevel = when {
    zoom < 0.3f -> LodLevel.FAR
    zoom < 0.55f -> LodLevel.MID
    zoom < 0.8f -> LodLevel.NEAR
    else -> LodLevel.DETAIL
}

fun cameraZoomProgress(visibleDurationMinutes: Int): Float {
    val minDuration = 2 * 60
    val maxDuration = 24 * 60
    val clamped = visibleDurationMinutes.coerceIn(minDuration, maxDuration)
    return ((maxDuration - clamped).toFloat() / (maxDuration - minDuration).toFloat()).coerceIn(0f, 1f)
}

fun cameraStateFor(centerMinute: Float, visibleDurationMinutes: Int): CameraState {
    val zoom = cameraZoomProgress(visibleDurationMinutes)
    return CameraState(
        centerMinute = centerMinute.coerceIn(0f, 24f * 60f),
        zoom = zoom,
        lodLevel = lodLevelForZoom(zoom)
    )
}

fun layerDetailAlphaBoost(lodLevel: LodLevel, layerName: String): Float {
    val detailSensitive = layerName in setOf("forest.svg", "foreground.svg", "clouds_front.svg")
    if (!detailSensitive) return 1f

    return when (lodLevel) {
        LodLevel.FAR -> 0.92f
        LodLevel.MID -> 1f
        LodLevel.NEAR -> 1.06f
        LodLevel.DETAIL -> 1.12f
    }
}

fun parallaxCoverageMultiplier(parallax: Float): Float = 1f + abs(parallax) * 0.7f
