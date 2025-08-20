# Firebase Data Cleanup Guide

## Issue: Extra Fields in Invitation Documents

The Firebase invitations collection contains unwanted computed properties that were accidentally stored:

### ❌ **Fields That Should NOT Be Stored:**
- `expired` (Boolean) - Computed from `expiresAt` and current time
- `pending` (Boolean) - Computed from `status` and expiry
- `statusEnum` (String) - Computed from `status` field

### ✅ **Fields That SHOULD Be Stored:**
- `id` (String) - Invitation document ID
- `babyId` (String) - Reference to baby profile
- `invitedBy` (String) - User ID who created invitation
- `invitationCode` (String) - 6-character invitation code
- `status` (String) - "PENDING", "ACCEPTED", or "EXPIRED"
- `createdAt` (Timestamp) - When invitation was created
- `expiresAt` (Timestamp) - When invitation expires

## Fix Applied in Code

Added `@Exclude` annotations to computed properties in `Invitation.kt`:

```kotlin
@Exclude
fun getStatusEnum(): InvitationStatus { ... }

@Exclude  
fun isExpired(): Boolean { ... }

@Exclude
fun isPending(): Boolean { ... }
```

This prevents Firebase from serializing these methods as fields.

## Cleaning Up Existing Firebase Data

### Option 1: Manual Cleanup in Firebase Console (Recommended for Testing)

1. Go to Firebase Console > Firestore Database
2. Navigate to `invitations` collection
3. For each document, **delete these fields:**
   - `expired`
   - `pending` 
   - `statusEnum`
4. Keep all other fields intact

### Option 2: Automatic Cleanup (For Production)

If you have many documents, you could run a cleanup script. Here's a sample approach:

```javascript
// Firebase Admin SDK script (run from Node.js)
const admin = require('firebase-admin');
const serviceAccount = require('./path/to/serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

async function cleanupInvitations() {
  const invitationsRef = db.collection('invitations');
  const snapshot = await invitationsRef.get();
  
  const batch = db.batch();
  
  snapshot.forEach(doc => {
    const docRef = invitationsRef.doc(doc.id);
    batch.update(docRef, {
      expired: admin.firestore.FieldValue.delete(),
      pending: admin.firestore.FieldValue.delete(),
      statusEnum: admin.firestore.FieldValue.delete()
    });
  });
  
  await batch.commit();
  console.log('Cleaned up invitation documents');
}

cleanupInvitations().catch(console.error);
```

### Option 3: Let Natural App Usage Clean Up (Gradual)

Since the app no longer writes these fields, they will naturally disappear as:
1. New invitations are created (won't have extra fields)
2. Existing invitations are updated (extra fields will be removed)

## Verification

After cleanup, invitation documents should only contain these 7 fields:
- `id`
- `babyId` 
- `invitedBy`
- `invitationCode`
- `status`
- `createdAt`
- `expiresAt`

## Future Prevention

The `@Exclude` annotations will prevent this issue from happening again. All computed properties will only exist in client-side code and won't be stored in Firebase.
