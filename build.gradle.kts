// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    configurations.all {
        resolutionStrategy {
            force("org.jdom:jdom2:2.0.6.1")
            force("org.apache.commons:commons-compress:1.26.1")
            eachDependency {
                if (requested.group == "io.netty" && requested.name.startsWith("netty-")) {
                    useVersion("4.1.115.Final")
                }
            }
        }
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.parcelize) apply false
    alias(libs.plugins.compose.compiler) apply false // Add this line to fix the version conflict
}

allprojects {
    configurations.all {
        resolutionStrategy {
            force("org.jdom:jdom2:2.0.6.1")
            force("org.apache.commons:commons-compress:1.26.1")
            eachDependency {
                if (requested.group == "io.netty" && requested.name.startsWith("netty-")) {
                    useVersion("4.1.115.Final")
                }
            }
        }
    }
}
