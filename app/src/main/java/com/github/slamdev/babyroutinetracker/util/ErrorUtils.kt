package com.github.slamdev.babyroutinetracker.util

import android.util.Log

/**
 * Centralized error handling utilities for converting technical errors to user-friendly messages
 * 
 * This follows the implementation guide's error handling architecture:
 * - Service Layer: Error categorization and user-friendly messages
 * - ViewModel Layer: State management with specific error states
 * - UI Layer: Appropriate error display components
 */
object ErrorUtils {
    private const val TAG = "ErrorUtils"
    
    /**
     * Convert a Firebase/technical error into a user-friendly message that explains
     * what went wrong and what the user can do about it.
     * 
     * @param exception The original exception that occurred
     * @param operation A brief description of what operation was being performed
     * @param context Additional context for more specific error messages
     * @return A user-friendly error message with actionable guidance
     */
    fun getFirebaseErrorMessage(
        exception: Throwable,
        operation: String = "operation",
        context: String? = null
    ): String {
        Log.e(TAG, "Processing Firebase error for $operation${context?.let { " ($it)" } ?: ""}", exception)
        
        val errorMessage = exception.message ?: ""
        
        return when {
            // Authentication errors
            errorMessage.contains("USER_NOT_FOUND") || 
            errorMessage.contains("INVALID_EMAIL") ||
            errorMessage.contains("WRONG_PASSWORD") -> {
                "Please sign in again to continue using the app"
            }
            
            errorMessage.contains("USER_DISABLED") -> {
                "Your account has been disabled. Please contact support for assistance"
            }
            
            // Permission errors
            errorMessage.contains("PERMISSION_DENIED") -> {
                when (operation.lowercase()) {
                    "baby profile access", "view baby activities" -> 
                        "You don't have permission to view this baby's information"
                    "create activity", "update activity", "delete activity" ->
                        "You don't have permission to modify activities for this baby"
                    "invite partner" ->
                        "You don't have permission to invite others to this baby profile"
                    else -> "You don't have permission to perform this action"
                }
            }
            
            // Network connectivity errors
            errorMessage.contains("UNAVAILABLE") ||
            errorMessage.contains("DEADLINE_EXCEEDED") ||
            errorMessage.contains("network") ||
            errorMessage.contains("timeout") -> {
                "Unable to connect to the server. Please check your internet connection and try again"
            }
            
            // Database setup/indexing errors
            errorMessage.contains("FAILED_PRECONDITION") ||
            errorMessage.contains("index") ||
            errorMessage.contains("requires an index") -> {
                "The database is being set up. Please try again in a few minutes"
            }
            
            // Data validation errors
            errorMessage.contains("INVALID_ARGUMENT") -> {
                when (operation.lowercase()) {
                    "create baby profile" -> "Please check that all baby information is filled out correctly"
                    "log activity" -> "Please check that the activity information is valid"
                    "update activity" -> "Please check that the updated information is valid"
                    else -> "Please check that the information you entered is valid"
                }
            }
            
            // Resource exhaustion
            errorMessage.contains("RESOURCE_EXHAUSTED") ||
            errorMessage.contains("QUOTA_EXCEEDED") -> {
                "The service is currently busy. Please try again in a few minutes"
            }
            
            // Document not found
            errorMessage.contains("NOT_FOUND") -> {
                when (operation.lowercase()) {
                    "baby profile" -> "The baby profile could not be found"
                    "activity" -> "The activity could not be found"
                    "invitation" -> "The invitation code is invalid or has expired"
                    else -> "The requested information could not be found"
                }
            }
            
            // Conflict errors (concurrent modifications)
            errorMessage.contains("ABORTED") ||
            errorMessage.contains("ALREADY_EXISTS") -> {
                "Someone else updated this information at the same time. Please refresh and try again"
            }
            
            // Internal server errors
            errorMessage.contains("INTERNAL") ||
            errorMessage.contains("UNKNOWN") -> {
                "Something went wrong on our end. Please try again in a few minutes"
            }
            
            // App-specific business logic errors
            errorMessage.contains("User not authenticated") -> {
                "Please sign in to continue"
            }
            
            errorMessage.contains("No access to baby profile") -> {
                "You don't have access to this baby's information"
            }
            
            errorMessage.contains("Baby profile not found") -> {
                "The baby profile could not be found. It may have been deleted"
            }
            
            errorMessage.contains("No ongoing") && operation.contains("sleep") -> {
                "No sleep session is currently active to modify"
            }
            
            errorMessage.contains("No ongoing") && operation.contains("feeding") -> {
                "No feeding session is currently active to modify"
            }
            
            errorMessage.contains("Invalid invitation") -> {
                "The invitation code is invalid or has already been used"
            }
            
            errorMessage.contains("expired") -> {
                "The invitation code has expired. Please request a new invitation"
            }
            
            // Default fallback with operation context
            else -> {
                when (operation.lowercase()) {
                    "start sleep", "end sleep" -> "Unable to update sleep tracking. Please try again"
                    "start feeding", "end feeding", "log bottle" -> "Unable to log feeding. Please try again"
                    "log diaper", "log poop" -> "Unable to log diaper change. Please try again"
                    "create baby profile" -> "Unable to create baby profile. Please try again"
                    "update baby profile" -> "Unable to update baby profile. Please try again"
                    "invite partner" -> "Unable to send invitation. Please try again"
                    "join profile" -> "Unable to join baby profile. Please try again"
                    "delete activity" -> "Unable to delete activity. Please try again"
                    "update activity" -> "Unable to update activity. Please try again"
                    else -> "Unable to complete $operation. Please try again"
                }
            }
        }
    }
    
