# Firebase Functions Deployment Error Fix

## Problem
You encountered this error while deploying Firebase Functions:

```
Permission denied on 'locations/europe-west4' (or it may not exist)
```

## Root Cause
The `europe-west4` region may not be available for Firebase Functions in your project, or there could be permissions/billing issues.

## Solution Applied ✅

### 1. **Changed Region from europe-west4 to europe-west1**
- **europe-west1** (Belgium) is more widely supported than europe-west4
- Updated both Cloud Functions and Android app configuration

### 2. **Fixed Cloud Function Code Issues**
- Corrected 2nd generation API usage 
- Updated package.json to proper 2nd gen versions:
  - `firebase-functions`: `^5.0.0`
  - `firebase-admin`: `^12.0.0`

### 3. **Updated Android App**
Changed `PartnerNotificationService.kt`:
```kotlin
// Before
private val functions = Firebase.functions("europe-west4")

// After  
private val functions = Firebase.functions("europe-west1")
```

## Alternative Solutions (If europe-west1 Still Fails)

### Option 1: Use Default Region (Most Reliable)
Remove region specification to use `us-central1`:
```javascript
exports.sendPartnerNotifications = onCall({
  cors: true  // Remove region line
}, async (request) => {
```

And in Android app:
```kotlin
private val functions = Firebase.functions() // No region specified
```

### Option 2: Try Other European Regions
```javascript
region: 'europe-west3'  // Frankfurt, Germany
```

### Option 3: Check Project Setup
1. **Enable APIs** in Google Cloud Console:
   - Cloud Functions API
   - Cloud Build API  
   - Cloud Logging API

2. **Verify Billing** - Ensure Firebase project has billing enabled

3. **Check Permissions** - You need Editor or Owner role

## Next Steps

1. **Try deploying with europe-west1**:
   ```bash
   cd functions
   npm install
   firebase deploy --only functions
   ```

2. **If that fails, use us-central1** (most reliable):
   - Remove `region: 'europe-west1'` from both functions
   - Update Android app to `Firebase.functions()` (no region)
   - Deploy again

3. **Test the notification functionality** once deployed

## Status: Ready to Deploy ✅

The Cloud Functions code has been updated to:
- ✅ Use **europe-west1** region (more reliable than europe-west4)
- ✅ Proper **2nd generation** Firebase Functions API
- ✅ **Android app** updated to match new region
- ✅ **Troubleshooting guide** added for future issues

Try deploying now with the updated configuration!
