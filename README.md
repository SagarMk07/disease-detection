# MedVision AI

MedVision AI is a Kotlin and Jetpack Compose Android health assistant. It combines authentication, AI symptom analysis, Gemini-powered image scanning, scan comparison, medical chat, health history, and a responsive light/dark Material 3 interface.

This app is intended for general health information and visual triage support. It does not diagnose, prescribe medicine, or replace a licensed medical professional.

## Features

- Email/password login and signup
- Firebase-ready authentication
- Local demo fallback when Firebase is unavailable
- Home dashboard with quick access cards
- AI symptom checker
- Gemini-powered disease image scan
- Camera capture for scan analysis
- Upload from gallery for scan analysis
- Gemini-powered scan comparison
- Compare earlier and newer skin/eye images
- Comparison results: `Improving`, `Worsening`, or `Unclear`
- Gemini-powered AI Health Chat
- Medical chatbot for symptoms, care steps, medicine safety questions, and red flags
- Firestore-backed health history timeline
- History entries for symptom checks, scans, and scan comparisons
- Settings screen with logout and theme toggle
- Working light and dark mode
- Theme-aware status/navigation bar icons
- 16 KB page-size compatible native libraries
- Medical disclaimer shown across health workflows

## Main Screens

- **Authentication**: Login and signup.
- **Home**: Entry point for symptom checking, scanning, scan comparison, AI chat, and history.
- **AI Symptom Checker**: Type symptoms and receive possible conditions, risk level, and suggested actions.
- **Disease Scan**: Capture an image with CameraX or upload one from gallery, then analyze it with Gemini.
- **Scan Comparison**: Select an earlier image and a newer image to track visible changes over time.
- **AI Health Chat**: Ask follow-up medical questions in a chat interface powered by Gemini.
- **Health History**: Review past symptom checks, scan results, and comparison results.
- **Settings**: Toggle dark mode, read app info, and logout.

## Tech Stack

- Kotlin
- Jetpack Compose
- Material 3
- MVVM
- StateFlow
- Firebase Auth
- Firebase Firestore
- Retrofit
- Kotlinx Serialization
- Google Gemini API
- OpenAI API support for symptom checker
- CameraX
- Coil
- Lottie Compose
- Android DataStore

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

## AI Capabilities

### Symptom Checker

The symptom checker accepts natural-language symptoms and returns:

- Possible conditions
- Risk level
- Suggested actions

If no OpenAI API key is configured, the app returns a simple sample response so the screen still works during development.

### Disease Scan

The scan flow supports:

- Camera capture
- Gallery upload
- Gemini image analysis
- Preview of the analyzed image
- Result summary and confidence
- History saving

Gemini is prompted to provide broad visual triage only. It is instructed not to diagnose or claim certainty.

### Scan Comparison

The scan comparison flow supports:

- Earlier image selection
- Newer image selection
- Gemini comparison of visible changes
- Trend result: `Improving`, `Worsening`, or `Unclear`
- Confidence score
- Summary
- Suggested action
- History saving

The comparison prompt asks Gemini to choose `Unclear` when lighting, angle, focus, body part, or image quality makes comparison unreliable.

### AI Health Chat

The AI Health Chat uses Gemini for back-and-forth medical information support. It can help with:

- Understanding symptoms
- General self-care steps
- Medicine safety questions
- Common over-the-counter medicine categories
- Questions to ask a doctor
- Red flag symptoms that need urgent care

The chatbot is instructed not to diagnose, prescribe, or give personalized dosage instructions.

## Configuration

Add API keys to `local.properties` or environment variables. The app reads both.

### Firebase

Create a Firebase project for package name:

```text
com.medvision.ai
```

Enable Email/Password authentication and create a Firestore database.

```properties
FIREBASE_API_KEY=your_firebase_api_key
FIREBASE_APP_ID=your_firebase_app_id
FIREBASE_PROJECT_ID=your_project_id
FIREBASE_STORAGE_BUCKET=your_storage_bucket
```

Add your Firebase `google-services.json` file at:

```text
app/google-services.json
```

If Firebase is unavailable, the app falls back to local demo-friendly auth/history behavior.

### Gemini

Gemini powers image scan, scan comparison, and AI Health Chat.

```properties
GEMINI_API_KEY=your_gemini_api_key_here
GEMINI_MODEL=gemini-2.5-flash
GEMINI_BASE_URL=https://generativelanguage.googleapis.com/
```

### OpenAI

The symptom checker repository supports OpenAI Responses API.

```properties
OPENAI_API_KEY=your_openai_api_key_here
OPENAI_MODEL=gpt-5-mini
OPENAI_BASE_URL=https://api.openai.com/
```

## Build And Run

1. Open the project in Android Studio.
2. Let Gradle sync dependencies.
3. Make sure Android SDK 34 is installed.
4. Add API keys in `local.properties` if you want live AI/Firebase behavior.
5. Connect a device or start an emulator.
6. Run the `app` module.

Command-line build:

```powershell
.\gradlew.bat :app:assembleDebug
```

## 16 KB Page-Size Support

The project is configured to support Android devices with 16 KB memory pages.

Relevant details:

- Android Gradle Plugin: `8.5.2`
- CameraX upgraded to `1.4.2`
- Unused ML Kit image labeling dependency removed
- Packaged native libraries were verified with `LOAD_ALIGN=[16384...]`

This avoids Android Studio warnings such as:

```text
does not support 16KB device
```

## Theme Support

The app supports both dark mode and light mode.

The shared background, glass cards, buttons, navigation bar, chat bubbles, and system bars are theme-aware. In light mode, the status bar uses dark icons so battery, time, and notification icons remain visible.

## Safety Notes

- MedVision AI does not diagnose disease.
- MedVision AI does not prescribe medicine.
- Medicine suggestions are general information only.
- Users should follow package labels or clinician/pharmacist advice.
- The app should recommend urgent care for red flags such as chest pain, trouble breathing, stroke symptoms, severe allergic reaction, fainting, suicidal thoughts, severe dehydration, or rapidly worsening symptoms.
- Image analysis can be affected by lighting, focus, camera angle, and image quality.

## Disclaimer

MedVision AI provides general health insights and educational information only. It is not a substitute for professional medical advice, diagnosis, or treatment. Always consult a qualified healthcare professional for medical concerns.
