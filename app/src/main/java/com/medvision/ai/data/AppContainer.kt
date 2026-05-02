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
import com.medvision.ai.data.repository.SettingsRepository
import com.medvision.ai.network.OpenAiServiceFactory

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val firebaseEnabled = FirebaseApp.getApps(appContext).isNotEmpty()

    private val auth: FirebaseAuth? = if (firebaseEnabled) FirebaseAuth.getInstance() else null
    private val firestore: FirebaseFirestore? = if (firebaseEnabled) FirebaseFirestore.getInstance() else null

    val settingsRepository = SettingsRepository(appContext)
    val authRepository = AuthRepository(auth)
    val historyRepository = HistoryRepository(firestore, authRepository)
    val aiRepository = AiRepository(
        service = OpenAiServiceFactory.create(
            baseUrl = BuildConfig.OPENAI_BASE_URL,
            apiKey = BuildConfig.OPENAI_API_KEY
        ),
        model = BuildConfig.OPENAI_MODEL,
        apiKey = BuildConfig.OPENAI_API_KEY
    )
    val detectionRepository = DetectionRepository(appContext)
}
