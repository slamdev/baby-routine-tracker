package com.github.slamdev.babyroutinetracker.preferences

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.content.res.Configuration
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.*

/**
 * Manages language preferences for the application.
 * Supports English, Russian, and system default settings.
 */
class LanguagePreferences(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    private val _selectedLanguage = MutableStateFlow(getSelectedLanguage())
    val selectedLanguage: StateFlow<String> = _selectedLanguage.asStateFlow()
    
    companion object {
        private const val PREFS_NAME = "language_prefs"
        private const val KEY_LANGUAGE = "selected_language"
        
        const val LANGUAGE_SYSTEM = "system"
        const val LANGUAGE_ENGLISH = "en"
        const val LANGUAGE_RUSSIAN = "ru"
        
        // Supported languages list for UI
        val SUPPORTED_LANGUAGES = listOf(
            LANGUAGE_SYSTEM,
            LANGUAGE_ENGLISH,
            LANGUAGE_RUSSIAN
        )
    }
    
    /**
     * Get the currently selected language preference
     */
    fun getSelectedLanguage(): String {
        return prefs.getString(KEY_LANGUAGE, LANGUAGE_SYSTEM) ?: LANGUAGE_SYSTEM
    }
    
    /**
     * Set the language preference
     */
    fun setSelectedLanguage(language: String) {
        if (language in SUPPORTED_LANGUAGES && language != getSelectedLanguage()) {
            prefs.edit().putString(KEY_LANGUAGE, language).apply()
            _selectedLanguage.value = language
            
            // Apply the language change with activity recreation
            applyLanguage(language, recreateActivity = true)
        }
    }
    
    /**
     * Get the actual locale to use based on the preference
     */
    fun getLocale(): Locale {
        return when (getSelectedLanguage()) {
            LANGUAGE_ENGLISH -> Locale.ENGLISH
            LANGUAGE_RUSSIAN -> Locale.Builder().setLanguage("ru").build()
            LANGUAGE_SYSTEM -> Locale.getDefault()
            else -> Locale.getDefault()
        }
    }
    
    /**
     * Apply the language setting to the app context
     */
    private fun applyLanguage(languageCode: String, recreateActivity: Boolean = true) {
        val locale = when (languageCode) {
            LANGUAGE_ENGLISH -> Locale.ENGLISH
            LANGUAGE_RUSSIAN -> Locale.Builder().setLanguage("ru").build()
            LANGUAGE_SYSTEM -> Locale.getDefault()
            else -> Locale.getDefault()
        }
        
        // Set the locale as default for the app
        Locale.setDefault(locale)
        
        // Update the app configuration
        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
        }
        
        // Try to find and recreate the current Activity to apply changes immediately
        if (recreateActivity) {
            findActivity(context)?.let { activity ->
                activity.recreate()
            }
        }
    }
    
    /**
     * Find the Activity from a Context
     */
    private fun findActivity(context: Context): Activity? {
        var ctx = context
        while (ctx is ContextWrapper) {
            if (ctx is Activity) {
                return ctx
            }
            ctx = ctx.baseContext
        }
        return null
    }
    
    /**
     * Initialize language settings on app startup
     */
    fun initializeLanguage() {
        // Don't recreate activity during initialization
        applyLanguage(getSelectedLanguage(), recreateActivity = false)
    }
    
    /**
     * Get display name for a language code
     */
    fun getLanguageDisplayName(languageCode: String): String {
        return when (languageCode) {
            LANGUAGE_SYSTEM -> when (Locale.getDefault().language) {
                "ru" -> "Системный"
                else -> "System Default"
            }
            LANGUAGE_ENGLISH -> "English"
            LANGUAGE_RUSSIAN -> "Русский"
            else -> languageCode
        }
    }
    
    /**
     * Check if the current system language is supported
     */
    fun isSystemLanguageSupported(): Boolean {
        val systemLanguage = Locale.getDefault().language
        return systemLanguage in listOf("en", "ru")
    }
}

/**
 * Composable function to observe language changes
 */
@Composable
fun rememberLanguagePreference(context: Context): String {
    val languagePreferences = remember { LanguagePreferences(context) }
    val selectedLanguage by languagePreferences.selectedLanguage.collectAsState()
    return selectedLanguage
}

/**
 * Extension function to provide a singleton instance
 */
private var languagePreferencesInstance: LanguagePreferences? = null

fun Context.getLanguagePreferences(): LanguagePreferences {
    return languagePreferencesInstance ?: LanguagePreferences(this.applicationContext).also {
        languagePreferencesInstance = it
    }
}
