plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "vn.simpletv.launcher"
    compileSdk = 34

    defaultConfig {
        applicationId = "vn.simpletv.launcher"
        minSdk = 21          // Android 5.0, first Android TV version
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"
    }

    // Two builds of the same launcher:
    //  offline = no INTERNET permission at all, no weather (default, recommended)
    //  weather = adds INTERNET permission + optional weather widget (off by default in settings)
    flavorDimensions += "network"
    productFlavors {
        create("offline") { dimension = "network" }
        create("weather") {
            dimension = "network"
            versionNameSuffix = "-weather"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            // Signed with the debug key so the release APK installs for sideloading.
            // Replace with your own keystore if you ever distribute it.
            signingConfig = signingConfigs.getByName("debug")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

// Deliberately NO dependencies: only the Android framework is used.
// Smaller APK, less RAM, nothing that can phone home.
dependencies {
}
