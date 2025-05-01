import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.plugin.compose")
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
val localVersion: String = properties.getProperty("local.version")
val amuzicVersionCode: Int = properties.getProperty("local.version.code")?.toInt()
    ?: System.getenv("RELEASES")?.toInt() ?: 0

//configurations {
    //ktlint
//}

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
                "appName" to "@string/app_name_debug"
            )
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
                "proguard-rules.pro"
            )

            manifestPlaceholders.putAll(
                arrayOf(
                "appIcon" to "@mipmap/ic_amuzic",
                "appRoundIcon" to "@mipmap/ic_amuzic_round",
                "appName" to "@string/app_name"
            )
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
    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.6"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.compose.material3:material3-android:1.3.1")
    val composeBom = platform("androidx.compose:compose-bom:2025.02.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation("androidx.core:core-ktx:1.15.0")

    implementation("androidx.compose.ui:ui")

    implementation("androidx.compose.foundation:foundation")

    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.ui:ui-tooling")

    implementation("androidx.core:core-splashscreen:1.0.1")

    implementation("androidx.navigation:navigation-compose:2.5.3")

    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.3.1")
    implementation("androidx.activity:activity-compose:1.10.1")

    implementation("androidx.media3:media3-exoplayer:1.3.1")
    implementation("androidx.media3:media3-ui:1.3.1")
    implementation("androidx.media3:media3-common:1.3.1")
    implementation("androidx.media3:media3-session:1.3.1")

    implementation("com.infbyte:amuze:0.2.3")

    implementation("com.google.dagger:hilt-android:2.55")
    kapt("com.google.dagger:hilt-compiler:2.55")

    // ktlint("com.pinterest:ktlint:0.47.1")

    implementation("com.google.android.gms:play-services-ads:24.2.0")

    implementation("androidx.datastore:datastore-preferences:1.1.4")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

kapt {
    correctErrorTypes = true
}

/* tasks.register('ktlintFormat', JavaExec) {
    mainClass = "com.pinterest.ktlint.Main"
    classpath = configurations.ktlint
    args("$rootDir/**/*.kt", "!$rootDir/**/build/**")
    jmArgs += "--add-opens=java.base/java.lang=ALL-UNNAMED"

    if (project.hasProperty("autoCorrect") && project.property("autoCorrect") == "0") {
        logger.quiet("(KTLINT): auto correction is disabled")
    } else {
        logger.quiet("(KTLINT): auto correction is enabled")
        args += "-F"
    }
}*/

/* tasks.register('ktlintCheck', JavaExec) {
    classpath = configurations.ktlint
    mainClass = "com.pinterest.ktlint.Main"
    args.addAll(arrayOf("src/**/*.kt", "**.kts", "!**/build/**"))
}*/

// tasks.preBuild.dependsOn("ktlintCheck")

// tasks.ktlintCheck.dependsOn("ktlintFormat")

