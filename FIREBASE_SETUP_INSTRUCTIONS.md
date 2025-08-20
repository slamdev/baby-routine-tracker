# Firebase Setup Instructions for Baby Routine Tracker

## 🔥 Firebase Project Setup

### Step 1: Create Firebase Project
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Create a project" or "Add project"
3. Enter project name: `baby-routine-tracker`
4. Enable Google Analytics (optional but recommended)
5. Choose or create Google Analytics account
6. Click "Create project"

### Step 2: Add Android App to Firebase
1. In the Firebase console, click the Android icon to add Android app
2. Register app with these details:
   - **Android package name**: `com.github.slamdev.babyroutinetracker`
   - **App nickname**: `Baby Routine Tracker`
   - **Debug signing certificate SHA-1**: (Get this from your development environment)

### Step 3: Download Configuration File
1. Download the `google-services.json` file
2. Replace the template `/app/google-services.json` file with the real one from Firebase

### Step 4: Enable Authentication
1. In Firebase console, go to **Authentication** > **Sign-in method**
2. Enable **Google** sign-in provider:
   - Enable the toggle
   - Add your project support email
   - Save

### Step 5: Configure Google Sign-In
1. In **Authentication** > **Sign-in method** > **Google**
2. Copy the **Web SDK configuration** > **Web client ID**
3. Update the web client ID in `SignInScreen.kt`:
   ```kotlin
   .requestIdToken("YOUR_ACTUAL_WEB_CLIENT_ID_HERE")
   ```

### Step 6: Get Debug SHA-1 Certificate
Run this command in your project root to get your debug SHA-1:
```bash
./gradlew signingReport
```
Copy the SHA-1 from the debug certificate and add it to your Firebase Android app settings.

### Step 7: Set up Firestore Database
1. In Firebase console, go to **Firestore Database**
2. Click **Create database**
3. Start in **test mode** (we'll add security rules later)
4. Choose your preferred location

## 🔧 Development Setup

### Required Files:
- ✅ `google-services.json` (template provided - replace with real file)
- ✅ Firebase dependencies added to `build.gradle`
- ✅ Authentication UI implemented
- ✅ Navigation setup complete

### Testing Authentication:
1. Replace the template `google-services.json` with your real Firebase config
2. Update the web client ID in `SignInScreen.kt`
3. Add your debug SHA-1 to Firebase project
4. Build and run the app
5. Test Google Sign-In flow

## 🚀 Current Implementation Status

### ✅ Completed Features:
- Firebase Authentication SDK integration
- Google Sign-In UI with proper error handling
- Authentication state management with ViewModel
- Navigation between Sign-In and Dashboard screens
- Session persistence (stays signed in on app restart)
- Clean, modern UI with Material 3 theming
- Responsive design support

### 🔄 Next Steps:
- Replace template Firebase config with real project config
- Test authentication flow on physical device
- Implement baby profile creation (Phase 1)
- Add activity logging features (Phase 2)

## 📱 User Experience

The implemented authentication flow follows the acceptance criteria:

1. **First Launch**: Shows "Sign in with Google" button prominently
2. **Sign-In Process**: Taps button → Google account selection → Authentication → Dashboard
3. **Session Persistence**: User remains logged in on subsequent app launches
4. **Sign-Out**: Available from dashboard with automatic navigation back to sign-in

## 🔒 Security Notes

- The template `google-services.json` contains placeholder values
- Web client ID is currently a placeholder - replace with real Firebase config
- Authentication tokens are handled securely by Firebase SDK
- User session managed automatically by Firebase Auth