    /**
     * Get a user-friendly error message for network-related issues specifically
     */
    fun getNetworkErrorMessage(operation: String = "operation"): String {
        return "No internet connection. Please check your network and try $operation again"
    }
    
    /**
     * Get a user-friendly error message for validation errors
     */
    fun getValidationErrorMessage(field: String, issue: String? = null): String {
        return when (issue?.lowercase()) {
            "required", "empty", "blank" -> "$field is required"
            "invalid" -> "Please enter a valid $field"
            "too_short" -> "$field is too short"
            "too_long" -> "$field is too long"
            "format" -> "$field format is invalid"
            else -> "Please check the $field and try again"
        }
    }
    
    /**
     * Format error message for logging with context
     */
    fun logError(
        tag: String,
        operation: String,
        exception: Throwable,
        context: Map<String, Any?> = emptyMap()
    ) {
        val contextString = context.entries.joinToString(", ") { "${it.key}=${it.value}" }
        Log.e(tag, "Failed $operation${if (contextString.isNotEmpty()) " [$contextString]" else ""}", exception)
    }
    
    /**
     * Check if an error is retryable (user should see retry option)
     */
    fun isRetryableError(exception: Throwable): Boolean {
        val errorMessage = exception.message ?: ""
        return when {
            // Network issues are retryable
            errorMessage.contains("UNAVAILABLE") ||
            errorMessage.contains("DEADLINE_EXCEEDED") ||
            errorMessage.contains("network") ||
            errorMessage.contains("timeout") -> true
            
            // Database setup issues are retryable
            errorMessage.contains("FAILED_PRECONDITION") ||
            errorMessage.contains("index") -> true
            
            // Server overload is retryable
            errorMessage.contains("RESOURCE_EXHAUSTED") ||
            errorMessage.contains("QUOTA_EXCEEDED") -> true
            
            // Internal server errors are retryable
            errorMessage.contains("INTERNAL") ||
            errorMessage.contains("UNKNOWN") -> true
            
            // Concurrent modification conflicts are retryable
            errorMessage.contains("ABORTED") -> true
            
            // Permission and authentication errors are not retryable
            errorMessage.contains("PERMISSION_DENIED") ||
            errorMessage.contains("USER_NOT_FOUND") ||
            errorMessage.contains("INVALID_EMAIL") -> false
            
            // Validation errors are not retryable (user needs to fix input)
            errorMessage.contains("INVALID_ARGUMENT") -> false
            
            // Not found errors are generally not retryable
            errorMessage.contains("NOT_FOUND") -> false
            
            // Default to retryable for unknown errors
            else -> true
        }
    }
    
    /**
     * Get appropriate suggestions for common error scenarios
     */
    fun getErrorSuggestions(exception: Throwable, operation: String): List<String> {
        val errorMessage = exception.message ?: ""
        val suggestions = mutableListOf<String>()
        
        when {
            errorMessage.contains("network") || errorMessage.contains("UNAVAILABLE") -> {
                suggestions.addAll(listOf(
                    "Check your internet connection",
                    "Try switching between WiFi and mobile data",
                    "Try again in a few moments"
                ))
            }
            
            errorMessage.contains("PERMISSION_DENIED") -> {
                if (operation.contains("baby") || operation.contains("profile")) {
                    suggestions.addAll(listOf(
                        "Make sure you've been invited to this baby profile",
                        "Try signing out and signing back in",
                        "Contact the person who shared the profile with you"
                    ))
                }
            }
            
            errorMessage.contains("FAILED_PRECONDITION") || errorMessage.contains("index") -> {
                suggestions.addAll(listOf(
                    "Wait a few minutes for the database setup to complete",
                    "Try refreshing the app"
                ))
            }
            
            errorMessage.contains("expired") || errorMessage.contains("invalid") -> {
                if (operation.contains("invitation")) {
                    suggestions.addAll(listOf(
                        "Request a new invitation code",
                        "Double-check the invitation code for typos"
                    ))
                }
            }
        }
        
        return suggestions
    }
}
