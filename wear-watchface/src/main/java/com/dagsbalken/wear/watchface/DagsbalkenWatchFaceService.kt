package com.dagsbalken.wear.watchface

import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.view.SurfaceHolder
import androidx.wear.watchface.CanvasType
import androidx.wear.watchface.ComplicationSlotsManager
import androidx.wear.watchface.Renderer
import androidx.wear.watchface.WatchFace
import androidx.wear.watchface.WatchFaceService
import androidx.wear.watchface.WatchFaceType
import androidx.wear.watchface.WatchState
import androidx.wear.watchface.style.CurrentUserStyleRepository
import androidx.wear.watchface.style.UserStyleSchema
import androidx.wear.watchface.style.UserStyleSetting
import androidx.wear.watchface.style.WatchFaceLayer
import com.dagsbalken.core.data.CalendarRepository
import com.dagsbalken.core.data.DayEvent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.atomic.AtomicReference

/**
 * Dagsbalken watch face service for Galaxy Watch.
 * Shows a horizontal curved timeline with day/night gradient and events.
 */
class DagsbalkenWatchFaceService : WatchFaceService() {

    companion object {
        const val THEME_STYLE_SETTING = "theme_setting"
        const val AOD_POSITION_SETTING = "aod_position_setting"
        const val SHOW_EVENTS_SETTING = "show_events_setting"

        const val THEME_COLD = "cold"
        const val THEME_WARM = "warm"
        const val THEME_COLD_HC = "cold_hc"
        const val THEME_WARM_HC = "warm_hc"

        const val AOD_POS_TOP = "top"
        const val AOD_POS_MIDDLE = "middle"
        const val AOD_POS_BOTTOM = "bottom"
    }

    override fun createUserStyleSchema(): UserStyleSchema {
        // Note: This module currently keeps the watch face functional.
        // A richer settings UI can be added later via a configuration activity.

        val themeSetting = UserStyleSetting.ListUserStyleSetting(
            UserStyleSetting.Id(THEME_STYLE_SETTING),
            resources,
            R.string.theme_setting_name,
            R.string.theme_setting_description,
            null,
            listOf(
                UserStyleSetting.ListUserStyleSetting.ListOption(
                    UserStyleSetting.Option.Id(THEME_COLD),
                    resources,
                    R.string.theme_cold,
                    R.string.theme_cold,
                    null
                ),
                UserStyleSetting.ListUserStyleSetting.ListOption(
                    UserStyleSetting.Option.Id(THEME_WARM),
                    resources,
                    R.string.theme_warm,
                    R.string.theme_warm,
                    null
                ),
                UserStyleSetting.ListUserStyleSetting.ListOption(
                    UserStyleSetting.Option.Id(THEME_COLD_HC),
                    resources,
                    R.string.theme_cold_hc,
                    R.string.theme_cold_hc,
                    null
                ),
                UserStyleSetting.ListUserStyleSetting.ListOption(
                    UserStyleSetting.Option.Id(THEME_WARM_HC),
                    resources,
                    R.string.theme_warm_hc,
                    R.string.theme_warm_hc,
                    null
                )
            ),
            WatchFaceLayer.ALL_WATCH_FACE_LAYERS
        )

        val aodPositionSetting = UserStyleSetting.ListUserStyleSetting(
            UserStyleSetting.Id(AOD_POSITION_SETTING),
            resources,
            R.string.aod_position_setting_name,
            R.string.aod_position_setting_description,
            null,
            listOf(
                UserStyleSetting.ListUserStyleSetting.ListOption(
                    UserStyleSetting.Option.Id(AOD_POS_TOP),
                    resources,
                    R.string.aod_position_top,
                    R.string.aod_position_top,
                    null
                ),
                UserStyleSetting.ListUserStyleSetting.ListOption(
                    UserStyleSetting.Option.Id(AOD_POS_MIDDLE),
                    resources,
                    R.string.aod_position_middle,
                    R.string.aod_position_middle,
                    null
                ),
                UserStyleSetting.ListUserStyleSetting.ListOption(
                    UserStyleSetting.Option.Id(AOD_POS_BOTTOM),
                    resources,
                    R.string.aod_position_bottom,
                    R.string.aod_position_bottom,
                    null
                )
            ),
            WatchFaceLayer.ALL_WATCH_FACE_LAYERS
        )

        val showEventsSetting = UserStyleSetting.BooleanUserStyleSetting(
            UserStyleSetting.Id(SHOW_EVENTS_SETTING),
            resources,
            R.string.show_events_setting_name,
            R.string.show_events_setting_description,
            null,
            WatchFaceLayer.ALL_WATCH_FACE_LAYERS,
            true
        )

        return UserStyleSchema(listOf(themeSetting, aodPositionSetting, showEventsSetting))
    }

