plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)

    id("com.google.devtools.ksp")
    kotlin("plugin.serialization") version "2.1.20"
    alias(libs.plugins.google.gms.google.services)
}

android {
    namespace = "com.jp.foodyvilla_backoffice"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.jp.foodyvilla_backoffice"
        minSdk = 28
        targetSdk = 36
        versionCode = 6
        versionName = "6.0"

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
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.firebase.messaging)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)


    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.0")



    val nav_version = "2.8.9"

    implementation("androidx.navigation:navigation-compose:$nav_version")


    // Koin Android features
    implementation(libs.koin.android.v350)
    implementation(libs.koin.androidx.navigation)
    implementation(libs.koin.androidx.compose)

    //    coil (image loading library)

    implementation("io.coil-kt:coil-compose:2.4.0")


    implementation("androidx.webkit:webkit:1.12.0")
    // gms for location
    implementation ("com.google.android.gms:play-services-location:21.0.1")


    // extended icons for menu items
    implementation("androidx.compose.material:material-icons-extended:1.7.4")


    val supabaseVersion = "3.5.0"

    implementation("io.github.jan-tennert.supabase:postgrest-kt:$supabaseVersion")
    implementation("io.github.jan-tennert.supabase:auth-kt:$supabaseVersion")
    implementation("io.github.jan-tennert.supabase:storage-kt:$supabaseVersion")
    implementation("io.github.jan-tennert.supabase:functions-kt:${supabaseVersion}")
    implementation("io.github.jan-tennert.supabase:realtime-kt:${supabaseVersion}") // or latest

    // Required
    implementation("io.ktor:ktor-client-okhttp:3.4.2")
    implementation("io.ktor:ktor-client-content-negotiation:3.4.2")

    // sign in with google
    implementation("com.google.android.gms:play-services-auth:20.7.0")

    // lottie
    implementation("com.airbnb.android:lottie-compose:6.7.1")

    //location
    implementation("com.google.android.gms:play-services-location:21.0.1")
    // razorpay
    implementation("com.razorpay:checkout:1.6.33")
}