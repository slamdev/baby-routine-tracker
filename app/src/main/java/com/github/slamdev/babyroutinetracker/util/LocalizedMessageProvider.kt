package com.github.slamdev.babyroutinetracker.util

import android.content.Context
import com.github.slamdev.babyroutinetracker.R

/**
 * Provides localized messages for ViewModels and other components that need
 * string resources but don't have direct access to Compose stringResource().
 */
class LocalizedMessageProvider(private val context: Context) {
    
    // Success Messages (using existing strings)
    fun getProfileCreatedSuccessMessage(): String = 
        context.getString(R.string.success_baby_profile_created)
    
    fun getInvitationCreatedSuccessMessage(): String = 
        context.getString(R.string.success_invitation_created)
    
    fun getJoinInvitationSuccessMessage(babyName: String): String = 
        context.getString(R.string.success_joined_profile, babyName)
    
    fun getProfileUpdatedSuccessMessage(): String = 
        context.getString(R.string.success_baby_profile_updated)
    
    fun getBottleFeedingLoggedSuccessMessage(): String = 
        context.getString(R.string.success_bottle_feeding_logged)
    
    fun getPoopLoggedSuccessMessage(): String = 
        context.getString(R.string.success_poop_logged)
    
    fun getDiaperUpdatedSuccessMessage(): String = 
        context.getString(R.string.success_diaper_updated)
    
    fun getFeedingUpdatedSuccessMessage(): String = 
        context.getString(R.string.success_feeding_updated)
    
    fun getStartTimeUpdatedSuccessMessage(): String = 
        context.getString(R.string.success_start_time_updated)
    
    // Error Messages (using existing strings)
    fun getEnterInvitationCodeErrorMessage(): String = 
        context.getString(R.string.error_enter_invitation_code)
    
    fun getBabyProfileNotFoundErrorMessage(): String = 
        context.getString(R.string.error_baby_profile_not_found)
    
    fun getSelectBabyProfileFirstErrorMessage(): String = 
        context.getString(R.string.error_select_baby_profile_first)
    
    // Generic error messages
    fun getNetworkErrorMessage(): String = 
        context.getString(R.string.error_network)
    
    fun getPermissionErrorMessage(): String = 
        context.getString(R.string.error_permission)
    
    fun getGenericErrorMessage(): String = 
        context.getString(R.string.error_generic)
    
    // Feeding-specific error messages
    fun getNoOngoingBreastFeedingSessionErrorMessage(): String = 
        context.getString(R.string.error_no_ongoing_breast_feeding_session)
    
    fun getNoOngoingBreastFeedingToUpdateErrorMessage(): String = 
        context.getString(R.string.error_no_ongoing_breast_feeding_to_update_time)
    
    fun getFailedToUpdateStartTimeErrorMessage(): String = 
        context.getString(R.string.error_failed_to_update_start_time)
    
    fun getPleaseEnterAmountErrorMessage(): String = 
        context.getString(R.string.error_please_enter_amount)
    
    fun getInvalidAmountNumberErrorMessage(): String = 
        context.getString(R.string.error_invalid_amount_number)
}