    override suspend fun createWatchFace(
        surfaceHolder: SurfaceHolder,
        watchState: WatchState,
        complicationSlotsManager: ComplicationSlotsManager,
        currentUserStyleRepository: CurrentUserStyleRepository
    ): WatchFace {
        val renderer = DagsbalkenRenderer(
            context = this,
            surfaceHolder = surfaceHolder,
            watchState = watchState,
            userStyleRepository = currentUserStyleRepository
        )

        return WatchFace(
            watchFaceType = WatchFaceType.DIGITAL,
            renderer = renderer
        )
    }

    private class DagsbalkenRenderer(
        private val context: WatchFaceService,
        surfaceHolder: SurfaceHolder,
        watchState: WatchState,
        private val userStyleRepository: CurrentUserStyleRepository
    ) : Renderer.CanvasRenderer2<DagsbalkenRenderer.DagsbalkenSharedAssets>(
        surfaceHolder = surfaceHolder,
        currentUserStyleRepository = userStyleRepository,
        watchState = watchState,
        canvasType = CanvasType.HARDWARE,
        interactiveDrawModeUpdateDelayMillis = 60_000L,
        clearWithBackgroundTintBeforeRenderingHighlightLayer = false
    ) {

        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
        private val events = AtomicReference<List<DayEvent>>(emptyList())
        private var lastEventReloadHour = -1

        class DagsbalkenSharedAssets : SharedAssets {
            override fun onDestroy() {}
        }

        override suspend fun createSharedAssets(): DagsbalkenSharedAssets {
            loadEvents()
            return DagsbalkenSharedAssets()
        }

        private fun loadEvents() {
            scope.launch(Dispatchers.IO) {
                try {
                    val calendarRepo = CalendarRepository(context)
                    events.set(calendarRepo.getEventsForToday())
                } catch (_: Exception) {
                    events.set(emptyList())
                }
            }
        }

        override fun render(
            canvas: Canvas,
            bounds: Rect,
            zonedDateTime: ZonedDateTime,
            sharedAssets: DagsbalkenSharedAssets
        ) {
            val currentHour = zonedDateTime.hour
            if (currentHour != lastEventReloadHour) {
                lastEventReloadHour = currentHour
                loadEvents()
            }

            val isAmbient = renderParameters.drawMode == androidx.wear.watchface.DrawMode.AMBIENT
            drawWatchFace(canvas, bounds, zonedDateTime, isAmbient)
        }

        override fun renderHighlightLayer(
            canvas: Canvas,
            bounds: Rect,
            zonedDateTime: ZonedDateTime,
            sharedAssets: DagsbalkenSharedAssets
        ) = Unit

        private fun drawWatchFace(canvas: Canvas, bounds: Rect, zonedDateTime: ZonedDateTime, isAmbient: Boolean) {
            val selectedTheme = when (
                userStyleRepository.userStyle.value[UserStyleSetting.Id(THEME_STYLE_SETTING)]?.toString()
            ) {
                THEME_WARM -> ThemeColors.Warm
                THEME_COLD_HC -> ThemeColors.ColdHighContrast
                THEME_WARM_HC -> ThemeColors.WarmHighContrast
                else -> ThemeColors.Cold
            }

            val aodPosition = when (
                userStyleRepository.userStyle.value[UserStyleSetting.Id(AOD_POSITION_SETTING)]?.toString()
            ) {
                AOD_POS_TOP -> 0.3f
                AOD_POS_BOTTOM -> 0.7f
                else -> 0.5f
            }

            val showEvents = userStyleRepository.userStyle.value[
                UserStyleSetting.Id(SHOW_EVENTS_SETTING)
            ]?.toString() != "false"

            val paint = Paint().apply {
                isAntiAlias = !isAmbient
                color = if (isAmbient) 0xFF000000.toInt() else selectedTheme.background
            }
            canvas.drawRect(bounds, paint)

            if (isAmbient) {
                drawAmbientMode(canvas, bounds, zonedDateTime, aodPosition, paint)
            } else {
                drawInteractiveMode(canvas, bounds, zonedDateTime, selectedTheme, showEvents, paint)
            }
        }

        private fun drawAmbientMode(
            canvas: Canvas,
            bounds: Rect,
            zonedDateTime: ZonedDateTime,
            verticalPosition: Float,
            paint: Paint
        ) {
            val barHeight = 40f
            val barTop = bounds.height() * 0.8f
            val barBottom = barTop + barHeight

            paint.color = 0xFF333333.toInt()
            paint.style = Paint.Style.FILL
            canvas.drawRect(0f, barTop, bounds.width().toFloat(), barBottom, paint)

            val currentMinutes = zonedDateTime.hour * 60 + zonedDateTime.minute
            val progress = currentMinutes.toFloat() / (24 * 60)
            paint.color = 0xFFFFFFFF.toInt()
            canvas.drawRect(0f, barTop, bounds.width() * progress, barBottom, paint)

            val currentX = bounds.width() * progress
            paint.strokeWidth = 4f
            canvas.drawLine(currentX, barTop - 20f, currentX, barBottom, paint)

            paint.style = Paint.Style.FILL
            paint.color = 0xFFFFFFFF.toInt()
            paint.textAlign = Paint.Align.CENTER
            paint.textSize = bounds.width() * 0.18f

            val timeText = zonedDateTime.format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()))
            val centerY = bounds.height() * verticalPosition
            canvas.drawText(timeText, bounds.exactCenterX(), centerY, paint)
        }

        private fun drawInteractiveMode(
            canvas: Canvas,
            bounds: Rect,
            zonedDateTime: ZonedDateTime,
            theme: ThemeColors,
            showEvents: Boolean,
            paint: Paint
        ) {
            val width = bounds.width().toFloat()
            val height = bounds.height().toFloat()

            val barHeight = height * 0.25f
            val barTop = height * 0.62f
            val barRect = RectF(0f, barTop, width, barTop + barHeight)

            val gradient = LinearGradient(
                0f,
                barTop,
                width,
                barTop,
                intArrayOf(theme.timelineNightColor, theme.timelineDayColor, theme.timelineNightColor),
                floatArrayOf(0f, 0.5f, 1f),
                Shader.TileMode.CLAMP
            )

            paint.shader = gradient
            paint.style = Paint.Style.FILL
            canvas.drawRoundRect(barRect, 20f, 20f, paint)
            paint.shader = null

            val currentMinutes = zonedDateTime.hour * 60 + zonedDateTime.minute
            val progress = currentMinutes.toFloat() / (24 * 60)
            val currentX = width * progress

            paint.color = theme.currentTimeColor
            paint.strokeWidth = 6f
            canvas.drawLine(currentX, barTop - 20f, currentX, barTop + barHeight + 20f, paint)

            if (showEvents) {
                drawEvents(canvas, barTop, barHeight, width, theme, paint)
            }

            drawTimeLabels(canvas, bounds, zonedDateTime, theme, paint)
        }

        private fun drawEvents(
            canvas: Canvas,
            barTop: Float,
            barHeight: Float,
            width: Float,
            theme: ThemeColors,
            paint: Paint
        ) {
            val currentEvents = events.get()
            if (currentEvents.isEmpty()) return

            paint.style = Paint.Style.FILL
            paint.color = theme.eventColor

            for (event in currentEvents) {
                val startMinutes = event.start.hour * 60 + event.start.minute
                val endMinutes = (event.end ?: event.start).let { it.hour * 60 + it.minute }

                val startX = width * (startMinutes.toFloat() / (24 * 60))
                val endX = width * (endMinutes.toFloat() / (24 * 60))

                val eventRect = RectF(
                    startX,
                    barTop + barHeight * 0.1f,
                    endX,
                    barTop + barHeight * 0.9f
                )
                canvas.drawRoundRect(eventRect, 12f, 12f, paint)
            }
        }

        private fun drawTimeLabels(
            canvas: Canvas,
            bounds: Rect,
            zonedDateTime: ZonedDateTime,
            theme: ThemeColors,
            paint: Paint
        ) {
            paint.shader = null
            paint.style = Paint.Style.FILL
            paint.color = theme.textColor
            paint.textAlign = Paint.Align.CENTER
            paint.textSize = bounds.width() * 0.12f

            val timeText = zonedDateTime.format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()))
            canvas.drawText(timeText, bounds.exactCenterX(), bounds.height() * 0.28f, paint)
        }
    }
}

