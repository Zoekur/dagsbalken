plugins {

    alias(libs.plugins.android.application)
    alias(libs.plugins.org.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    namespace = "com.dagsbalken.app"
        // SDK 36 är en stabil och bra version att kompilera mot.
    compileSdk = 36

    defaultConfig {
        applicationId = "com.com.dagsbalken.com.com.dagsbalken.app"
        // minSdk 33 är ett bra val för moderna funktioner som Glance.
        minSdk = 33
        targetSdk = 36

        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        // Aktiverar Jetpack Compose för modulen.
        compose = true
        // Aktiverar BuildConfig för att kunna läsa versionsnamn
        buildConfig = true
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

// Allt beroende (dependencies) ska ligga i ett enda block.
dependencies {
    implementation(project(":core"))
    // Kärnbibliotek
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)

    // Jetpack Compose
    // Använder "Bill of Materials" (BOM) för att hantera Compose-versioner.
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material.icons.extended)

    // Accompanist (för System UI Controller)
    implementation(libs.accompanist.systemuicontroller)

    // Glance (för App Widgets)
    implementation(libs.androidx.glance)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    // WorkManager (för bakgrundsjobb)
    implementation(libs.androidx.work.runtime.ktx)

    // DataStore (för att spara data asynkront)
    implementation(libs.androidx.datastore.preferences)

    // Testbibliotek
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)

    // Debug-verktyg för Compose
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
