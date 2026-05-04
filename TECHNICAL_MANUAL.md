# MedVision AI Technical Manual

## 1. Purpose

MedVision AI is an Android health-assistant app built with Kotlin and Jetpack Compose. It provides:

- Email/password authentication
- AI symptom analysis
- Camera or gallery image analysis for visible disease triage
- Before/after scan comparison
- AI medical chat
- Health history tracking
- Theme switching and modern Compose UI

The app is designed for general health guidance and visual triage. It does not diagnose disease or replace a clinician.

## 2. High-Level Architecture

The app follows an MVVM-style architecture with a single dependency container.

### Runtime flow

1. `MedVisionApplication` initializes Firebase if local Firebase config is present.
2. `AppContainer` creates repositories for auth, settings, history, symptom analysis, scan analysis, and chat.
3. `AppViewModelFactory` injects those repositories into the view models.
4. `MainActivity` reads the dark-mode setting and launches the Compose UI.
5. `MedVisionApp` manages authentication gating and navigation.
6. Screens call view model methods.
7. View models call repositories.
8. Repositories talk to Firebase, DataStore, or Gemini through Retrofit.

### Key layers

- UI layer: Compose screens and reusable components
- Presentation layer: ViewModels with `StateFlow`
- Data layer: Repositories and models
- Network layer: Retrofit Gemini service
- Platform services: CameraX, Firebase Auth, Firestore, DataStore

## 3. Project Setup

### Android / Kotlin baseline

- Android Gradle Plugin: `8.5.2`
- Kotlin: `1.9.24`
- Compile SDK: `34`
- Target SDK: `34`
- Minimum SDK: `26`
- Java compatibility: `17`

### Build flavors

There are no product flavors in this project. Configuration is controlled through:

- `local.properties`
- optional `.env`
- environment variables

These values are exposed to the app with `buildConfigField`.

## 4. Library And Dependency Inventory

The app uses the following major libraries and frameworks.

### UI and app architecture

- `androidx.activity:activity-compose`
- `androidx.compose.ui`
- `androidx.compose.material3`
- `androidx.compose.material:material-icons-extended`
- `androidx.lifecycle:lifecycle-runtime-compose`
- `androidx.lifecycle:lifecycle-viewmodel-compose`
- `androidx.navigation:navigation-compose`
- `androidx.core:core-ktx`

### State and storage

- `androidx.datastore:datastore-preferences`
- Kotlin coroutines: `kotlinx-coroutines-android`
- Kotlin serialization: `kotlinx-serialization-json`

### Networking and AI

- Retrofit `2.11.0`
- OkHttp logging interceptor `4.12.0`
- Retrofit Kotlinx serialization converter
- Google Gemini Generative Language API
- Guava `33.2.1-android`

### Firebase

- Firebase BOM `33.2.0`
- Firebase Auth KTX
- Firebase Firestore KTX

### Camera and media

- CameraX core `1.4.2`
- CameraX camera2 `1.4.2`
- CameraX lifecycle `1.4.2`
- CameraX view `1.4.2`
- Coil Compose `2.7.0`

### Motion / loading UI

- Lottie Compose `6.4.1`

### Testing

- JUnit `4.13.2`
- AndroidX test ext JUnit `1.2.1`
- Espresso `3.6.1`

## 5. Configuration And Secrets

The app reads configuration from Gradle build config fields populated from `local.properties`, `.env`, or environment variables.

### Gemini config

- `GEMINI_API_KEY`
- `GEMINI_MODEL`
- `GEMINI_BASE_URL`

Defaults used in the build file:

- Model: `gemini-2.5-flash`
- Base URL: `https://generativelanguage.googleapis.com/`

### Firebase config

- `FIREBASE_API_KEY`
- `FIREBASE_APP_ID`
- `FIREBASE_PROJECT_ID`
- `FIREBASE_STORAGE_BUCKET`

These values are used to initialize Firebase dynamically at runtime if `google-services.json` is present and the app is not already initialized.

### Firebase setup requirement

The app expects the Firebase project/package name to be:

- `com.medvision.ai`

Email/password authentication must be enabled in Firebase Console for the auth flow to work online.

## 6. Startup And Dependency Injection