private sealed class ThemeColors(
    val background: Int,
    val timelineNightColor: Int,
    val timelineDayColor: Int,
    val currentTimeColor: Int,
    val eventColor: Int,
    val textColor: Int
) {
    data object Cold : ThemeColors(
        background = 0xFF000000.toInt(),
        timelineNightColor = 0xFF0A1A2A.toInt(),
        timelineDayColor = 0xFF2A6FA8.toInt(),
        currentTimeColor = 0xFFFFD54F.toInt(),
        eventColor = 0xFF4FC3F7.toInt(),
        textColor = 0xFFFFFFFF.toInt()
    )

    data object Warm : ThemeColors(
        background = 0xFF000000.toInt(),
        timelineNightColor = 0xFF2A0A0A.toInt(),
        timelineDayColor = 0xFFA85A2A.toInt(),
        currentTimeColor = 0xFFFFEB3B.toInt(),
        eventColor = 0xFFFFAB91.toInt(),
        textColor = 0xFFFFFFFF.toInt()
    )

    data object ColdHighContrast : ThemeColors(
        background = 0xFF000000.toInt(),
        timelineNightColor = 0xFF000000.toInt(),
        timelineDayColor = 0xFFFFFFFF.toInt(),
        currentTimeColor = 0xFFFFD54F.toInt(),
        eventColor = 0xFFFFFFFF.toInt(),
        textColor = 0xFFFFFFFF.toInt()
    )

    data object WarmHighContrast : ThemeColors(
        background = 0xFF000000.toInt(),
        timelineNightColor = 0xFF000000.toInt(),
        timelineDayColor = 0xFFFFFFFF.toInt(),
        currentTimeColor = 0xFFFFEB3B.toInt(),
        eventColor = 0xFFFFFFFF.toInt(),
        textColor = 0xFFFFFFFF.toInt()
    )
}
