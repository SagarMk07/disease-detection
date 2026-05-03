import java.util.Properties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization")
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

fun propertyOrEnv(name: String): String {
    return localProperties.getProperty(name)
        ?: System.getenv(name)
        ?: ""
}

android {
    namespace = "com.medvision.ai"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.medvision.ai"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField("String", "OPENAI_API_KEY", "\"${propertyOrEnv("OPENAI_API_KEY")}\"")
        buildConfigField("String", "OPENAI_MODEL", "\"${propertyOrEnv("OPENAI_MODEL").ifBlank { "gpt-5-mini" }}\"")
        buildConfigField("String", "OPENAI_BASE_URL", "\"${propertyOrEnv("OPENAI_BASE_URL").ifBlank { "https://api.openai.com/" }}\"")
        buildConfigField("String", "GEMINI_API_KEY", "\"${propertyOrEnv("GEMINI_API_KEY")}\"")
        buildConfigField("String", "GEMINI_MODEL", "\"${propertyOrEnv("GEMINI_MODEL").ifBlank { "gemini-2.5-flash" }}\"")
        buildConfigField("String", "GEMINI_BASE_URL", "\"${propertyOrEnv("GEMINI_BASE_URL").ifBlank { "https://generativelanguage.googleapis.com/" }}\"")
        buildConfigField("String", "FIREBASE_API_KEY", "\"${propertyOrEnv("FIREBASE_API_KEY")}\"")
        buildConfigField("String", "FIREBASE_APP_ID", "\"${propertyOrEnv("FIREBASE_APP_ID")}\"")
        buildConfigField("String", "FIREBASE_PROJECT_ID", "\"${propertyOrEnv("FIREBASE_PROJECT_ID")}\"")
        buildConfigField("String", "FIREBASE_STORAGE_BUCKET", "\"${propertyOrEnv("FIREBASE_STORAGE_BUCKET")}\"")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.06.00")
    val firebaseBom = platform("com.google.firebase:firebase-bom:33.2.0")

    implementation(composeBom)
    androidTestImplementation(composeBom)
    implementation(firebaseBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Added to resolve AAPT error: Theme.Material3.DayNight.NoActionBar not found
    implementation("com.google.android.material:material:1.12.0")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")

    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")
    implementation("com.google.guava:guava:33.2.1-android")

    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("com.airbnb.android:lottie-compose:6.4.1")

    implementation("androidx.camera:camera-core:1.4.2")
    implementation("androidx.camera:camera-camera2:1.4.2")
    implementation("androidx.camera:camera-lifecycle:1.4.2")
    implementation("androidx.camera:camera-view:1.4.2")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}
