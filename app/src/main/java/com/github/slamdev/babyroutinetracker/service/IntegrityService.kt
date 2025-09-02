package com.github.slamdev.babyroutinetracker.service

import android.content.Context
import android.util.Log
import com.google.android.play.core.integrity.IntegrityManager
import com.google.android.play.core.integrity.IntegrityManagerFactory
import com.google.android.play.core.integrity.IntegrityTokenRequest
import com.google.android.play.core.integrity.IntegrityTokenResponse
import kotlinx.coroutines.tasks.await

/**
 * Service for managing Google Play Integrity checks to verify app authenticity and device integrity.
 * This helps protect against modified apps, emulators, and compromised devices.
 */
class IntegrityService(private val context: Context) {
    
    companion object {
        private const val TAG = "IntegrityService"
        private const val NONCE_PREFIX = "baby_routine_tracker_"
        private const val CLOUD_PROJECT_NUMBER = 346075310302L // Replace with actual Google Cloud project number
    }
    
    private val integrityManager: IntegrityManager = IntegrityManagerFactory.create(context)
    
    /**
     * Verify device integrity before performing sensitive operations.
     * 
     * @param operation Description of the operation being performed (for logging and nonce generation)
     * @return Result indicating whether the integrity check passed
     */
    suspend fun verifyIntegrity(operation: String): Result<Boolean> {
        return try {
            Log.d(TAG, "Starting integrity verification for operation: $operation")
            
            // Generate a unique nonce for this request
            val nonce = generateNonce(operation)
            
            // Create integrity token request
            val integrityTokenRequest = IntegrityTokenRequest.builder()
                .setNonce(nonce)
                .setCloudProjectNumber(CLOUD_PROJECT_NUMBER) // Your Google Cloud project number
                .build()
            
            // Request integrity token
            val integrityTokenResponse: IntegrityTokenResponse = integrityManager
                .requestIntegrityToken(integrityTokenRequest)
                .await()
            
            // Get the integrity token
            val token = integrityTokenResponse.token()
            
            if (token.isNullOrEmpty()) {
                Log.w(TAG, "Integrity token is null or empty for operation: $operation")
                return Result.failure(SecurityException("Failed to obtain integrity token"))
            }
            
            Log.i(TAG, "Integrity verification successful for operation: $operation")
            
            // In a production app, you would typically send this token to your backend
            // for server-side verification. For now, we consider the successful token
            // generation as a pass.
            Result.success(true)
            
        } catch (e: Exception) {
            Log.e(TAG, "Integrity verification failed for operation: $operation", e)
            Result.failure(e)
        }
    }
    
    /**
     * Verify integrity for authentication-related operations.
     */
    suspend fun verifyAuthenticationIntegrity(): Result<Boolean> {
        return verifyIntegrity("authentication")
    }
    
    /**
     * Verify integrity for data write operations.
     */
    suspend fun verifyDataWriteIntegrity(dataType: String): Result<Boolean> {
        return verifyIntegrity("data_write_$dataType")
    }
    
    /**
     * Verify integrity for baby profile operations.
     */
    suspend fun verifyBabyProfileIntegrity(action: String): Result<Boolean> {
        return verifyIntegrity("baby_profile_$action")
    }
    
    /**
     * Check if Google Play Integrity API is available on this device.
     */
    fun isIntegrityAvailable(): Boolean {
        return try {
            IntegrityManagerFactory.create(context)
            true
        } catch (e: Exception) {
            Log.w(TAG, "Google Play Integrity API is not available", e)
            false
        }
    }
    
    /**
     * Generate a unique nonce for integrity requests.
     * The nonce should be unique for each request and include context about the operation.
     */
    private fun generateNonce(operation: String): String {
        val timestamp = System.currentTimeMillis()
        val randomComponent = (1..1000).random()
        return "${NONCE_PREFIX}${operation}_${timestamp}_$randomComponent"
    }
}