### Application startup

`MedVisionApplication` checks whether Firebase is already initialized. If not, and if Firebase config is present, it creates `FirebaseOptions` and calls `FirebaseApp.initializeApp()`.

### Dependency graph

`AppContainer` creates:

- `SettingsRepository`
- `AuthRepository`
- `HistoryRepository`
- `AiRepository`
- `DetectionRepository`
- `MedicalChatRepository`

It also creates a shared Retrofit-based Gemini service via `GeminiServiceFactory`.

### ViewModel creation

`AppViewModelFactory` injects repositories into:

- `AuthViewModel`
- `HomeViewModel`
- `SymptomCheckerViewModel`
- `ScanViewModel`
- `ScanComparisonViewModel`
- `MedicalChatViewModel`
- `HistoryViewModel`
- `SettingsViewModel`

## 7. UI Structure

### Entry point

`MainActivity` is a `ComponentActivity` that:

- collects the dark-mode preference from `SettingsViewModel`
- applies edge-to-edge system bars
- wraps the app in `MedVisionTheme`
- starts `MedVisionApp`

### Navigation

`MedVisionApp` uses a `NavHost` with these routes:

- `home`
- `symptoms`
- `scan`
- `scan_comparison`
- `medical_chat`
- `history`
- `settings`

Bottom navigation is shown only on the main routes:

- Home
- History
- Settings

### Authentication gate

If `AuthRepository.currentUser` is null, the app shows `AuthScreen`. Once a user exists, the main scaffold and navigation appear.

## 8. Feature Logic

### 8.1 Authentication

Files involved:

- `AuthRepository`
- `AuthViewModel`
- `AuthScreen`

Behavior:

- Online mode uses Firebase Auth sign-in and sign-up.
- Sign-up also updates the Firebase display name.
- If Firebase is unavailable, the repository falls back to an offline mock user so the UI can still be exercised locally.
- Logout clears the Firebase session or the mock session.

State handling:

- UI fields are held in `AuthUiState`
- loading and error states are surfaced directly to the screen

### 8.2 Symptom Checker

Files involved:

- `SymptomCheckerScreen`
- `SymptomCheckerViewModel`
- `AiRepository`

Flow:

1. User enters free-form symptoms.
2. View model validates that input is not blank.
3. The view model starts analysis with a timeout of 15 seconds.
4. It retries once for retryable failures.
5. Repository sends a prompt to Gemini requesting JSON only.
6. Parsed response is converted into `AnalysisResult`.
7. The result is stored in history.

Prompt contract:

- Must not diagnose
- Must not prescribe
- Must return JSON with:
  - `possible_conditions`
  - `risk_level`
  - `suggested_actions`

Fallbacks:

- If no Gemini API key is configured, `AiRepository` returns a sample response.
- If network access is unavailable, a `NoInternetException` is raised.
- If the request fails after retries, the view model displays a local fallback guidance response and still saves it to history.

### 8.3 Disease Scan

Files involved:

- `ScanScreen`
- `ScanViewModel`
- `DetectionRepository`
- `CameraPreview`

Flow:

1. The screen requests camera permission.
2. If permission is granted, CameraX preview is bound to the lifecycle.
3. The user can capture a photo or upload a gallery image.
4. Gallery images are copied into cache before analysis.
5. The repository compresses the image to JPEG, converts it to Base64, and sends it to Gemini as inline image data.
6. Gemini returns JSON describing a broad visual finding.
7. The result is displayed and stored in history.

Prompt contract:

- Do not diagnose
- Do not claim certainty
- Return JSON with:
  - `possible_condition`
  - `confidence`
  - `summary`

Important implementation details:

- Images are resized down to a maximum side of 1024 pixels before upload.
- JPEG quality is set to 82.
- Scan analysis has a 45 second timeout in the view model.

Fallbacks:

- With no Gemini API key, the app returns a message explaining that visual analysis is unavailable.
- Invalid API key, quota, and server errors are mapped to human-readable messages.

### 8.4 Scan Comparison

Files involved:

- `ScanComparisonScreen`
- `ScanComparisonViewModel`
- `DetectionRepository`

Flow:

