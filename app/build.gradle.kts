import com.android.build.gradle.internal.tasks.factory.dependsOn
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.gradle.ktlint)
    alias(libs.plugins.google.devtools.ksp)
}

val properties = Properties()
val propertiesFile: File = rootProject.file("local.properties")
if (propertiesFile.exists()) {
    properties.load(propertiesFile.inputStream())
}

val testBannerAdUnitId: String? = properties.getProperty("test.banner.ad.unit.id")
val bannerAdUnitId: String? = properties.getProperty("banner.ad.unit.id")
val banner1AdUnitId: String? = properties.getProperty("banner.1.ad.unit.id")
val admobAppId: String? = properties.getProperty("admob.app.id")
val amuzicKeyAlias: String? = properties.getProperty("key.alias")
val signingKeyStorePass: String? = properties.getProperty("key.store.pass")
val keyPass: String? = properties.getProperty("key.pass")
val localVersion: String? = properties.getProperty("local.version")
val amuzicVersionCode: Int =
    properties.getProperty("local.version.code")?.toInt()
        ?: System.getenv("RELEASES")?.toInt() ?: 0

android {
    compileSdk = 35

    namespace = "com.infbyte.amuzic"

    defaultConfig {
        applicationId = "com.infbyte.amuzic"
        minSdk = 24
        targetSdk = 35
        versionCode = amuzicVersionCode + 1
        versionName = System.getenv("VERSION_NAME") ?: localVersion

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file("amuzic_release_keystore.jks")
            storePassword = System.getenv("SIGNING_KEYSTORE_PASSWORD") ?: signingKeyStorePass
            keyAlias = System.getenv("KEY_ALIAS") ?: amuzicKeyAlias
            keyPassword = System.getenv("KEY_PASSWORD") ?: keyPass
            enableV1Signing = true
            enableV2Signing = true
        }
    }

    buildTypes {
        debug {
            resValue("string", "banner_ad_unit_id", System.getenv("TEST_BANNER_AD_UNIT_ID") ?: "$testBannerAdUnitId")
            resValue("string", "banner_1_ad_unit_id", System.getenv("TEST_BANNER_AD_UNIT_ID") ?: "$testBannerAdUnitId")
            resValue("string", "admob_app_id", System.getenv("ADMOB_APP_ID") ?: "$admobAppId")

            manifestPlaceholders.putAll(
                arrayOf(
                    "appIcon" to "@mipmap/ic_amuzic_debug",
                    "appRoundIcon" to "@mipmap/ic_amuzic_debug_round",
                    "appName" to "@string/app_name_debug",
                ),
            )
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true

            ndk {
                debugSymbolLevel = "SYMBOL_TABLE"
            }

            resValue("string", "admob_app_id", System.getenv("ADMOB_APP_ID") ?: "$admobAppId")
            resValue("string", "banner_ad_unit_id", System.getenv("BANNER_AD_UNIT_ID") ?: "$bannerAdUnitId")
            resValue("string", "banner_1_ad_unit_id", System.getenv("BANNER_1_AD_UNIT_ID") ?: "$banner1AdUnitId")

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )

            manifestPlaceholders.putAll(
                arrayOf(
                    "appIcon" to "@mipmap/ic_amuzic",
                    "appRoundIcon" to "@mipmap/ic_amuzic_round",
                    "appName" to "@string/app_name",
                ),
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(libs.androidx.material3)
    val composeBom = platform(libs.androidx.compose.bom)
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation(libs.androidx.core.ktx)

    implementation(libs.androidx.compose.ui)

    implementation(libs.androidx.compose.foundation)

    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.ui.tooling)

    implementation(libs.androidx.core.splashscreen)

    implementation(libs.androidx.compose.navigation)

    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.compose.activity)

    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.media3.common)
    implementation(libs.androidx.media3.session)

    implementation(libs.infbyte.amuze)

    implementation(libs.google.dagger.hilt)
    ksp(libs.google.dagger.hilt.compiler)

    implementation(libs.google.mobile.ads)

    implementation(libs.androidx.datastore)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.junit.ext)
    androidTestImplementation(libs.androidx.test.espresso.core)

    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

tasks.preBuild.dependsOn("ktlintCheck")

tasks.ktlintCheck.dependsOn("ktlintFormat")
