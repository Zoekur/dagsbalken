package com.dagsbalken.core.schedule

fun glyphForIcon(iconKey: String, style: IconStyle): String = when (style) {
    IconStyle.EmojiClassic -> when (iconKey) {
        "food" -> "🍽"
        "school" -> "🏫"
        "sport" -> "🏃"
        "sleep" -> "😴"
        else -> "★"
    }
    IconStyle.EmojiSimple -> when (iconKey) {
        "food" -> "🍴"
        "school" -> "🎒"
        "sport" -> "⚽"
        "sleep" -> "🌙"
        else -> "•"
    }
    IconStyle.EmojiHighContrast -> when (iconKey) {
        "food" -> "🍽"
        "school" -> "🎓"
        "sport" -> "🏅"
        "sleep" -> "🛏"
        else -> "✪"
    }
}
