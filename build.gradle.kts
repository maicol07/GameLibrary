val versionCode by extra(2)
val versionName by extra("0.1")

buildscript {
    val compose_version by extra("1.2.0")
}
// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    id("com.android.application") version "8.0.2" apply false
    id("org.jetbrains.kotlin.android") version "1.8.21" apply false
    id("com.google.devtools.ksp") version "1.8.21-1.0.11" apply false
    id("com.google.gms.google-services") version "4.3.15" apply false

    // Hilt
    id("com.google.dagger.hilt.android") version "2.44" apply false
}
