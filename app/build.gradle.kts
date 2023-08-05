@file:Suppress("UnstableApiUsage")

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.gms)
    kotlin("kapt")
    alias(libs.plugins.hiddenSecrets)
}

android {
    namespace = "it.unibo.gamelibrary"
    compileSdk = 34

    defaultConfig {
        applicationId = "it.unibo.gamelibrary"
        minSdk = 26
        //noinspection EditedTargetSdkVersion (AGP 8.1 required)
        targetSdk = 34
        versionCode = rootProject.extra["versionCode"] as Int
        versionName = rootProject.extra["versionName"] as String

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        getByName("release") {
            isShrinkResources = true
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    // Enable NDK build
    externalNativeBuild {
        cmake {
            path("src/main/cpp/CMakeLists.txt")
        }
    }
}

dependencies {
    // Android
    implementation(libs.core.ktx)
    implementation(libs.lifecycle.runtime.ktx)
    implementation(libs.activity.compose)
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.graphics)
    implementation(libs.ui.tooling.preview)
    implementation(libs.material3)

    // Project
    implementation(libs.accompanist.permissions)
    implementation(libs.appcompat)
    implementation(libs.biometric)
    implementation(libs.compose.destinations.core)
    ksp(libs.compose.destinations.ksp)
    implementation(libs.compose.ratingbar)
    implementation(libs.compose.settings.m3)
    implementation(libs.compose.settings.datastore)
    implementation(libs.compose.shimmer)
    implementation(libs.coroutines.core)
    implementation(libs.coroutines.android)
    implementation(libs.customactivityoncrash)
    implementation(libs.datastore)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth.ktx)
    implementation(libs.gms.play.services.auth)
    implementation(libs.gms.play.services.location)
    implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.igdb.api.jvm)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.landscapist.animation)
    implementation(libs.landscapist.glide)
    implementation(libs.material.icons.extended)
    ksp(libs.room.compiler)
    implementation(libs.room.ktx)
    implementation(libs.room.runtime)
    implementation(libs.security.crypto)


    // Test
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
//    implementation("androidx.annotation:annotation:1.6.0")
//    implementation("com.github.TutorialsAndroid:crashx:v6.0.19")
}

// Allow references to generated code
kapt {
    correctErrorTypes = true
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}