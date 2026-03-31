plugins {
    // AGP 8.9.1 is required for API 36 compatibility
    id("com.android.application") version "8.9.1" apply false
    id("com.android.library") version "8.9.1" apply false

    // Kotlin 2.1.10 is the stable path for Compose and KSP right now
    id("org.jetbrains.kotlin.android") version "2.1.10" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.1.10" apply false
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.10" apply false

    // KSP version must match the first three parts of the Kotlin version
    id("com.google.devtools.ksp") version "2.1.10-1.0.29" apply false
}