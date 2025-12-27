package com.dagsbalken.app.ui.dagskompisen

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.dagsbalken.core.dagskompisen.WeatherContext
import com.dagsbalken.core.dagskompisen.WeatherCondition
import com.dagsbalken.core.dagskompisen.assistant.AssistantMessageProvider
import com.dagsbalken.core.dagskompisen.assistant.RuleBasedMessageProvider

/**
 * Small, reusable assistant card that layers images: Base -> Outfit -> Overlay
 * and shows a short message from the assistant. No animation included but the
 * structure supports adding animations later.
 */
@Composable
fun WeatherAssistantCard(
    context: WeatherContext,
    modifier: Modifier = Modifier,
    messageProvider: AssistantMessageProvider = RuleBasedMessageProvider()
) {
    // Use the extension functions as methods on the WeatherContext instance.
    val outfitName = remember(context) { context.toOutfitName() }
    val overlayName = remember(context) { context.toOverlayName() }

    // Map names (from core) to drawable resource ids in app module at runtime.
    val composeContext = LocalContext.current
    val baseId = composeContext.resources.getIdentifier("character_base", "drawable", composeContext.packageName)
    val outfitId = remember(outfitName) { nameToDrawableId(outfitName, composeContext) }
    val overlayId = remember(overlayName) { nameToDrawableId(overlayName, composeContext) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .height(96.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(modifier = Modifier.size(72.dp), contentAlignment = Alignment.Center) {
                if (baseId != 0) {
                    Image(
                        painter = painterResource(id = baseId),
                        contentDescription = "Dagskompisen base",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.matchParentSize()
                    )
                }

                if (outfitId != 0) {
                    Image(
                        painter = painterResource(id = outfitId),
                        contentDescription = "Outfit",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.matchParentSize()
                    )
                }

                if (overlayId != 0) {
                    Image(
                        painter = painterResource(id = overlayId),
                        contentDescription = "Weather overlay",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.matchParentSize()
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                val msg = remember(context) { messageProvider.message(context) }
                Text(text = msg, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}

// Resolve resource id by name at runtime. This avoids compile-time dependency on
// the presence of specific drawable resources and keeps the app building even
// when assets are temporarily missing.
fun nameToDrawableId(name: String, ctx: Context): Int {
    if (name.isBlank()) return 0
    return ctx.resources.getIdentifier(name, "drawable", ctx.packageName)
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
