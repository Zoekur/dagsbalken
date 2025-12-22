plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.dagsbalken.wear.watchface"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.dagsbalken.wear.watchface"
        minSdk = 33
        targetSdk = 36

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        // We're using Canvas rendering inside the WatchFaceService, but Compose is handy for any future config UI.
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":core"))

    implementation(libs.androidx.core.ktx)

    // Wear OS watch face
    implementation(libs.androidx.wear.watchface)
    implementation(libs.androidx.wear.watchface.complications.rendering)
    implementation(libs.androidx.wear.watchface.data)
    implementation(libs.androidx.wear.watchface.style)
    implementation(libs.androidx.wear.watchface.editor)

    // Optional (if we add a compose-based configuration activity later)
    implementation(libs.androidx.wear.compose.material)
    implementation(libs.androidx.wear.compose.foundation)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
}

