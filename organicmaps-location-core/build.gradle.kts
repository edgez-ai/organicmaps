plugins {
    alias(libs.plugins.android.library)
    `maven-publish`
}

val organicMapsAndroid = rootProject.file("android")

android {
    namespace = "app.organicmaps.sdk.location"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
    }

    sourceSets {
        getByName("main") {
            manifest.srcFile("src/main/AndroidManifest.xml")
            java.srcDir(organicMapsAndroid.resolve("sdk/location/core/src/main/java"))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

dependencies {
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.core.ktx)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                artifactId = "organicmaps-location-core"
            }
        }
        repositories {
            maven {
                name = "build"
                url = uri(rootProject.layout.buildDirectory.dir("maven-repository"))
            }
        }
    }
}