1. User picks an earlier image and a newer image from the gallery.
2. Both are copied into cache.
3. The repository sends both images to Gemini in one request.
4. Gemini is asked to compare visible changes only.
5. The trend is normalized to one of:
  - `Improving`
  - `Worsening`
  - `Unclear`
6. The result is shown and saved to history.

Prompt contract:

- Compare only visible changes
- Return JSON with:
  - `trend`
  - `confidence`
  - `summary`
  - `suggested_action`

The model is explicitly instructed to choose `Unclear` when lighting, focus, angle, body part, or image quality makes comparison unreliable.

### 8.5 AI Health Chat

Files involved:

- `MedicalChatScreen`
- `MedicalChatViewModel`
- `MedicalChatRepository`

Flow:

1. User types a question.
2. The view model appends the message locally.
3. The last 10 messages are flattened into a transcript.
4. Gemini receives a constrained medical-assistant prompt plus the transcript.
5. The assistant answer is appended to the chat UI.

Safety constraints:

- No diagnosis
- No prescription
- No personalized dosage
- Urgent symptoms should trigger emergency-care advice

If the Gemini API key is missing, the repository returns a local explanation telling the user to configure the key.

### 8.6 Health History

Files involved:

- `HistoryScreen`
- `HistoryViewModel`
- `HistoryRepository`

Behavior:

- History is cached in memory using a `MutableStateFlow`.
- If Firebase is available and a user is signed in, history is synced from Firestore at startup.
- New symptom checks, scans, and comparisons are written to both the local cache and Firestore when possible.

Firestore path:

- `users/{uid}/history/{historyId}`

The UI renders each history entry with:

- type
- timestamp
- optional image
- original input text
- result title and details

### 8.7 Settings

Files involved:

- `SettingsScreen`
- `SettingsViewModel`
- `SettingsRepository`

Behavior:

- Dark mode is stored in DataStore preferences.
- The current value is collected as a `Flow<Boolean>`.
- Toggling the switch writes the preference asynchronously.
- Logout calls into `AuthRepository`.

## 9. API Mechanics

### Gemini Retrofit service

The shared API interface is `GeminiService`.

Endpoint used:

- `POST v1beta/models/{model}:generateContent?key={apiKey}`

Request format:

- `contents`: list of parts
- optional `generationConfig`

Supported part types:

- text
- inline image data

Response parsing:

- `GeminiServiceFactory` uses Retrofit + Kotlinx serialization
- `Json { ignoreUnknownKeys = true }`
- The code reads the first non-empty text part from the first candidate
- JSON fences are stripped when needed for symptom analysis

### Error handling

Common failures are converted into user-friendly messages:

- invalid API key
- quota exceeded or rate limited
- server unavailable
- network unavailable
- empty Gemini response

## 10. Data Models

### AI symptom analysis

- `AnalysisResult`
  - `conditions`
  - `riskLevel`
  - `advice`
  - `disclaimer`

- `AiAnalysisResponse`
  - `possible_conditions`
  - `risk_level`
  - `suggested_actions`

### Scan analysis

- `DetectionResult`
  - `possibleCondition`
  - `confidence`
  - `imagePath`
  - `summary`
  - `disclaimer`

- `ScanComparisonResult`
  - `trend`
  - `confidence`
  - `summary`
  - `suggestedAction`
  - `disclaimer`

### History

- `HistoryItem`
  - `id`
  - `timestamp`
  - `type`
  - `inputText`
  - `imagePath`
  - `resultTitle`
  - `resultDetail`

- `HistoryType`
  - `SYMPTOM`
  - `SCAN`
  - `COMPARISON`

### Auth

- `UserProfile`
  - `uid`
  - `name`
  - `email`

### Chat

- `MedicalChatTurn`
  - `text`
  - `isUser`

## 11. UI And Theme System

### Theme

`MedVisionTheme` switches between custom light and dark `MaterialTheme` color schemes.

### Visual system

Reusable components include:

- `GradientBackground`
- `GlassCard`
- `ScreenHeader`
- `PrimaryActionButton`
- `DisclaimerBanner`
- `LoadingCard`

### Motion and loading

- `PrimaryActionButton` uses a subtle infinite alpha pulse
- `LoadingCard` uses a Lottie heartbeat animation from `res/raw/heartbeat_loader.json`

