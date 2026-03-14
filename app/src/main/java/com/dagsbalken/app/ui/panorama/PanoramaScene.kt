package com.dagsbalken.app.ui.panorama

private const val PanoramaAssetPrefix = "file:///android_asset/panorama/"
private const val PanoramaAspectRatio = 4096f / 512f

data class PanoramaLayer(
    val assetPath: String,
    val parallaxFactor: Float,
    val alpha: Float = 1f,
    val stretchX: Float = 1f
)

data class PanoramaScene(
    val aspectRatio: Float,
    val layers: List<PanoramaLayer>
)

val DagsbalkenPanoramaScene = PanoramaScene(
    aspectRatio = PanoramaAspectRatio,
    layers = listOf(
        PanoramaLayer(
            assetPath = PanoramaAssetPrefix + "mountains_far.svg",
            parallaxFactor = 0.18f,
            stretchX = 1.55f
        ),
        PanoramaLayer(
            assetPath = PanoramaAssetPrefix + "mountains_mid.svg",
            parallaxFactor = 0.30f,
            stretchX = 1.38f
        ),
        PanoramaLayer(
            assetPath = PanoramaAssetPrefix + "foreground.svg",
            parallaxFactor = 1.00f,
            stretchX = 1.22f
        )
    )
)

