package com.medvision.ai.viewmodel

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.medvision.ai.MedVisionApplication

class AppViewModelFactory(
    application: Application
) : ViewModelProvider.Factory {
    private val container = (application as MedVisionApplication).container

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(AuthViewModel::class.java) ->
                AuthViewModel(container.authRepository) as T
            modelClass.isAssignableFrom(HomeViewModel::class.java) ->
                HomeViewModel(container.authRepository) as T
            modelClass.isAssignableFrom(SymptomCheckerViewModel::class.java) ->
                SymptomCheckerViewModel(container.aiRepository, container.historyRepository) as T
            modelClass.isAssignableFrom(HistoryViewModel::class.java) ->
                HistoryViewModel(container.historyRepository) as T
            modelClass.isAssignableFrom(ScanViewModel::class.java) ->
                ScanViewModel(container.detectionRepository, container.historyRepository) as T
            modelClass.isAssignableFrom(SettingsViewModel::class.java) ->
                SettingsViewModel(container.settingsRepository, container.authRepository) as T
            else -> error("Unknown ViewModel: ${modelClass.simpleName}")
        }
    }
}
