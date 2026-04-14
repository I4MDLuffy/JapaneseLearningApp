import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.room)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    // Treat src/main/java as commonMain so all existing code compiles for both platforms.
    // The handful of Android-specific files have been moved to src/androidMain.
    sourceSets {
        commonMain {
            kotlin.srcDirs("src/main/java", "src/commonMain/kotlin")
            resources.srcDirs("src/commonMain/composeResources")
            dependencies {
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.materialIconsExtended)
                implementation(compose.components.resources)
                implementation(compose.components.uiToolingPreview)
                implementation(libs.androidx.lifecycle.viewmodel)
                implementation(libs.androidx.lifecycle.viewmodel.compose)
                implementation(libs.androidx.navigation.compose)
                implementation(libs.kotlinx.serialization.json)
                implementation(libs.kotlinx.coroutines.core)
                implementation(libs.androidx.room.runtime)
                implementation(libs.multiplatform.settings)
                implementation(libs.kotlinx.datetime)
            }
        }

        androidMain {
            kotlin.srcDirs("src/androidMain/kotlin")
            dependencies {
                implementation(libs.androidx.core.ktx)
                implementation(libs.androidx.lifecycle.runtime.ktx)
                implementation(libs.androidx.activity.compose)
                implementation(libs.kotlinx.coroutines.android)
                implementation(libs.androidx.compose.ui.tooling.preview)
            }
        }

        iosMain {
            kotlin.srcDirs("src/iosMain/kotlin")
        }
    }
}

android {
    namespace = "app.kotori.japanese"
    compileSdk = 36

    sourceSets {
        getByName("main") {
            manifest.srcFile("src/main/AndroidManifest.xml")
            res.srcDirs("src/main/res")
            assets.srcDirs("src/main/assets")
            // KMP owns the Kotlin/Java sources — clear AGP's default so files
            // aren't registered in both commonMain and androidDebug.
            java.setSrcDirs(emptyList<File>())
            kotlin.setSrcDirs(emptyList<File>())
        }
    }

    defaultConfig {
        applicationId = "app.kotori.japanese"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
    }
}

compose.resources {
    packageOfResClass = "app.kotori.japanese.generated.resources"
    publicResClass = true
}

room {
    schemaDirectory("$projectDir/schemas")
}

// KSP for Room on every target
dependencies {
    add("kspAndroid", libs.androidx.room.compiler)
    add("kspIosX64", libs.androidx.room.compiler)
    add("kspIosArm64", libs.androidx.room.compiler)
    add("kspIosSimulatorArm64", libs.androidx.room.compiler)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
