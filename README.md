# Paleru Congress Party Android App

Private, bilingual Telugu/English Android coordination app for Congress members in the Paleru Assembly Constituency. It includes an Instagram-style shared activity feed, development updates, comments, reactions, photo posts, GPS links, leadership and Gram Panchayat directories, Arabic numerals (`0-9`), and elected-member age correction suggestions.

## Live architecture

- Native Android app: Kotlin, Jetpack Compose, Android 7.0+
- Shared API: FastAPI on Render
- Persistent data: MongoDB Atlas database `paleru_congress`
- API URL compiled into version 1.4.0: `https://paleru-congress-party-api.onrender.com`
- APK after deployment: `https://paleru-congress-party-api.onrender.com/downloads/PaleruCongress.apk`

Posts, comments, reactions, edits, and deletions use the shared API and are visible across installed devices after refresh. Local PINs, bookmarks, unsynced drafts, field notes, and profile-correction suggestions remain on each device.

## Deploy on Render

1. In Render, choose **New > Blueprint** and connect this GitHub repository.
2. Render reads `render.yaml` and creates `paleru-congress-party-api`.
3. Enter `MONGODB_URI` when Render requests the secret. Use the existing Bayes Pharma MongoDB Atlas URI from its Render environment; do not paste it into GitHub.
4. Keep `PALERU_SOCIAL_MONGODB_DB=paleru_congress` to isolate this app's collections.
5. Deploy and verify:
   - `https://paleru-congress-party-api.onrender.com/health`
   - `https://paleru-congress-party-api.onrender.com/paleru-social/health`
   - `https://paleru-congress-party-api.onrender.com/downloads/PaleruCongress.apk`

If Render changes the service name, update `PALERU_SOCIAL_API_BASE` in `app/build.gradle.kts`, rebuild the APK, commit it, and redeploy.

## MongoDB safety

The URI is read only from the Render `MONGODB_URI` environment variable. `.env` files are ignored and the public repository contains no database credentials. The backend creates isolated `devices`, `posts`, `comments`, `reactions`, and `media` collections in `paleru_congress`.

## Build Android APK

Requirements: Java 17 and Android SDK 35.

```powershell
$env:ANDROID_HOME="$env:LOCALAPPDATA\Android\Sdk"
$env:ANDROID_SDK_ROOT=$env:ANDROID_HOME
.\gradlew.bat --no-daemon --console=plain --max-workers=1 :app:testDebugUnitTest :app:assembleDebug :app:lintDebug
```

Copy `app/build/outputs/apk/debug/app-debug.apk` to `backend/PaleruCongress.apk` before committing a new mobile release.

## Current access boundary

The local PIN hides the app on a device, and device credentials prevent one device from editing another device's content. The current release does not yet verify Congress membership against an approved member list and does not provide administrator-managed roles. Do not distribute the APK publicly until verified login, account approval, moderation, and role-based permissions are added.

Sarpanch offices are non-party positions. Directory inclusion does not identify every Sarpanch as a Congress member. Photos and ages should be published only with verified sources and consent where required.
