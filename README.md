# Tone Rewriter

Tone Rewriter is a modern Android application built with Jetpack Compose that leverages AI to rewrite your text into different tones. Whether you want your message to sound more professional, casual, or enthusiastic, Tone Rewriter intelligently adapts your text while maintaining your core message.

## Features

- **AI-Powered Tone Rewriting**: Instantly rewrite text into multiple different tones.
- **Modern UI**: Built entirely with Jetpack Compose.
- **Material 3 Design**: Features a beautiful "Bubblegum & Sky" (blue + pink) theme with full support for light and dark schemes.
- **Custom Typography**: Integrated with the sleek Space Grotesk font family.
- **Adaptive Launcher Icon**: Custom monochrome and adaptive launcher icon support.
- **Firebase Integration**: Uses Firebase backend services for AI operations.

## Setup Instructions

> **Note**: The `google-services.json` file is intentionally excluded from this repository for security reasons.

To build and run this project, you will need to add your own Firebase configuration:
1. Create a Firebase project in the Firebase Console.
2. Register an Android app with the package name `com.bhatt.tonerewriter`.
3. Download the `google-services.json` file.
4. Place the `google-services.json` file inside the `app/` directory of this project.
5. Build and run the app from Android Studio.

## Technologies Used

- Kotlin
- Jetpack Compose
- Material Design 3
- Firebase AI / Cloud Services
