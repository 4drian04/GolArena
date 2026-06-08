plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.adriangg.golarena"
    compileSdk = 35

    defaultConfig {
        buildConfigField("String", "CSV_VERSION", "\"1\"")
        applicationId = "com.adriangg.golarena"
        minSdk = 30
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation(libs.material.v1110)
    implementation(libs.glide)
    implementation(libs.volley)
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)
    implementation(libs.work.runtime)
    implementation(libs.tasks.genai)
    implementation(libs.opencsv.opencsv)
    implementation(libs.translate)
}