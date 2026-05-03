package com.medvision.ai.data

import android.content.Context
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.medvision.ai.BuildConfig
import com.medvision.ai.data.repository.AiRepository
import com.medvision.ai.data.repository.AuthRepository
import com.medvision.ai.data.repository.DetectionRepository
import com.medvision.ai.data.repository.HistoryRepository
import com.medvision.ai.data.repository.MedicalChatRepository
import com.medvision.ai.data.repository.SettingsRepository
import com.medvision.ai.network.GeminiServiceFactory

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val firebaseEnabled = FirebaseApp.getApps(appContext).isNotEmpty()

    private val auth: FirebaseAuth? = if (firebaseEnabled) FirebaseAuth.getInstance() else null
    private val firestore: FirebaseFirestore? = if (firebaseEnabled) FirebaseFirestore.getInstance() else null

    val settingsRepository = SettingsRepository(appContext)
    val authRepository = AuthRepository(auth)
    val historyRepository = HistoryRepository(firestore, authRepository)
    val aiRepository = AiRepository(
        context = appContext,
        service = GeminiServiceFactory.create(
            baseUrl = BuildConfig.GEMINI_BASE_URL
        ),
        model = BuildConfig.GEMINI_MODEL,
        apiKey = BuildConfig.GEMINI_API_KEY
    )
    val detectionRepository = DetectionRepository(
        context = appContext,
        service = GeminiServiceFactory.create(
            baseUrl = BuildConfig.GEMINI_BASE_URL
        ),
        model = BuildConfig.GEMINI_MODEL,
        apiKey = BuildConfig.GEMINI_API_KEY
    )
    val medicalChatRepository = MedicalChatRepository(
        service = GeminiServiceFactory.create(
            baseUrl = BuildConfig.GEMINI_BASE_URL
        ),
        model = BuildConfig.GEMINI_MODEL,
        apiKey = BuildConfig.GEMINI_API_KEY
    )
}
