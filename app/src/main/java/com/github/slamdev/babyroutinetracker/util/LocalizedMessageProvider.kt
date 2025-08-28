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
    
    fun getShareInvitationMessage(babyName: String, invitationCode: String): String = 
        context.getString(R.string.share_invitation_message, babyName, invitationCode)
    
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

    // Sleep-specific error messages
    fun getNoOngoingSleepSessionErrorMessage(): String = 
        context.getString(R.string.error_no_ongoing_sleep_session)
    
    fun getNoOngoingSleepToUpdateErrorMessage(): String = 
        context.getString(R.string.error_no_ongoing_sleep_to_update)
    
    fun getSleepActivityUpdatedSuccessMessage(): String = 
        context.getString(R.string.success_sleep_activity_updated)

    fun getFailedToUpdateSleepActivityErrorMessage(): String = 
        context.getString(R.string.error_failed_to_update_sleep_activity)

    // Common error messages
    fun getNoBabyProfileSelectedErrorMessage(): String = 
        context.getString(R.string.error_no_baby_profile_selected)
    
    fun getUnexpectedErrorMessage(errorMessage: String): String = 
        context.getString(R.string.error_unexpected_error, errorMessage)

    // Test notification status messages  
    fun getSendingTestNotificationMessage(): String = 
        context.getString(R.string.status_sending_test_notification)
    
    fun getTestNotificationFailedMessage(errorMessage: String): String = 
        context.getString(R.string.error_test_notification_failed, errorMessage)
    
    fun getSavePreferencesFailedMessage(errorMessage: String): String = 
        context.getString(R.string.error_save_preferences_failed, errorMessage)

    // Notification settings error messages
    fun getSavePreferencesFailedErrorMessage(details: String): String =
        context.getString(R.string.error_save_preferences_failed, details)

    fun getTestNotificationFailedErrorMessage(details: String): String =
        context.getString(R.string.error_test_notification_failed, details)

    fun getSendingTestNotificationStatusMessage(): String =
        context.getString(R.string.status_sending_test_notification)

    // Data visualization error messages
    fun getUnableToLoadActivityDataErrorMessage(): String =
        context.getString(R.string.error_unable_to_load_activity_data)

    fun getFailedToProcessActivityDataErrorMessage(): String =
        context.getString(R.string.error_failed_to_process_activity_data)

    // Activity history error messages
    fun getFailedToUpdateActivityTimeErrorMessage(): String =
        context.getString(R.string.error_failed_to_update_activity_time)

    fun getFailedToUpdateActivityNotesErrorMessage(): String =
        context.getString(R.string.error_failed_to_update_activity_notes)

    fun getFailedToDeleteActivityErrorMessage(): String =
        context.getString(R.string.error_failed_to_delete_activity)

    // Account deletion error messages
    fun getAccountDeletionUserNotAuthenticatedErrorMessage(): String =
        context.getString(R.string.error_account_deletion_user_not_authenticated)

    fun getAccountDeletionNetworkErrorMessage(): String =
        context.getString(R.string.error_account_deletion_network)

    fun getAccountDeletionPermissionErrorMessage(): String =
        context.getString(R.string.error_account_deletion_permission)

    fun getAccountDeletionGenericErrorMessage(): String =
        context.getString(R.string.error_account_deletion_generic)

    fun getAccountDeletionUnexpectedErrorMessage(): String =
        context.getString(R.string.error_account_deletion_unexpected)
    
    // Default fallback names
    fun getDefaultBabyName(): String = 
        context.getString(R.string.default_baby_name)
    
    fun getDefaultPartnerName(): String = 
        context.getString(R.string.default_partner_name)

    // Service Layer Messages (for classes without UI context)
    fun getNoOngoingBreastFeedingToUpdateMessage(): String {
        return context.getString(R.string.error_no_ongoing_breast_feeding_to_update)
    }
    
    fun getFailedToUpdateStartTimeMessage(): String {
        return context.getString(R.string.error_failed_to_update_start_time)
    }
    
    fun getFailedToUpdateFeedingMessage(): String {
        return context.getString(R.string.error_failed_to_update_feeding)
    }
    
    fun getNoBabyProfileSelectedMessage(): String {
        return context.getString(R.string.error_no_baby_profile_selected)
    }
    
    fun getPleaseSignInToViewPreferencesMessage(): String {
        return context.getString(R.string.error_please_sign_in_to_view_preferences)
    }
    
    fun getNoPermissionToViewPreferencesMessage(): String {
        return context.getString(R.string.error_no_permission_to_view_preferences)
    }
    
    fun getUnableToLoadNotificationPreferencesMessage(): String {
        return context.getString(R.string.error_unable_to_load_notification_preferences)
    }
    
    fun getFailedToProcessNotificationPreferencesMessage(): String {
        return context.getString(R.string.error_failed_to_process_notification_preferences)
    }
    
    fun getUnableToConnectToServerMessage(): String {
        return context.getString(R.string.error_unable_to_connect_to_server)
    }
    
    // Partner Notification Messages
    fun getActivityStartedSleepMessage(): String {
        return context.getString(R.string.notification_started_sleep)
    }
    
    fun getActivityEndedSleepMessage(): String {
        return context.getString(R.string.notification_ended_sleep)
    }
    
    fun getActivityStartedBreastFeedingMessage(): String {
        return context.getString(R.string.notification_started_breast_feeding)
    }
    
    fun getActivityFinishedBreastFeedingMessage(): String {
        return context.getString(R.string.notification_finished_breast_feeding)
    }
    
    fun getActivityLoggedFeedingMessage(): String {
        return context.getString(R.string.notification_logged_feeding)
    }
    
    fun getActivityChangedDiaperMessage(): String {
        return context.getString(R.string.notification_changed_diaper)
    }
    
    fun getNewActivityNotificationTitle(babyName: String): String {
        return context.getString(R.string.notification_new_activity_title, babyName)
    }
    
    fun getPartnerActivityNotificationBody(partnerName: String, activityDescription: String): String {
        return context.getString(R.string.notification_partner_activity_body, partnerName, activityDescription)
    }
    
    fun getGaveBottleNotification(amount: Int): String {
        return context.getString(R.string.notification_gave_bottle, amount)
    }
    
    fun getTestNotificationTitle(): String {
        return context.getString(R.string.notification_test_title)
    }
    
    fun getTestNotificationBody(): String {
        return context.getString(R.string.notification_test_body)
    }
    
    fun getUserNotAuthenticatedMessage(): String {
        return context.getString(R.string.error_user_not_authenticated)
    }
    
    fun getNoPartnersFoundToNotifyMessage(): String {
        return context.getString(R.string.error_no_partners_found_to_notify)
    }
    
    // Time Formatting Messages
    fun getHappenedNowMessage(): String {
        return context.getString(R.string.happened_now)
    }
    
    fun getHappenedMinutesAgoMessage(minutes: Long): String {
        return context.getString(R.string.happened_minutes_ago, minutes)
    }
    
    fun getHappenedHoursAgoMessage(hours: Long): String {
        return context.getString(R.string.happened_hours_ago, hours)
    }
    
    fun getHappenedHoursMinutesAgoMessage(hours: Long, minutes: Long): String {
        return context.getString(R.string.happened_hours_minutes_ago, hours, minutes)
    }
    
    fun getHappenedDaysAgoMessage(days: Long): String {
        return context.getString(R.string.happened_days_ago, days)
    }
    
    fun getHappenedWeeksAgoMessage(weeks: Long): String {
        return context.getString(R.string.happened_weeks_ago, weeks)
    }
}
