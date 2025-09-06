# Baby Routine Tracker

A collaborative Android application designed to help new parents track their baby's daily activities including sleep, feedings, and diaper changes. Built with Kotlin and Jetpack Compose, featuring Firebase for real-time synchronization between devices and AI-powered suggestions via the Google Gemini API.

## ✨ Features

- **📊 Real-time Activity Tracking**: Track sleep, feeding, and diaper changes with timers and quick logging
- **🔄 Multi-Device Sync**: Real-time data synchronization across parent devices using Firebase
- **👥 Partner Collaboration**: Invite partner with unique codes to share baby profiles
- **🤖 AI-Powered Sleep Suggestions**: Get intelligent sleep routine recommendations via Gemini API
- **🎨 Modern UI**: Material Design 3 with automatic light/dark mode support
- **📱 Responsive Design**: Optimized for portrait, landscape, and split-screen modes
- **📈 Data Visualization**: Historical charts and trends analysis
- **🚀 Automated CI/CD**: Continuous deployment to Google Play Console internal testing

## 🛠️ Technology Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Backend**: Firebase (Firestore, Authentication, Functions, Messaging)
- **AI Integration**: Google Gemini API
- **Charts**: Vico for Android
- **Build System**: Gradle with Kotlin DSL
- **CI/CD**: GitHub Actions with Google Play Console integration

## 🚀 CI/CD Pipeline

This project features a fully automated CI/CD pipeline that:

- ✅ **Automatically builds and tests** on every push/PR
- ✅ **Deploys to Google Play Console** internal testing on main branch commits  
- ✅ **Auto-increments version codes** using commit count
- ✅ **Creates GitHub releases** with build information
- ✅ **Validates app signing** and Firebase configuration

### Quick CI/CD Setup
1. **Configure GitHub Secrets** - See [GitHub Secrets Reference](./GITHUB_SECRETS_REFERENCE.md)
2. **Set up Google Play Console** - Follow [CI Pipeline Setup Guide](./CI_PIPELINE_SETUP.md)
3. **Push to main branch** - Automatic deployment will start! 🎉

### CI/CD Files
- 🔧 [`.github/workflows/android-release.yml`](./.github/workflows/android-release.yml) - Main workflow
- 📋 [`CI_PIPELINE_SETUP.md`](./CI_PIPELINE_SETUP.md) - Detailed setup guide  
- 🔑 [`GITHUB_SECRETS_REFERENCE.md`](./GITHUB_SECRETS_REFERENCE.md) - Secrets quick reference
- ✅ [`scripts/validate-ci-setup.sh`](./scripts/validate-ci-setup.sh) - Validation script

## 💻 Development Setup

### Prerequisites
- Android Studio (latest version)
- JDK 17+
- Firebase project configured
- Google Cloud project with Gemini API access

### Local Development
1. Clone the repository
2. Open in Android Studio
3. Add your Firebase configuration (`google-services.json`)
4. Create signing keystore for release builds
5. Build and run on device/emulator

### Build Commands
```bash
# Debug build
./gradlew assembleDebug

# Release build  
./gradlew bundleRelease

# Run tests
./gradlew testDebugUnitTest

# Validate CI setup
./scripts/validate-ci-setup.sh
```

## 🏗️ Architecture

The app follows modern Android development patterns:

- **MVVM Architecture** with Jetpack Compose
- **Repository Pattern** for data layer abstraction  
- **Real-time Data Flow** using Firebase Firestore listeners
- **Coroutines** for asynchronous operations
- **Material Design 3** theming system

## 🔑 Key Components

- **Activity Tracking**: Sleep, feeding, and diaper change logging with timers
- **Partner Invitation System**: Secure sharing of baby profiles
- **Real-time Synchronization**: Cross-device data consistency  
- **AI Sleep Recommendations**: Gemini API integration for routine suggestions
- **Data Visualization**: Charts for sleep patterns and feeding trends
- **Multi-language Support**: English and Russian localization

## 📱 Supported Features

### Activity Types
- 😴 **Sleep Tracking**: Start/stop timers with duration calculations
- 🤱 **Breast Feeding**: Timer-based tracking with duration logging
- 🍼 **Bottle Feeding**: Quick amount logging with notes
- 💩 **Diaper Changes**: Instant logging with optional notes

### User Experience
- **4-Card Dashboard**: Responsive grid layout for all activities
- **Swipe Navigation**: Horizontal navigation between screens
- **Activity History**: Complete log with editing and deletion
- **Profile Management**: Baby profiles with age calculations
- **Account Management**: Full data deletion and privacy controls

## 🤝 Contributing

This is a personal project, but contributions are welcome:

1. Fork the repository
2. Create feature branch
3. Follow existing code patterns and documentation
4. Write tests for new functionality
5. Submit pull request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🔐 Privacy & Security

- All data is stored securely in Firebase with proper access controls
- No personal data is shared with third parties
- AI features use anonymized data only
- Users have full control over their data deletion
- End-to-end encryption for sensitive information

## 📞 Support

For issues and feature requests, please use the GitHub issue tracker.

---

**🎯 Ready to deploy?** Follow the [CI Pipeline Setup Guide](./CI_PIPELINE_SETUP.md) to get automatic deployments to Google Play Console!


