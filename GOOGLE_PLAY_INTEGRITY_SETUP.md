# Google Play Integrity API Setup

## Overview

The Baby Routine Tracker app integrates Google Play Integrity API to verify app authenticity and device integrity, protecting against modified apps, emulators, and compromised devices.

## Implementation Details

### Dependencies

The app includes the Google Play Integrity library:
```gradle
implementation 'com.google.android.play:integrity:1.4.0'
```

### IntegrityService

The `IntegrityService` class handles all integrity verification operations:

**Location:** `app/src/main/java/com/github/slamdev/babyroutinetracker/service/IntegrityService.kt`

**Key Features:**
- Device integrity verification before sensitive operations
- Contextual nonce generation for each request
- Specialized methods for different operation types:
  - `verifyAuthenticationIntegrity()` - For authentication operations
  - `verifyDataWriteIntegrity(dataType)` - For data write operations  
  - `verifyBabyProfileIntegrity(action)` - For baby profile operations

### Integration Points

#### 1. ActivityService Integration
- **startActivity()** - Verifies integrity before starting sleep/feeding activities
- **logCompletedFeeding()** - Verifies integrity before logging feeding activities
- **logPoop()** - Verifies integrity before logging diaper changes

#### 2. InvitationService Integration  
- **createBabyProfile()** - Verifies integrity before creating baby profiles

### Development vs Production Behavior

**Current State (Development):**
- Integrity checks are performed but failures only generate warnings
- Operations continue even if integrity verification fails
- Extensive logging for debugging purposes

**Production Recommendations:**
```kotlin
// In production, uncomment these lines to enforce integrity:
// if (integrityResult.isFailure) {
//     return Result.failure(SecurityException("Device integrity verification failed"))
// }
```

## Required Configuration

### 1. Google Cloud Project Setup

1. **Enable Play Integrity API:**
   - Go to Google Cloud Console
   - Navigate to APIs & Services > Library
   - Search for "Play Integrity API"
   - Enable the API for your project

2. **Get Cloud Project Number:**
   - Go to Google Cloud Console > Project Settings
   - Copy your project number (numeric ID)
   - Update `IntegrityService.kt`:
   ```kotlin
   private const val CLOUD_PROJECT_NUMBER = 123456789L // Replace with actual project number
   ```

### 2. Firebase Console Configuration

1. **Link Google Cloud Project:**
   - Ensure your Firebase project is linked to the same Google Cloud project
   - Go to Firebase Console > Project Settings > General
   - Verify the Google Cloud project ID matches

2. **App Configuration:**
   - Ensure your app is properly registered in Firebase Console
   - Download the latest `google-services.json` file
   - Verify the package name matches your app's applicationId

### 3. Play Console Setup (For Production)

1. **Upload App Bundle:**
   - Upload your signed app bundle to Play Console (Internal Testing track is sufficient)
   - The app must be uploaded to Play Console for integrity verification to work properly

2. **Enable Play Integrity:**
   - The Play Integrity API automatically works for apps distributed through Play Console
   - No additional Play Console configuration is required

## Security Considerations

### 1. Nonce Generation
- Each integrity request uses a unique nonce with timestamp and operation context
- Nonce format: `baby_routine_tracker_{operation}_{timestamp}_{random}`

### 2. Token Validation
- Current implementation validates token presence locally
- **Production Recommendation:** Send tokens to your backend for server-side validation using Google's validation endpoint

### 3. Error Handling
- Integrity failures are logged with appropriate context
- Users receive user-friendly error messages without exposing security details
- Operations can proceed or fail based on configuration (development vs production)

## Testing

### Development Testing
1. **Debug Builds:**
   - Integrity checks may fail on debug builds signed with debug certificates
   - Consider implementing debug-specific behavior or testing on release builds

2. **Emulator Testing:**
   - Emulators typically fail integrity checks
   - Test on physical devices for accurate results

### Production Testing
1. **Release Builds:**
   - Test with release-signed APKs/AABs
   - Upload to Play Console (Internal Testing) for full integrity verification

2. **Device Coverage:**
   - Test on various device types and Android versions
   - Monitor integrity success rates in production logs

## Monitoring and Maintenance

### 1. Logging
- All integrity checks are logged with operation context
- Failed integrity checks include error details for debugging
- Success/failure rates should be monitored in production

### 2. Updates
- Google Play Integrity library should be kept up to date
- Monitor Google's documentation for API changes or security updates

### 3. Performance
- Integrity checks add latency to operations
- Consider caching results for repeated operations within a session
- Monitor user experience impact

## Troubleshooting

### Common Issues

1. **"Project number not found" errors:**
   - Verify the Cloud project number is correct
   - Ensure Play Integrity API is enabled

2. **Token generation failures:**
   - Check if app is properly registered in Firebase/Play Console  
   - Verify `google-services.json` is up to date

3. **Debug build failures:**
   - Expected behavior for debug builds
   - Test with release builds for accurate results

4. **Emulator failures:**
   - Expected behavior on emulators
   - Use physical devices for testing

### Debug Commands

```bash
# Check if Play Integrity API is available
adb shell dumpsys package com.github.slamdev.babyroutinetracker | grep "Play Integrity"

# View integrity-related logs
adb logcat | grep "IntegrityService"
```

## Future Enhancements

### 1. Backend Validation
- Implement server-side token validation
- Use Google's token validation endpoint for enhanced security

### 2. Adaptive Policies
- Implement different integrity requirements based on operation sensitivity
- Allow configuration of integrity enforcement levels

### 3. Fallback Mechanisms
- Implement graceful degradation for devices without Play Integrity support
- Consider alternative security measures for unsupported scenarios

## References

- [Google Play Integrity API Documentation](https://developer.android.com/google/play/integrity)
- [Firebase Play Integrity Guide](https://firebase.google.com/docs/app-check/android/play-integrity-provider)
- [Play Console App Security Guidelines](https://support.google.com/googleplay/android-developer/answer/113469)
