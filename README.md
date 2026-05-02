# MedVision AI

MedVision AI is a modern Android health assistant built with Kotlin and Jetpack Compose. It combines Firebase-ready authentication, AI-assisted symptom analysis, camera-based image scanning, and a premium glassmorphism-inspired UI.

## Overview

The app is designed around MVVM and clean separation of concerns:

- `data/` contains models and repositories
- `network/` handles OpenAI API communication
- `ui/` contains Compose screens, theme, and shared components
- `viewmodel/` manages screen state with `StateFlow`
- `utils/` contains small helper functions

## Features

- Email/password login and signup
- Session-aware authentication flow
- Premium home screen with animated glass cards
- AI symptom checker with OpenAI integration
- Sample AI fallback when no API key is configured
- CameraX capture flow for disease image scanning
- ML Kit-based placeholder visual detection
- Firestore-backed health history timeline
- Dark mode toggle
- Logout and about section
- Medical disclaimer displayed inside the app

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- MVVM
- Firebase Auth
- Firebase Firestore
- Retrofit
- Kotlinx Serialization
- Coil
- CameraX
- ML Kit
- Lottie Compose

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

### Firebase

Create a Firebase project for package name `com.medvision.ai`, enable Email/Password authentication, and create a Firestore database.

Add the following values to `local.properties` or your environment:

```properties
FIREBASE_API_KEY=your_firebase_api_key
FIREBASE_APP_ID=your_firebase_app_id
FIREBASE_PROJECT_ID=your_project_id
FIREBASE_STORAGE_BUCKET=your_storage_bucket
```

If Firebase is not configured, the app falls back to local demo-friendly behavior for auth and history.

### OpenAI

Add the following values to `local.properties` or your environment:

```properties
OPENAI_API_KEY=your_api_key_here
OPENAI_MODEL=gpt-5.2-mini
OPENAI_BASE_URL=https://api.openai.com/
```

If no API key is provided, the symptom checker returns a sample response instead of calling the API.

## Build And Run

1. Open the project in Android Studio.
2. Let Gradle sync dependencies.
3. Make sure Android SDK 34 is installed.
4. Connect a device or start an emulator.
5. Run the `app` module.

## Main Screens

- Authentication: login and signup flow
- Home: quick access to symptom analysis, scan, and history
- Symptom Checker: AI-generated possible conditions, risk level, and advice
- Scan Disease: capture an image and run on-device visual analysis
- Health History: view previous symptom checks and scans
- Settings: dark mode, logout, and app info

## Important Notes

- The symptom checker does not provide a diagnosis.
- The current image analysis uses ML Kit as a lightweight placeholder.
- For production-grade disease detection, replace the detection pipeline with a trained TensorFlow Lite model.
- Google Sign-In is not implemented yet, but the architecture is ready to extend.

## Disclaimer

This app provides general health insights and is not a substitute for professional medical advice.
