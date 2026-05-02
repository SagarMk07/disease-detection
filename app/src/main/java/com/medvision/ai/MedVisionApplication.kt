package com.medvision.ai

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.medvision.ai.BuildConfig
import com.medvision.ai.data.AppContainer

class MedVisionApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        val hasFirebaseConfig = BuildConfig.FIREBASE_API_KEY.isNotBlank() &&
            BuildConfig.FIREBASE_APP_ID.isNotBlank() &&
            BuildConfig.FIREBASE_PROJECT_ID.isNotBlank()
        if (FirebaseApp.getApps(this).isEmpty() && hasFirebaseConfig) {
            val options = FirebaseOptions.Builder()
                .setApiKey(BuildConfig.FIREBASE_API_KEY)
                .setApplicationId(BuildConfig.FIREBASE_APP_ID)
                .setProjectId(BuildConfig.FIREBASE_PROJECT_ID)
                .apply {
                    if (BuildConfig.FIREBASE_STORAGE_BUCKET.isNotBlank()) {
                        setStorageBucket(BuildConfig.FIREBASE_STORAGE_BUCKET)
                    }
                }
                .build()
            runCatching { FirebaseApp.initializeApp(this, options) }
        }
        container = AppContainer(this)
    }
}
