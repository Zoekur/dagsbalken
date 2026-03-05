package com.dagsbalken.app.ui.dagskompisen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
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
import com.dagsbalken.core.dagskompisen.OutfitDescriptor
import com.dagsbalken.core.dagskompisen.WeatherContext
import com.dagsbalken.core.dagskompisen.WeatherCondition
import com.dagsbalken.core.dagskompisen.toOutfitDescriptor
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
    val outfit: OutfitDescriptor = context.toOutfitDescriptor()
    val overlayName = context.toOverlayName()

    val baseId = remember(outfit.baseName) { layerNameToDrawableId(outfit.baseName) }
    val hairId = remember(outfit.hairName) { outfit.hairName?.let { layerNameToDrawableId(it) } ?: 0 }
    val topId = remember(outfit.topName) { layerNameToDrawableId(outfit.topName) }
    val bottomId = remember(outfit.bottomName) { layerNameToDrawableId(outfit.bottomName) }
    val shoesId = remember(outfit.shoesName) { layerNameToDrawableId(outfit.shoesName) }
    val hatId = remember(outfit.hatName) { outfit.hatName?.let { layerNameToDrawableId(it) } ?: 0 }
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
                .fillMaxHeight()
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            val characterModifier = Modifier.fillMaxHeight()

            if (baseId != 0) {
                Image(
                    painter = painterResource(id = baseId),
                    contentDescription = "Dagskompisen basfigur",
                    contentScale = ContentScale.Fit,
                    modifier = characterModifier
                )
            }

            if (bottomId != 0) {
                Image(
                    painter = painterResource(id = bottomId),
                    contentDescription = "Dagskompisen underdel",
                    contentScale = ContentScale.Fit,
                    modifier = characterModifier
                )
            }

            if (shoesId != 0) {
                Image(
                    painter = painterResource(id = shoesId),
                    contentDescription = "Dagskompisen skor",
                    contentScale = ContentScale.Fit,
                    modifier = characterModifier
                )
            }

            if (topId != 0) {
                Image(
                    painter = painterResource(id = topId),
                    contentDescription = "Dagskompisen överdel",
                    contentScale = ContentScale.Fit,
                    modifier = characterModifier
                )
            }

            if (hairId != 0) {
                Image(
                    painter = painterResource(id = hairId),
                    contentDescription = "Dagskompisen hår",
                    contentScale = ContentScale.Fit,
                    modifier = characterModifier
                )
            }

            if (hatId != 0) {
                Image(
                    painter = painterResource(id = hatId),
                    contentDescription = "Dagskompisen mössa",
                    contentScale = ContentScale.Fit,
                    modifier = characterModifier
                )
            }

            if (overlayId != 0) {
                Image(
                    painter = painterResource(id = overlayId),
                    contentDescription = "Dagskompisen vädersymbol",
                    contentScale = ContentScale.Fit,
                    modifier = characterModifier
                )
            }
        }
    }
}

// Static mapping from core-provided names to compile-time drawable ids.
// For now we don't map overlay_* here to avoid build issues; return 0 to skip overlay.
fun nameToDrawableId(name: String): Int = 0

// Map lager-namn (utan filändelse) till nya WebP-resurser.
fun layerNameToDrawableId(name: String): Int = when (name) {
    "base" -> R.drawable.base
    "basepojken-hair" -> R.drawable.basepojken_hair
    "baspojken-hairless" -> R.drawable.baspojken_hairless

    "hair" -> R.drawable.hair

    "jeans" -> R.drawable.jeans
    "jeans_g" -> R.drawable.jeans_g
    "jorts" -> R.drawable.jorts

    "shirt_lb" -> R.drawable.shirt_lb
    "coat-winter" -> R.drawable.coat_winter
    "raincoat" -> R.drawable.raincoat
    "raincoat_hood" -> R.drawable.raincoat_hood

    "boots_winter" -> R.drawable.boots_winter
    "sneakers_v" -> R.drawable.sneakers_v
    "boot_grey" -> R.drawable.boot_grey
    "boot_y" -> R.drawable.boot_y

    "winter-hat" -> R.drawable.winter_hat

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
