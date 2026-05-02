# MedVision AI

MedVision AI is a Kotlin + Jetpack Compose Android application with MVVM architecture, premium glassmorphism-inspired UI, Firebase-ready authentication/history storage, OpenAI-powered symptom analysis with safe fallback responses, and CameraX + ML Kit visual scanning.

## Features

- Email/password login and signup with Firebase Auth fallback-safe offline mode
- Premium home screen with glass cards and gradient visuals
- AI symptom checker with OpenAI Responses API integration
- CameraX capture flow with ML Kit label-based condition estimation
- Firestore-backed health history timeline
- Settings with dark mode toggle and logout
- Required in-app disclaimer on health insight surfaces

## Project Structure

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

## Configuration

### 1. Firebase

1. Create a Firebase Android app with package name `com.medvision.ai`.
2. Enable Email/Password authentication.
3. Create a Firestore database.
4. Add these values to `local.properties` or your environment:

```properties
FIREBASE_API_KEY=your_firebase_api_key
FIREBASE_APP_ID=your_firebase_app_id
FIREBASE_PROJECT_ID=your_project_id
FIREBASE_STORAGE_BUCKET=your_storage_bucket
```

If Firebase is not configured, the app still opens and uses offline-safe auth/history fallbacks for demo purposes.

### 2. OpenAI API

Add these values to your local `local.properties` file or environment variables:

```properties
OPENAI_API_KEY=your_api_key_here
OPENAI_MODEL=gpt-5.2-mini
OPENAI_BASE_URL=https://api.openai.com/
```

If no API key is present, the symptom checker returns sample AI responses instead of making a network call.

## Build And Run

1. Open the project in Android Studio Hedgehog or newer.
2. Let Gradle sync and install missing SDK components for API 34.
3. Connect an Android device or start an emulator running Android 8.0+.
4. Run the `app` configuration.

## Notes

- Camera scanning uses ML Kit image labeling as a lightweight visual analysis placeholder.
- For higher accuracy medical imaging, replace the detection repository with a custom TensorFlow Lite model.
- Google Sign-In is left optional and can be added on top of the current auth flow if needed.
- This app provides general health insights and is not a substitute for professional medical advice.
