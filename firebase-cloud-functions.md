# Firebase Cloud Function for Partner Notifications (2nd Generation)

This document provides the Firebase Cloud Function code needed to send partner notifications using 2nd generation functions.

## Function Code (functions/index.js)

```javascript
const { onCall, HttpsError } = require('firebase-functions/v2/https');
const { onSchedule } = require('firebase-functions/v2/scheduler');
const { initializeApp } = require('firebase-admin/app');
const { getMessaging } = require('firebase-admin/messaging');
const { getFirestore, FieldValue } = require('firebase-admin/firestore');

initializeApp();

/**
 * Cloud Function to send partner notifications
 * Called from the Android app when activities are logged
 * Deployed to europe-west1 region (2nd generation)
 */
exports.sendPartnerNotifications = onCall({
  region: 'europe-west1',
  cors: true
}, async (request) => {
  // Verify user is authenticated
  if (!request.auth) {
    throw new HttpsError(
      'unauthenticated',
      'The function must be called while authenticated.'
    );
  }

  const { tokens, notification } = request.data;

  // Validate input
  if (!tokens || !Array.isArray(tokens) || tokens.length === 0) {
    throw new HttpsError(
      'invalid-argument',
      'The function must be called with a valid tokens array.'
    );
  }

  if (!notification || typeof notification !== 'object') {
    throw new HttpsError(
      'invalid-argument',
      'The function must be called with a valid notification object.'
    );
  }

  try {
    // Create FCM message
    const message = {
      notification: {
        title: notification.title || 'Baby Activity Update',
        body: notification.body || 'Your partner logged a new activity',
        icon: 'ic_notification'
      },
      data: {
        babyId: notification.babyId || '',
        babyName: notification.babyName || '',
        activityType: notification.activityType || '',
        activityId: notification.activityId || '',
        partnerName: notification.partnerName || '',
        timestamp: notification.timestamp || Date.now().toString(),
        icon: notification.icon || '🍼'
      },
      android: {
        priority: 'normal',
        notification: {
          channelId: 'partner_activity_notifications',
          priority: 'default',
          defaultSound: true,
          defaultVibrateTimings: true
        }
      }
    };

    // Send to all tokens using 2nd gen messaging
    const messaging = getMessaging();
    const promises = tokens.map(token => 
      messaging.send({ ...message, token })
    );

    const results = await Promise.allSettled(promises);
    
    // Count successful sends
    const successCount = results.filter(result => result.status === 'fulfilled').length;
    const failureCount = results.length - successCount;

    console.log(`Partner notifications sent: ${successCount} successful, ${failureCount} failed`);

    return {
      success: true,
      sent: successCount,
      failed: failureCount,
      message: `Sent ${successCount} notifications successfully`
    };

  } catch (error) {
    console.error('Error sending partner notifications:', error);
    throw new HttpsError(
      'internal',
      'Failed to send notifications',
      error
    );
  }
});

/**
 * Clean up expired FCM tokens
 * Runs daily to remove invalid tokens from user documents
 * Deployed to europe-west1 region (2nd generation)
 */
exports.cleanupExpiredTokens = onSchedule({
  schedule: 'every 24 hours',
  region: 'europe-west1',
  timeZone: 'Europe/Amsterdam'
}, async (event) => {
  console.log('Starting FCM token cleanup...');
  
  try {
    const db = getFirestore();
    const usersRef = db.collection('users');
    const snapshot = await usersRef.where('fcmToken', '!=', null).get();
    
    let cleanedCount = 0;
    const batch = db.batch();
    
    for (const doc of snapshot.docs) {
      const userData = doc.data();
      const token = userData.fcmToken;
      
      if (token) {
        try {
          // Try to send a test message to validate the token using 2nd gen messaging
          const messaging = getMessaging();
          await messaging.send({
            token: token,
            data: { test: 'true' },
            dryRun: true // Don't actually send
          });
        } catch (error) {
          // If token is invalid, remove it
          if (error.code === 'messaging/invalid-registration-token' ||
              error.code === 'messaging/registration-token-not-registered') {
            console.log(`Removing invalid token for user: ${doc.id}`);
            batch.update(doc.ref, { fcmToken: FieldValue.delete() });
            cleanedCount++;
          }
        }
      }
    }
    
    if (cleanedCount > 0) {
      await batch.commit();
      console.log(`Cleaned up ${cleanedCount} expired FCM tokens`);
    } else {
      console.log('No expired tokens found');
    }
    
    return { cleanedCount };
  } catch (error) {
    console.error('Error cleaning up FCM tokens:', error);
    throw error;
  }
});
```

