import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    // iOS targets with dynamic framework (required for Compose Multiplatform)
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.compilations.all {
            compilerOptions.configure {
                // Disable optimizations that create $artificial symbols
                freeCompilerArgs.add("-Xno-param-assertions")
                freeCompilerArgs.add("-Xno-call-assertions")
                freeCompilerArgs.add("-Xno-receiver-assertions")
            }
        }

        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = false  // Dynamic framework resolves symbols at runtime

            // Enable transitive export to include all dependencies
            transitiveExport = true

            // Export base ViewModel library (not compose variant to avoid $artificial symbols)
            export(libs.androidx.lifecycle.viewmodel)
            // Export Compose and other dependencies
            export(compose.runtime)
            export(compose.foundation)
            export(compose.material3)
            export(compose.ui)
            export(libs.androidx.lifecycle.viewmodelCompose)
            export(libs.androidx.lifecycle.runtimeCompose)
            export(libs.androidx.navigation.compose)
            export(libs.koin.core)
            export(libs.koin.compose)
            export(libs.koin.compose.viewmodel)
        }
    }
    
    sourceSets {
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
            implementation(libs.androidx.browser)
            implementation(libs.androidx.security.crypto)
            implementation(libs.ktor.client.okhttp)
            implementation(libs.koin.android)
        }
        commonMain.dependencies {
            // Use api() for core Compose dependencies needed by iOS framework
            api(compose.runtime)
            api(compose.foundation)
            api(compose.material3)
            implementation(compose.materialIconsExtended)
            api(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            // Base lifecycle ViewModel (KMP-compatible, no $artificial symbols)
            api(libs.androidx.lifecycle.viewmodel)
            // Compose-specific lifecycle dependencies
            api(libs.androidx.lifecycle.viewmodelCompose)
            api(libs.androidx.lifecycle.runtimeCompose)
            api(libs.androidx.navigation.compose)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.content.negotiation)
            implementation(libs.ktor.serialization.kotlinx.json)
            implementation(libs.ktor.client.logging)
            // Koin DI - use api() for exported dependencies
            api(libs.koin.core)
            api(libs.koin.compose)
            api(libs.koin.compose.viewmodel)
            // Coil for image loading
            implementation(libs.coil.compose)
            implementation(libs.coil.network.ktor3)
        }
        iosMain.dependencies {
            implementation(libs.ktor.client.darwin)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
            implementation(libs.turbine)
            implementation(libs.ktor.client.mock)
        }
    }
}

android {
    namespace = "com.example.mysterybox"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.example.mysterybox"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
    // LINE SDK for Android (must be in dependencies block for proper resolution)
    add("androidMainImplementation", "com.linecorp.linesdk:linesdk:5.9.0")
}

