plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    id("kotlin-kapt") // For Room annotation processing
    id("com.google.gms.google-services") version "4.4.2" apply false
}

android {
    namespace = "com.catradar.proyectofinal"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.catradar.proyectofinal"
        minSdk = 24
        targetSdk = 35
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
}

dependencies {
    // Navigation Component
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
    // Material Design Components
    implementation("com.google.android.material:material:1.11.0")
    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation(libs.play.services.location)
    kapt("androidx.room:room-compiler:2.6.1")
    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:33.15.0"))
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore-ktx:25.1.4")
    implementation("com.google.firebase:firebase-storage-ktx:21.0.2")
    // Viewmodel and LiveData
    implementation("androix.lifecycle:lifecycle-viewmodel-ktx:2.7.1")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.1")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}