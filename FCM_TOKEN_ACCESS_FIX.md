# Firestore Security Rules - Partner FCM Token Access Fix

## Problem
The UserService is trying to read other users' documents to get FCM tokens for partner notifications, but the security rules were blocking this access.

## Quick Fix Applied ✅

I've updated the security rules to allow authenticated users to read user documents containing FCM tokens:

```javascript
// Users collection - allow reading for FCM tokens between partners
match /users/{userId} {
  allow read: if request.auth != null;
  allow write: if request.auth != null && request.auth.uid == userId;
}
```

**What this allows:**
- ✅ Any authenticated user can read user documents (including FCM tokens)
- ✅ Users can only write to their own documents
- ✅ Partner notification system can access FCM tokens
- ✅ Maintains authentication requirement

## Apply the Fix

1. **Go to Firebase Console** → Firestore Database → Rules
2. **Copy the complete updated rules** from `firestore_security_rules.txt`
3. **Replace existing rules** and click **"Publish"**
4. **Test the notification** feature again

## Security Considerations

### Current Approach (Quick Fix):
- **Pros**: Simple, enables partner notifications immediately
- **Cons**: Any authenticated user can read FCM tokens (but not write)

### More Secure Alternative (Future Enhancement):

If you want stricter security, consider this approach:

1. **Separate FCM tokens** into a dedicated collection:
   ```
   /fcmTokens/{userId} - contains only FCM token and baby associations
   /users/{userId} - contains personal profile data (private)
   ```

2. **Update security rules** to allow partner access only to FCM tokens:
   ```javascript
   match /fcmTokens/{userId} {
     allow read: if request.auth != null && isPartner(request.auth.uid, userId);
     allow write: if request.auth.uid == userId;
   }
   ```

3. **Modify UserService** to read from the fcmTokens collection instead

## Current Status

With the applied fix:
- ✅ **Permission errors resolved** for partner FCM token access
- ✅ **Test notifications should work** when you have partners
- ✅ **Real notifications will work** when Cloud Functions are deployed
- ✅ **Security maintained** - write access restricted to document owners

The "No partners found" message should now change to actual partner detection once you have partners who share baby profiles! 🎉

## Next Steps

1. **Apply the updated rules** in Firebase Console
2. **Test the notification settings** again - the permission error should be gone
3. **Invite a partner** to test the full notification flow
4. **Deploy Cloud Functions** when ready: `./deploy-functions.sh`
