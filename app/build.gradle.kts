import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    id("kotlin-parcelize")
}

android {
    namespace = "net.hearnsoft.tcm.compose"
    compileSdk = 36

    defaultConfig {
        applicationId = "net.hearnsoft.tcm.compose"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures {
        buildConfig = true
    }

    packaging {
        resources {
            excludes += setOf(
                "**/sf_pro.ttf",
            )
        }
    }

    buildTypes {
        configureEach {
            val serverPropsFile = rootProject.file("server.properties")
            if (serverPropsFile.exists()) {
                val serverProps = Properties()
                serverProps.load(FileInputStream(serverPropsFile))

                val apiBaseUrl = serverProps.getProperty("API_BASE_URL")
                if (apiBaseUrl.isNullOrBlank()) {
                    throw GradleException("API_BASE_URL is not set in server.properties")
                }

                buildConfigField("String", "API_BASE_URL", "\"$apiBaseUrl\"")

                // 生成网络安全配置文件
                generateNetworkSecurityConfig(apiBaseUrl)
            } else {
                throw GradleException(
                    "Missing server.properties file. " +
                            "Please create one based on server.properties.example in the root directory."
                )
            }
        }

        release {
            multiDexEnabled = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            dexOptions {
                preDexLibraries = true
                dexInProcess = true
            }
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


// 生成网络安全配置文件的workaround函数
fun generateNetworkSecurityConfig(apiBaseUrl: String) {
    val xmlDir = File(projectDir, "src/main/res/xml")
    if (!xmlDir.exists()) {
        xmlDir.mkdirs()
    }

    val configFile = File(xmlDir, "network_security_config.xml")

    if (apiBaseUrl.startsWith("http://")) {
        // HTTP - 需要明文传输配置
        val domain = apiBaseUrl
            .replace("http://", "")
            .replace(Regex(":\\d+.*$"), "") // 移除端口号和路径
            .split("/")[0]

        val xmlContent = """<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">$domain</domain>
    </domain-config>
</network-security-config>"""

        configFile.writeText(xmlContent)
        println("Generated HTTP network security config for domain: $domain")
    } else {
        // HTTPS - 使用默认安全设置
        val xmlContent = """<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <!-- HTTPS connections use default security settings -->
</network-security-config>"""

        configFile.writeText(xmlContent)
        println("Generated HTTPS network security config (default settings)")
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

    // Palettes
    implementation(libs.palette)
    implementation(libs.palette.ktx)

    // Accompanist Lyrics
    implementation(libs.lyrics.ui)
    implementation(libs.lyrics.core)

    // AndroidX DataStore Preferences
    implementation(libs.androidx.datastore.preferences)

    // LazyColumn Scrollbar
    implementation(libs.lazycolumnscrollbar)

    // RenderScript Toolkit
    implementation(libs.renderscrip.toolkit)

    // Gson
    implementation(libs.gson)

    // Retrofit
    implementation(libs.retrofit)

    // thcdb api sdk
    implementation(project(":thcdb-api"))

    // Crop kit
    implementation(libs.crop.kit)

    // Compose Rating bar
    implementation(libs.compose.ratingbar)

    // XXPermissions
    // 设备兼容框架：https://github.com/getActivity/DeviceCompat
    implementation(libs.devicecompat)
    // 权限请求框架：https://github.com/getActivity/XXPermissions
    implementation(libs.xxpermissions)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}