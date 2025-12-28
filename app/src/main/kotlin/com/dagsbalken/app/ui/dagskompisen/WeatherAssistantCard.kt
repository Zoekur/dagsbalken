package com.dagsbalken.app.ui.dagskompisen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dagsbalken.app.R
import com.dagsbalken.core.dagskompisen.WeatherContext
import com.dagsbalken.core.dagskompisen.WeatherCondition
import com.dagsbalken.core.dagskompisen.toOutfitName
import com.dagsbalken.core.dagskompisen.toOverlayName

/**
 * Compact, pictogram-only assistant card.
 * Layers images Base -> Outfit -> Overlay without any text bubble.
 */
@Composable
fun WeatherAssistantCard(
    context: WeatherContext,
    modifier: Modifier = Modifier
) {
    // Call extension functions directly to avoid type inference issues with remember
    val outfitName = context.toOutfitName()
    val overlayName = context.toOverlayName()

    // Map names (from core) to drawable resource ids in app module at compile time.
    val baseId = R.drawable.character_base
    val outfitId = remember(outfitName) { nameToDrawableId(outfitName) }
    val overlayId = remember(overlayName) { nameToDrawableId(overlayName) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            if (baseId != 0) {
                Image(
                    painter = painterResource(id = baseId),
                    contentDescription = "Dagskompisen basfigur",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(96.dp)
                )
            }

            if (outfitId != 0) {
                Image(
                    painter = painterResource(id = outfitId),
                    contentDescription = "Dagskompisen klädsel",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(96.dp)
                )
            }

            if (overlayId != 0) {
                Image(
                    painter = painterResource(id = overlayId),
                    contentDescription = "Dagskompisen vädersymbol",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(96.dp)
                )
            }
        }
    }
}

// Static mapping from core-provided names to compile-time drawable ids.
// Map missing assets to reasonable fallbacks that exist in res/drawable.
fun nameToDrawableId(name: String): Int = when (name) {
    "outfit_rain" -> R.drawable.outfit_rain
    "outfit_snow" -> R.drawable.outfit_snow
    // outfit_windy not present in repository; fall back to rain outfit for now
    "outfit_windy" -> R.drawable.outfit_rain
    "outfit_hot" -> R.drawable.outfit_hot

    "overlay_sun" -> R.drawable.overlay_sun
    "overlay_cloudy" -> R.drawable.overlay_cloudy
    "overlay_rain" -> R.drawable.overlay_rain
    // overlay_storm not present; fall back to rain overlay
    "overlay_storm" -> R.drawable.overlay_rain
    "overlay_snow" -> R.drawable.overlay_snow
    "overlay_windy" -> R.drawable.overlay_windy
    "overlay_fog" -> R.drawable.overlay_fog
    "overlay_hot" -> R.drawable.overlay_hot

    else -> 0
}

@Preview(showBackground = true)
@Composable
fun WeatherAssistantCardPreview_Sun() {
    WeatherAssistantCard(
        context = WeatherContext(WeatherCondition.SUN, temperatureC = 22),
        modifier = Modifier
    )
}

@Preview(showBackground = true)
@Composable
fun WeatherAssistantCardPreview_Rain() {
    WeatherAssistantCard(
        context = WeatherContext(WeatherCondition.RAIN, temperatureC = 12),
        modifier = Modifier
    )
}
