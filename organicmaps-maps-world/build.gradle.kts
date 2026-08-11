plugins {
    alias(libs.plugins.android.library)
    `maven-publish`
}

val organicMapsAndroid = rootProject.file("android")

android {
    namespace = "app.organicmaps.sdk.maps.world"
    compileSdk = 36

    defaultConfig {
        minSdk = 26
    }

    sourceSets {
        getByName("main") {
            manifest.srcFile("src/main/AndroidManifest.xml")
            assets.srcDir(organicMapsAndroid.resolve("sdk/maps/world/src/main/assets"))
        }
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                artifactId = "organicmaps-maps-world"
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
