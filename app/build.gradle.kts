plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.example.emergencysos"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.emergencysos"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
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

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.04.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")

    // Volley
    implementation("com.android.volley:volley:1.2.1")

    // Coil
    implementation("io.coil-kt:coil-compose:2.4.0")

    // Gson
    implementation("com.google.code.gson:gson:2.10.1")

    // Kotlin stdlib
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.1.10")
    implementation(libs.androidx.tools.core)
    testImplementation(libs.junit.junit)

    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    implementation("androidx.compose.material:material-icons-core")
    implementation("androidx.compose.material:material-icons-extended")
    implementation ("com.airbnb.android:lottie-compose:6.3.0")
    implementation ("androidx.navigation:navigation-compose:2.8.3")
    implementation ("com.google.android.gms:play-services-maps:18.2.0")
    implementation ("com.google.android.gms:play-services-location:21.0.1")
    implementation ("com.google.maps.android:maps-compose:2.13.0")
    implementation ("com.google.android.gms:play-services-maps:18.1.0")
    implementation ("com.android.volley:volley:1.2.1")
    implementation ("com.google.code.gson:gson:2.10.1")
    implementation ("androidx.compose.material3:material3:1.1.0")
    implementation ("androidx.compose.material3:material3:1.2.1")
    // ExoPlayer
    implementation ("com.google.android.exoplayer:exoplayer:2.19.0")
    implementation ("com.google.accompanist:accompanist-pager:0.28.0")
    implementation ("com.google.accompanist:accompanist-pager-indicators:0.28.0")
    implementation ("androidx.datastore:datastore-preferences:1.1.1")
    implementation ("com.github.bumptech.glide:compose:1.0.0-alpha.1")
    implementation ("com.github.bumptech.glide:glide:4.15.1")
    implementation ("org.tensorflow:tensorflow-lite:2.14.0")
    implementation ("org.tensorflow:tensorflow-lite-support:0.4.4")
    implementation ("com.google.android.gms:play-services-location:21.0.1")
    implementation ("androidx.camera:camera-core:1.3.0")
    implementation ("androidx.camera:camera-camera2:1.3.0")
    implementation ("androidx.camera:camera-lifecycle:1.3.0")
    implementation ("androidx.camera:camera-view:1.3.0")


}
