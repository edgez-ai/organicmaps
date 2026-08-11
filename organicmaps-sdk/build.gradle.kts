plugins {
    alias(libs.plugins.android.library)
    `maven-publish`
}

val organicMapsRoot = rootProject.projectDir
val organicMapsAndroid = organicMapsRoot.resolve("android")
val organicMapsAbis = providers.gradleProperty("edgez.organicmaps.abis")
    .map { value -> value.split(',').map(String::trim).filter(String::isNotEmpty) }
    .getOrElse(listOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64"))

android {
    namespace = "app.organicmaps.sdk"
    compileSdk = 36
    ndkVersion = "29.0.14206865"
    enableKotlin = true

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        minSdk = 26

        externalNativeBuild {
            cmake {
                cppFlags += listOf("-fexceptions", "-frtti")
                cFlags += listOf("-fno-function-sections", "-fno-data-sections", "-Wno-extern-c-compat")
                arguments += listOf(
                    "-DANDROID_TOOLCHAIN=clang",
                    "-DANDROID_STL=c++_static",
                    "-DBUILD_TESTING=OFF",
                    "-DSKIP_TOOLS=ON",
                    "-DUSE_PCH=OFF",
                    "-DNJOBS=",
                    "-DENABLE_VULKAN_DIAGNOSTICS=OFF",
                    "-DENABLE_TRACE=OFF",
                )
                targets += listOf("organicmaps")
            }
        }

        ndk {
            abiFilters += organicMapsAbis
        }
    }

    sourceSets {
        getByName("main") {
            manifest.srcFile(organicMapsAndroid.resolve("sdk/src/main/AndroidManifest.xml"))
            java.srcDir(organicMapsAndroid.resolve("sdk/src/main/java"))
            kotlin.srcDir(organicMapsAndroid.resolve("sdk/src/main/java"))
            res.srcDir(organicMapsAndroid.resolve("sdk/src/main/res"))
            assets.srcDir(organicMapsAndroid.resolve("sdk/src/main/assets"))
        }
    }

    externalNativeBuild {
        cmake {
            version = "3.22.1+"
            buildStagingDirectory = file("nativeOutputs")
            path = organicMapsRoot.resolve("CMakeLists.txt")
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
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
    api(project(":organicmaps-location-core"))
    api(libs.androidx.lifecycle.process)
    api(libs.androidx.recyclerview)
    api(libs.androidx.fragment)

    coreLibraryDesugaring(libs.android.tools.desugaring)

    implementation(libs.androidx.annotation)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.media)
    implementation(libs.okhttp)
}

afterEvaluate {
    publishing {
        publications {
            create<MavenPublication>("release") {
                from(components["release"])
                artifactId = "organicmaps-sdk"
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
