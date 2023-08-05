val versionCode by extra(3)
val versionName by extra("0.1")

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.kotlinAndroid) apply false
    alias(libs.plugins.hiddenSecrets) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
}