### Typography

Typography is intentionally simple and uses sans-serif based styles for headline, title, and body text.

## 12. Camera And Image Handling

### Camera capture

`CameraPreview` binds CameraX to the lifecycle owner and uses the back camera.

### Capture output

- Captured photos are written to the app cache directory
- File names use timestamps
- `capturePhoto()` returns the local file path to the view model

### Gallery import

`copyGalleryImageToCache()` copies the selected content URI into a cache file so the rest of the pipeline can work with a normal file path.

### Why cache files are used

The AI pipeline expects file paths for bitmap decoding, display, and Base64 conversion. Converting gallery input into a cache file keeps the downstream logic uniform.

## 13. State Management

The app uses `StateFlow` for all feature state.

Typical pattern:

1. Screen collects a `StateFlow` from the view model.
2. User changes update fields in the view model.
3. User action triggers a repository call.
4. Result updates state.
5. Screen recomposes automatically.

This is used consistently across authentication, symptom checking, scan analysis, comparison, chat, history, and settings.

## 14. Build And Run

### Android Studio

1. Open the project.
2. Sync Gradle.
3. Ensure SDK 34 is installed.
4. Add your Gemini and Firebase values to `local.properties` or `.env`.
5. Run the `app` module on a device or emulator.

### Command line

```powershell
.\gradlew.bat :app:assembleDebug
```

## 15. Required Files For A Full Runtime Setup

To build and run the app with online functionality, the following should exist:

- `app/google-services.json`
- Gemini API key in `local.properties` or environment variables
- Firebase project enabled for Email/Password auth and Firestore

## 16. Behavior When Services Are Missing

### Missing Gemini key

- Symptom checker returns a sample response
- Scan analysis returns a message that visual analysis is unavailable
- Scan comparison returns a message explaining how to enable it
- Chat returns a message telling the user to add the key

### Missing Firebase config

- App still starts
- Auth falls back to a local mock user
- History remains local in memory unless Firestore is available

### No internet

- Symptom checker reports no connection
- AI requests fail gracefully with readable messages

## 17. Security And Privacy Notes

- API keys are not hardcoded in source files; they are read from build-time config.
- Health images are copied only to the app cache for processing.
- History is stored per signed-in user in Firestore when online.
- The app displays health disclaimers in the main workflow screens.

## 18. Known Implementation Constraints

- AI responses depend on Gemini output quality and may be affected by prompt compliance.
- Image analysis quality depends on lighting, focus, angle, and image clarity.
- Offline mode is functional but simplified.
- History sync is opportunistic and does not appear to implement conflict resolution.

## 19. How To Extend The App

If you want to add a new feature, the current architecture suggests this pattern:

1. Add a model in `data/model`.
2. Add a repository method for the data source.
3. Expose state and actions in a new or existing view model.
4. Add a Compose screen.
5. Register the view model in `AppViewModelFactory`.
6. Wire the screen into `MedVisionApp` navigation.

## 20. Repository Layout Summary

```text
app/src/main/java/com/medvision/ai
|-- data
|   |-- model
|   `-- repository
|-- network
|-- ui
|   |-- components
|   |-- screens
|   `-- theme
|-- utils
`-- viewmodel
```

## 21. Practical Reading Order For New Developers

If you are trying to understand the codebase quickly, read it in this order:

1. `build.gradle.kts` and `app/build.gradle.kts`
2. `MedVisionApplication.kt`
3. `AppContainer.kt`
4. `AppViewModelFactory.kt`
5. `MedVisionApp.kt`
6. `AiRepository.kt`
7. `DetectionRepository.kt`
8. `MedicalChatRepository.kt`
9. `AuthRepository.kt`
10. `HistoryRepository.kt`
11. View models
12. Compose screens and shared UI components

## 22. Short Summary

MedVision AI is a Compose-first Android app with a clear layered design: the UI sends user intent into view models, the view models orchestrate repositories, and the repositories integrate Firebase, DataStore, CameraX, and Gemini. The main logic lives in the repositories and view models, where the app enforces safe medical prompts, JSON response parsing, retry handling, offline fallbacks, and history persistence.