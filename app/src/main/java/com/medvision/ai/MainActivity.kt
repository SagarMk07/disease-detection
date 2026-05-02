package com.medvision.ai

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.medvision.ai.ui.MedVisionApp
import com.medvision.ai.ui.theme.MedVisionTheme
import com.medvision.ai.viewmodel.AppViewModelFactory
import com.medvision.ai.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {
    private val settingsViewModel: SettingsViewModel by viewModels {
        AppViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val darkMode by settingsViewModel.darkMode.collectAsState()
            MedVisionTheme(darkTheme = darkMode) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MedVisionApp(application = application)
                }
            }
        }
    }
}
