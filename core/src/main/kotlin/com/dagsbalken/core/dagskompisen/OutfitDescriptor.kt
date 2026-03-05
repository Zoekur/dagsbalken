package com.dagsbalken.core.dagskompisen

/**
 * Beskriver en lager-på-lager-outfit för Dagskompisen.
 * Alla namn är resursnamn utan filändelse (Android-fria för att kunna testas i core).
 */
data class OutfitDescriptor(
    val baseName: String,   // Alltid hårfri basfigur
    val hairName: String?,  // Null om mössa/huva används
    val topName: String,    // Överdel (t.ex. jacka, tröja, t-shirt)
    val bottomName: String, // Underdel (t.ex. jeans, shorts, termobyxor)
    val shoesName: String,  // Skor (t.ex. sneakers, kängor, stövlar)
    val hatName: String?,   // Mössa/keps/huva, null om ingen
)

