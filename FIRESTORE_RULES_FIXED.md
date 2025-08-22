# Fixed Firestore Security Rules

## Problem Identified

The issue occurs when trying to read a notification preferences document that doesn't exist yet. The security rule fails because `resource.data.userId` is null for non-existing documents.

## Solution: Updated Security Rules

Replace your Firestore rules with these corrected rules:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Users collection - users can only access their own document
    // Cloud Functions can read for FCM token access
    match /users/{userId} {
      allow read: if request.auth != null && 
                     (request.auth.uid == userId || 
                      request.auth.token.firebase.sign_in_provider == 'custom');
      allow write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Babies collection - users can only access babies where their userId is in parentIds array
    // Cloud Functions can read for notification context
    match /babies/{babyId} {
      allow read: if request.auth != null && 
                     (request.auth.uid in resource.data.parentIds ||
                      request.auth.token.firebase.sign_in_provider == 'custom');
      allow create: if request.auth != null && 
                       request.auth.uid in request.resource.data.parentIds;
      allow update: if request.auth != null && 
                       (request.auth.uid in resource.data.parentIds ||
                        request.auth.uid in request.resource.data.parentIds);
      allow delete: if request.auth != null && 
                       request.auth.uid in resource.data.parentIds;
    }
    
    // Notification preferences - FIXED RULES
    // Document ID format: {userId}_{babyId}
    match /notificationPreferences/{preferencesId} {
      // Allow read if:
      // 1. Document exists and user owns it, OR
      // 2. Document doesn't exist but user ID matches the document ID pattern, OR  
      // 3. It's a Cloud Function
      allow read: if request.auth != null && 
                     (
                       // If document exists, check ownership
                       (resource != null && request.auth.uid == resource.data.userId) ||
                       // If document doesn't exist, check if user ID matches document ID pattern
                       (resource == null && preferencesId.matches(request.auth.uid + '_.*')) ||
                       // Allow Cloud Functions
                       request.auth.token.firebase.sign_in_provider == 'custom'
                     );
      
      // Allow write if user owns the document (existing or new)
      allow write: if request.auth != null && 
                      (
                        // For existing documents
                        (resource != null && request.auth.uid == resource.data.userId) ||
                        // For new documents
                        (resource == null && request.auth.uid == request.resource.data.userId)
                      );
      
      // Allow create if user is creating their own preferences
      allow create: if request.auth != null && 
                       request.auth.uid == request.resource.data.userId &&
                       preferencesId.matches(request.auth.uid + '_.*');
    }
    
    // Invitations collection - special rules for invitation system
    match /invitations/{invitationId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null && 
                       request.auth.uid == request.resource.data.invitedBy;
      allow update: if request.auth != null;
      allow delete: if request.auth != null && 
                       request.auth.uid == resource.data.invitedBy;
    }
    
    // Activity logs - only parents can access
    // Cloud Functions can read for notification context
    match /babies/{babyId}/activities/{activityId} {
      allow read: if request.auth != null && 
                     (request.auth.uid in get(/databases/$(database)/documents/babies/$(babyId)).data.parentIds ||
                      request.auth.token.firebase.sign_in_provider == 'custom');
      allow write: if request.auth != null && 
                      request.auth.uid in get(/databases/$(database)/documents/babies/$(babyId)).data.parentIds;
    }
    
    // Sleep plans (for future implementation) - only parents can access
    match /babies/{babyId}/sleepPlans/{planId} {
      allow read, write: if request.auth != null && 
                            request.auth.uid in get(/databases/$(database)/documents/babies/$(babyId)).data.parentIds;
    }
  }
}
```

## Key Fix

The main fix is in the notification preferences rule:

```javascript
allow read: if request.auth != null && 
           (
             // If document exists, check ownership
             (resource != null && request.auth.uid == resource.data.userId) ||
             // If document doesn't exist, check if user ID matches document ID pattern
             (resource == null && preferencesId.matches(request.auth.uid + '_.*')) ||
             // Allow Cloud Functions
             request.auth.token.firebase.sign_in_provider == 'custom'
           );
```

This allows reading:
1. **Existing documents** - if the user owns them
2. **Non-existing documents** - if the document ID starts with the user's ID
3. **Cloud Functions** - for notification processing

## Apply These Rules

1. **Copy the complete rules above**
2. **Go to Firebase Console** → Firestore Database → Rules  
3. **Replace ALL existing rules** with the fixed rules
4. **Click "Publish"**
5. **Test the notification settings** in your app

This should resolve the permission error! 🎉
