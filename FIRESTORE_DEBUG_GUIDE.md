# Firestore Rules Debugging Guide

## Step 1: Create Simple Test Rules

To debug the permission issue, let's first apply these temporary rules that allow all access to the notification preferences collection:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Temporary: Allow all access to debug notification preferences
    match /notificationPreferences/{document=**} {
      allow read, write: if request.auth != null;
    }
    
    // Keep existing rules for other collections
    match /users/{userId} {
      allow read, write: if request.auth != null && request.auth.uid == userId;
    }
    
    match /babies/{babyId} {
      allow read: if request.auth != null && 
                     request.auth.uid in resource.data.parentIds;
      allow create: if request.auth != null && 
                       request.auth.uid in request.resource.data.parentIds;
      allow update: if request.auth != null && 
                       (request.auth.uid in resource.data.parentIds ||
                        request.auth.uid in request.resource.data.parentIds);
      allow delete: if request.auth != null && 
                       request.auth.uid in resource.data.parentIds;
    }
    
    match /invitations/{invitationId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null && 
                       request.auth.uid == request.resource.data.invitedBy;
      allow update: if request.auth != null;
      allow delete: if request.auth != null && 
                       request.auth.uid == resource.data.invitedBy;
    }
    
    match /babies/{babyId}/activities/{activityId} {
      allow read, write: if request.auth != null && 
                            request.auth.uid in get(/databases/$(database)/documents/babies/$(babyId)).data.parentIds;
    }
    
    match /babies/{babyId}/sleepPlans/{planId} {
      allow read, write: if request.auth != null && 
                            request.auth.uid in get(/databases/$(database)/documents/babies/$(babyId)).data.parentIds;
    }
  }
}
```

## Step 2: Apply Test Rules

1. Go to Firebase Console → Your Project → Firestore Database → Rules
2. Replace ALL existing rules with the test rules above
3. Click "Publish"
4. Test the notification settings screen

## Step 3: If Test Rules Work

If the notification settings work with the test rules, then we know:
- ✅ The collection path `/notificationPreferences/{doc}` is correct
- ✅ The issue is with the specific rule logic

Then we can apply the proper rules with correct logic.

## Step 4: If Test Rules Still Fail

If even the test rules fail, then:
- ❌ There might be a different collection path being used
- ❌ The user authentication might have issues
- ❌ There might be a different Firestore database being used

## Debug Information to Check

1. **Firebase Console → Firestore Database → Data**
   - Look for the `notificationPreferences` collection
   - Check the document structure and field names
   - Verify the document ID format

2. **Firebase Console → Authentication**
   - Verify the user is properly authenticated
   - Check the user UID

3. **Android App Logs**
   - Look for any other error messages
   - Check if the user is authenticated before accessing preferences

Let me know the results of applying the test rules, and I'll help you proceed with the proper solution!
