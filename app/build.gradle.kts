plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "net.hearnsoft.tcm.compose"
    compileSdk = 35

    defaultConfig {
        applicationId = "net.hearnsoft.tcm.compose"
        minSdk = 28
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Salt UI
    implementation(libs.salt.ui)
    implementation(libs.salt.core)
    // Navigation
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.compose.android)
    // Animation
    implementation(libs.androidx.animation.graphics)
    implementation(libs.androidx.animation.graphics.android)
    // AndroidX Media 3
    implementation(libs.androidx.media3.common.ktx)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.exoplayer.hls)
    implementation(libs.androidx.media3.exoplayer.rtsp)
    implementation(libs.androidx.media3.exoplayer.smoothstreaming)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.ui)
    // Arrow
    implementation(libs.arrow.core)
    implementation(libs.arrow.fx.coroutines)
    // Coil
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)
    // Squiggly Slider
    implementation(libs.squigglyslider)
    // Pinyin4j
    implementation(libs.pinyin4j)
    // Room database
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)

    // Room annotation processor
    annotationProcessor(libs.androidx.room.compiler)
    // kapt room annotation processor
    ksp(libs.androidx.room.compiler)
    // javax inject
    implementation(libs.javax.inject)

    // Dagger - Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    // Hilt Navigation Compose
    implementation (libs.androidx.hilt.navigation.compose)

    // Material Color Utilities
    implementation(project(":material-color-utilities"))

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}