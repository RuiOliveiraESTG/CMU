plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("com.google.devtools.ksp") version "2.0.21-1.0.25"
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.plugin.compose")
}




android {
        namespace = "com.example.cmu"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.cmu"
        minSdk = 24
        targetSdk = 36
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.room.common.jvm)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    //firebase
    implementation(platform("com.google.firebase:firebase-bom:34.1.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation ("com.google.firebase:firebase-auth-ktx:21.2.0")
    implementation("com.google.firebase:firebase-firestore-ktx:24.4.4")

    //room
    implementation("androidx.room:room-runtime:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")


    //retrofit
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")

    //maps
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")

    //camaraX
    implementation("androidx.camera:camera-core:1.4.0")
    implementation("androidx.camera:camera-camera2:1.4.0")
    implementation("androidx.camera:camera-lifecycle:1.4.0")
    implementation("androidx.camera:camera-view:1.4.0")

    //jetpack compose
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.compose.ui:ui:1.7.4")
    implementation("androidx.compose.material3:material3:1.3.0")
    implementation("androidx.navigation:navigation-compose:2.8.3")

    // Compose extra
    implementation("androidx.compose.foundation:foundation:1.7.4")
    implementation("androidx.compose.ui:ui-tooling-preview:1.7.4")
    debugImplementation("androidx.compose.ui:ui-tooling:1.7.4")

    // Google Places API
    implementation("com.google.android.libraries.places:places:3.5.0")

    //Google Maps Compose
    implementation("com.google.maps.android:maps-compose:2.11.3")
    implementation("com.google.android.gms:play-services-maps:18.1.0")

    // Kotlin coroutines + lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1")


    implementation("androidx.work:work-runtime-ktx:2.9.0")
    
}