package com.github.slamdev.babyroutinetracker.service

import android.util.Log
import com.github.slamdev.babyroutinetracker.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

/**
 * Service for managing user data and FCM tokens
 */
class UserService {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val messaging = FirebaseMessaging.getInstance()

    companion object {
        private const val TAG = "UserService"
        private const val USERS_COLLECTION = "users"
    }

    /**
     * Update user's FCM token in Firestore
     */
    fun updateFcmToken(token: String) {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            try {
                firestore.collection(USERS_COLLECTION)
                    .document(currentUser.uid)
                    .update("fcmToken", token)
                Log.d(TAG, "FCM token updated for user: ${currentUser.uid}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to update FCM token for user: ${currentUser.uid}", e)
            }
        }
    }

    /**
     * Initialize FCM token for current user
     */
    suspend fun initializeFcmToken(): Result<String> {
        return try {
            val token = messaging.token.await()
            Log.d(TAG, "FCM token retrieved: $token")
            
            val currentUser = auth.currentUser
            if (currentUser != null) {
                firestore.collection(USERS_COLLECTION)
                    .document(currentUser.uid)
                    .update("fcmToken", token)
                    .await()
                Log.d(TAG, "FCM token saved to Firestore for user: ${currentUser.uid}")
            }
            
            Result.success(token)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize FCM token", e)
            Result.failure(e)
        }
    }

    /**
     * Get FCM tokens for all partners of a baby
     */
    suspend fun getPartnerFcmTokens(babyId: String, excludeCurrentUser: Boolean = true): List<String> {
        return try {
            val currentUserId = auth.currentUser?.uid
            
            // Get baby document to find all parent IDs
            val babyDoc = firestore.collection("babies").document(babyId).get().await()
            val parentIds = babyDoc.get("parentIds") as? List<String> ?: emptyList()
            
            // Filter out current user if needed
            val targetParentIds = if (excludeCurrentUser && currentUserId != null) {
                parentIds.filter { it != currentUserId }
            } else {
                parentIds
            }
            
            // Get FCM tokens for all partner parents
            val tokens = mutableListOf<String>()
            for (parentId in targetParentIds) {
                try {
                    val userDoc = firestore.collection(USERS_COLLECTION).document(parentId).get().await()
                    val user = userDoc.toObject(User::class.java)
                    user?.fcmToken?.let { token ->
                        if (token.isNotBlank()) {
                            tokens.add(token)
                        }
                    }
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to get FCM token for parent: $parentId", e)
                }
            }
            
            Log.d(TAG, "Retrieved ${tokens.size} FCM tokens for baby: $babyId")
            tokens
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get partner FCM tokens for baby: $babyId", e)
            emptyList()
        }
    }
}