## Package.json (Updated for 2nd Generation)

```json
## Package.json (Updated for 2nd Generation)

```json
{
  "name": "baby-routine-tracker-functions",
  "description": "Cloud Functions for Baby Routine Tracker (2nd Generation)",
  "scripts": {
    "serve": "firebase emulators:start --only functions",
    "shell": "firebase functions:shell",
    "start": "npm run shell",
    "deploy": "firebase deploy --only functions",
    "logs": "firebase functions:log"
  },
  "engines": {
    "node": "18"
  },
  "main": "index.js",
  "dependencies": {
    "firebase-admin": "^12.0.0",
    "firebase-functions": "^5.0.0"
  },
  "devDependencies": {
    "firebase-functions-test": "^3.1.0"
  },
  "private": true
}
```

## Deployment Instructions (2nd Generation Migration)

1. Install Firebase CLI:
   ```bash
   npm install -g firebase-tools
   ```

2. Initialize Firebase Functions in your project:
   ```bash
   firebase init functions
   ```

3. Replace the generated `functions/index.js` with the 2nd generation code above

4. Update `functions/package.json` with the 2nd generation dependencies above

5. **IMPORTANT - Migration from 1st to 2nd Generation:**
   ```bash
   cd functions
   npm install
   cd ..
   
   # Delete existing 1st generation functions (if they exist)
   firebase functions:delete sendPartnerNotifications --force
   firebase functions:delete cleanupExpiredTokens --force
   
   # Deploy new 2nd generation functions to europe-west1
   firebase deploy --only functions
   ```

6. Verify the 2nd generation functions are deployed correctly:
   ```bash
   firebase functions:list
   ```
   You should see:
   - Functions listed with region: europe-west1
   - Functions using the new 2nd generation runtime

7. Update Firestore Security Rules to allow the function to access user and baby data

## Important: Android App Configuration for europe-west1

Since the Cloud Function is deployed to europe-west1, you need to update the Android app to call the function in the correct region. Update the `PartnerNotificationService.kt` file:

```kotlin
// In PartnerNotificationService.kt, update the Firebase Functions instance
class PartnerNotificationService @Inject constructor(
    private val userService: UserService,
    private val notificationPreferencesService: NotificationPreferencesService
) {
    // Configure Firebase Functions to use europe-west1 region
    private val functions = Firebase.functions("europe-west1")
    
    suspend fun notifyPartnersOfActivity(
        babyId: String,
        activityType: String,
        activityDetails: Map<String, String> = emptyMap()
    ) {
        try {
            // ... rest of the method remains the same
        } catch (e: Exception) {
            Log.e("PartnerNotificationService", "Failed to send partner notifications", e)
        }
    }
}
```
   ```bash
   cd functions
   npm install
   cd ..
   firebase deploy --only functions
   ```

6. Update Firestore Security Rules to allow the function to access user and baby data

## Security Rules Update

Add these rules to allow the Cloud Function to access data:

```javascript
// In firestore.rules
service cloud.firestore {
  match /databases/{database}/documents {
    // Allow Cloud Functions to read user data for FCM tokens
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth.uid == userId;
    }
    
    // Allow Cloud Functions to read baby data for notifications
    match /babies/{babyId} {
      allow read: if request.auth != null && 
                     request.auth.uid in resource.data.parentIds;
      allow write: if request.auth != null && 
                      request.auth.uid in resource.data.parentIds;
    }
  }
}
```

## Troubleshooting

### Permission Denied or Region Not Available Error

If you encounter this error:
```
Permission denied on 'locations/europe-west1' (or it may not exist)
```

Try these solutions:

1. **Use a different region** - Some regions may not be available in your project:
   ```javascript
   // Try these alternative regions:
   region: 'us-central1'     // Most reliable, default region
   region: 'europe-west1'    // Europe (Belgium)
   region: 'europe-west3'    // Europe (Frankfurt)
   region: 'asia-northeast1' // Asia (Tokyo)
   ```

2. **Enable required APIs** in Google Cloud Console:
   - Cloud Functions API
   - Cloud Build API
   - Cloud Logging API

3. **Check billing** - Ensure your Firebase project has billing enabled

4. **Verify project permissions** - Ensure you have Editor or Owner role

5. **Try the default region first**:
   ```javascript
   // Remove region specification to use default us-central1
   exports.sendPartnerNotifications = onCall({
     cors: true
   }, async (request) => {
   ```

## Testing

You can test the function using the Firebase console or by sending test notifications from the app's notification settings screen.
