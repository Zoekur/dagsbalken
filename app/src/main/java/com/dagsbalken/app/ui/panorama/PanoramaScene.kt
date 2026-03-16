package com.dagsbalken.app.ui.panorama

private const val PanoramaAssetPrefix = "file:///android_asset/panorama/"
private const val PanoramaAspectRatio = 4096f / 512f

data class PanoramaLayer(
    val assetPath: String,
    val parallaxFactor: Float,
    val alpha: Float = 1f,
    val stretchX: Float = 1f,
    val maxZoomViewportCoverage: Float = 1.1f,
    val seamSafetyPx: Float = 72f
)

data class PanoramaScene(
    val aspectRatio: Float,
    val layers: List<PanoramaLayer>
)

val DagsbalkenPanoramaScene = PanoramaScene(
    aspectRatio = PanoramaAspectRatio,
    layers = listOf(
        PanoramaLayer(
            assetPath = PanoramaAssetPrefix + "sky.svg",
            parallaxFactor = 0f,
            alpha = 1f,
            stretchX = 1.02f,
            maxZoomViewportCoverage = 2.2f,
            seamSafetyPx = 112f
        ),
        PanoramaLayer(
            assetPath = PanoramaAssetPrefix + "stars.svg",
            parallaxFactor = 0.01f,
            alpha = 0.9f,
            stretchX = 1.02f,
            maxZoomViewportCoverage = 2.15f,
            seamSafetyPx = 112f
        ),
        PanoramaLayer(
            assetPath = PanoramaAssetPrefix + "celestial.svg",
            parallaxFactor = 0.03f,
            alpha = 1f,
            stretchX = 1.03f,
            maxZoomViewportCoverage = 2.1f,
            seamSafetyPx = 108f
        ),
        PanoramaLayer(
            assetPath = PanoramaAssetPrefix + "clouds_back.svg",
            parallaxFactor = 0.08f,
            alpha = 0.82f,
            stretchX = 1.08f,
            maxZoomViewportCoverage = 2f,
            seamSafetyPx = 96f
        ),
        PanoramaLayer(
            assetPath = PanoramaAssetPrefix + "mountains_far.svg",
            parallaxFactor = 0.18f,
            alpha = 1f,
            stretchX = 1.14f,
            maxZoomViewportCoverage = 1.82f,
            seamSafetyPx = 88f
        ),
        PanoramaLayer(
            assetPath = PanoramaAssetPrefix + "mountains_mid.svg",
            parallaxFactor = 0.30f,
            alpha = 1f,
            stretchX = 1.10f,
            maxZoomViewportCoverage = 1.66f,
            seamSafetyPx = 84f
        ),
        PanoramaLayer(
            assetPath = PanoramaAssetPrefix + "clouds_front.svg",
            parallaxFactor = 0.42f,
            alpha = 0.94f,
            stretchX = 1.08f,
            maxZoomViewportCoverage = 1.52f,
            seamSafetyPx = 84f
        ),
        PanoramaLayer(
            assetPath = PanoramaAssetPrefix + "forest.svg",
            parallaxFactor = 0.62f,
            alpha = 1f,
            stretchX = 1.07f,
            maxZoomViewportCoverage = 1.38f,
            seamSafetyPx = 80f
        ),
        PanoramaLayer(
            assetPath = PanoramaAssetPrefix + "foreground.svg",
            parallaxFactor = 1.00f,
            alpha = 1f,
            stretchX = 1.05f,
            maxZoomViewportCoverage = 1.26f,
            seamSafetyPx = 72f
        )
    )
)
