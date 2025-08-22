# Firestore Security Rules Fix for Notification Preferences

## Problem
The app is getting a `PERMISSION_DENIED` error when accessing notification preferences because the security rules don't match the actual collection structure.

## Root Cause
- **Expected**: Notification preferences in `/users/{userId}/notificationPreferences/{docId}`
- **Actual**: Notification preferences in `/notificationPreferences/{userId}_{babyId}`

## ✅ Solution Applied

I've updated the `firestore_security_rules.txt` file with the correct rule for the notification preferences collection:

```javascript
// Notification preferences - users can manage their own preferences
// Cloud Functions can read for notification filtering  
// Document ID format: {userId}_{babyId}
match /notificationPreferences/{preferencesId} {
  allow read: if request.auth != null && 
             (request.auth.uid == resource.data.userId ||
              request.auth.token.firebase.sign_in_provider == 'custom');
  allow write: if request.auth != null && 
              request.auth.uid == resource.data.userId;
  allow create: if request.auth != null && 
               request.auth.uid == request.resource.data.userId;
}
```

## 🔧 Next Steps to Fix the Error

### 1. Apply the Updated Security Rules

1. **Copy the complete updated rules** from `firestore_security_rules.txt`
2. **Open Firebase Console**: https://console.firebase.google.com/
3. **Navigate to your project** → Firestore Database → Rules
4. **Replace the existing rules** with the updated content from the file
5. **Click "Publish"** to apply the changes

### 2. Test the Notification Settings

After applying the rules:
1. **Close and reopen** the notification settings screen in your app
2. **Try accessing** notification preferences again
3. The permission error should be resolved

## 📋 What the Fixed Rules Allow

✅ **Users can**:
- Read their own notification preferences (`userId` matches authenticated user)
- Write/update their own notification preferences
- Create new notification preferences for themselves

✅ **Cloud Functions can**:
- Read any notification preferences (for sending notifications)
- This is identified by the custom sign-in provider

✅ **Security maintained**:
- Users cannot access other users' notification preferences
- All operations require authentication

## 🔍 Rule Breakdown

The key parts of the fixed rule:

```javascript
// Allow read if user owns the preferences OR it's a Cloud Function
allow read: if request.auth != null && 
           (request.auth.uid == resource.data.userId ||
            request.auth.token.firebase.sign_in_provider == 'custom');

// Allow write only if user owns the preferences
allow write: if request.auth != null && 
            request.auth.uid == resource.data.userId;
```

- `resource.data.userId` refers to the `userId` field in the notification preferences document
- `request.auth.uid` is the authenticated user's ID
- The Cloud Function check allows the notification system to read preferences

## ⚠️ Important Note

**You must apply these rules in Firebase Console** for the fix to take effect. The `firestore_security_rules.txt` file is just documentation - the actual rules need to be published in Firebase Console.

After applying the rules, your notification settings screen should work without permission errors! 🎉
