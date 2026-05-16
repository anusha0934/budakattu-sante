# Budakattu Sante Android App

Kotlin Android implementation for the BUDAKATTU-SANTE SOP.

## What is included

- Kotlin Android app using Jetpack Compose.
- MVVM structure with repositories and ViewModels.
- Room database for offline-first tribal leader inventory and local pre-orders.
- WorkManager sync job for pending batches and orders.
- Firebase Firestore integration points for `products`, `orders`, and `supply_logs`.
- Demo catalog fallback so the app opens even before Firebase is configured.

## Open in Android Studio

1. Open this folder in Android Studio:
   `C:\Users\user\OneDrive\Desktop\BUDAKATTU-SANTE`
2. Let Gradle sync.
3. Run the `app` configuration on an emulator or Android phone.

## Firebase setup

The app runs offline without Firebase, but cloud sync needs a Firebase Android app.

1. Create a Firebase project.
2. Add Android package:
   `com.mindmatrix.budakattusante`
3. Download `google-services.json`.
4. Place it at:
   `app/google-services.json`
5. Enable Cloud Firestore.
6. Publish the included `firestore.rules` from the Firebase console or CLI.

## Main flows

- Leader tab: save inventory batches locally, inspect supply logs, sync pending batches.
- Buyer tab: browse available produce, inspect details, lock a pre-order with MSP-based pricing.
- Orders tab: see local pre-orders and whether each has synced.
