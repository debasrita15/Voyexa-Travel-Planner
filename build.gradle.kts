// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "9.0.0" apply false

    // Required for Compose with Kotlin 2.0+
    id("org.jetbrains.kotlin.plugin.compose") version "2.3.0" apply false

    // KSP for Kotlin 2.3+
    id("com.google.devtools.ksp") version "2.3.4" apply false

    // Serialization plugin (since you use kotlinx serialization)
    id("org.jetbrains.kotlin.plugin.serialization") version "2.3.0" apply false
    id("com.google.gms.google-services") version "4.4.4" apply false
}