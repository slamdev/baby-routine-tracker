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
  enforceAppCheck: true,
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
    // Create simplified FCM message
    const message = {
      notification: {
        title: notification.title || 'Baby Activity Update',
        body: notification.body || 'Your partner logged a new activity'
      },
      data: {
        babyId: notification.babyId || '',
        babyName: notification.babyName || '',
        activityType: notification.activityType || '',
        activityId: notification.activityId || '',
        partnerName: notification.partnerName || '',
        timestamp: notification.timestamp || Date.now().toString(),
        click_action: 'FLUTTER_NOTIFICATION_CLICK'
      }
    };

    // Send to all tokens using 2nd gen messaging
    const messaging = getMessaging();
    const promises = tokens.map(token => 
      messaging.send({ ...message, token })
    );

    const results = await Promise.allSettled(promises);
    
    // Count successful sends and log failures
    const successCount = results.filter(result => result.status === 'fulfilled').length;
    const failureCount = results.length - successCount;

    // Log detailed failure information
    results.forEach((result, index) => {
      if (result.status === 'rejected') {
        console.error(`Failed to send notification to token ${index}:`, result.reason);
        console.error('Error code:', result.reason?.code);
        console.error('Error message:', result.reason?.message);
      } else {
        console.log(`Successfully sent notification to token ${index}: ${result.value}`);
      }
    });

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