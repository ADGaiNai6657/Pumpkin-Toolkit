import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }

    android {
        namespace = "com.pgigi.pumpkintoolkit.shared"
        compileSdk = libs.versions.android.compileSdk.get().toInt()
        minSdk = libs.versions.android.minSdk.get().toInt()

        compilerOptions {
            jvmTarget = JvmTarget.JVM_11
        }
        androidResources {
            enable = true
        }
        withHostTest {
            isIncludeAndroidResources = true
        }
        withDeviceTestBuilder {
            sourceSetTreeName = "test"
        }.configure {
            instrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        }
    }

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.compose.uiTooling)

            implementation(libs.ktor.android)

            // miuix
            implementation(libs.miuix.ui.android)
            implementation(libs.miuix.preference.android)
            implementation(libs.miuix.icons.android)
            implementation(libs.miuix.navigation3.android)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)

            implementation(libs.navigation3)
            implementation(libs.datetime)
            implementation(libs.ksoup)
            implementation(libs.ktor.core)
//            implementation(libs.ktor.resources)
            implementation(libs.kvault)
            implementation(libs.okio)
            implementation(libs.serialization.json)
            implementation(libs.compose.webview)

            implementation(libs.coil.compose)
            implementation(libs.htmlconverter)

            // miuix
            implementation(libs.miuix.ui)
            implementation(libs.miuix.preference)
            implementation(libs.miuix.icons)
            implementation(libs.miuix.navigation3)
        }
        iosArm64Main.dependencies {
            implementation(libs.ktor.darwin)
            // miuix
            implementation(libs.miuix.ui.iosarm64)
            implementation(libs.miuix.preference.iosarm64)
            implementation(libs.miuix.icons.iosarm64)
            implementation(libs.miuix.navigation3.iosarm64)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

dependencies {
    androidRuntimeClasspath(libs.compose.uiTooling)
}