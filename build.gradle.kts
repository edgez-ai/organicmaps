plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
}

allprojects {
    group = "ai.edgez.organicmaps"
    version = providers.gradleProperty("VERSION_NAME").getOrElse("0.1.0-SNAPSHOT")
}

// Android Studio's Kotlin DSL importer still requests this legacy model task.
// AGP 9 built-in Kotlin does not create it for source-less Android modules, so
// provide a no-op task only when no plugin has supplied the real one.
gradle.projectsEvaluated {
    allprojects {
        if (tasks.findByName("prepareKotlinBuildScriptModel") == null) {
            tasks.register("prepareKotlinBuildScriptModel") {
                group = "ide"
                description = "Compatibility task for Android Studio Kotlin DSL import"
            }
        }
    }
}
