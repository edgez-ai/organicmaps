plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "ai.edgez.organicmaps.example"
    compileSdk = 36

    defaultConfig {
        applicationId = "ai.edgez.organicmaps.example"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        buildConfig = true
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    coreLibraryDesugaring(libs.android.tools.desugaring)
    implementation(project(":organicmaps-sdk"))
    implementation(project(":organicmaps-maps-world"))
    implementation(libs.androidx.fragment)
}
