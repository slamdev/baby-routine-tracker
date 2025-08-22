package com.github.slamdev.babyroutinetracker.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude
import java.util.*
import kotlin.math.abs

data class Baby(
    val id: String = "",
    val name: String = "",
    val birthDate: Timestamp = Timestamp.now(),
    val dueDate: Timestamp? = null,
    val parentIds: List<String> = emptyList(),
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
) {
    /**
     * Calculate real age from birth date
     */
    @Exclude
    fun getRealAge(): AgeInfo {
        return calculateAge(birthDate.toDate())
    }
    
    /**
     * Calculate adjusted age from due date (if available)
     */
    @Exclude
    fun getAdjustedAge(): AgeInfo? {
        return dueDate?.let { calculateAge(it.toDate()) }
    }
    
    /**
     * Get formatted age display for real age
     */
    @Exclude
    fun getFormattedRealAge(): String {
        return formatAge(getRealAge())
    }
    
    /**
     * Get formatted age display for adjusted age (corrected age)
     */
    @Exclude
    fun getFormattedAdjustedAge(): String? {
        return getAdjustedAge()?.let { formatAge(it) }
    }
    
    /**
     * Check if baby was born early (before due date)
     */
    @Exclude
    fun wasBornEarly(): Boolean {
        return dueDate?.let { due ->
            birthDate.toDate().before(due.toDate())
        } ?: false
    }
    
    /**
     * Get gestation period in weeks if born early
     */
    @Exclude
    fun getGestationWeeks(): Int? {
        return dueDate?.let { due ->
            val diffMs = due.seconds * 1000 - birthDate.seconds * 1000
            val diffWeeks = diffMs / (7 * 24 * 60 * 60 * 1000)
            40 - diffWeeks.toInt() // Assuming 40 weeks full term
        }
    }
    
    @Exclude
    private fun calculateAge(fromDate: Date): AgeInfo {
        val now = Date()
        val diffMs = abs(now.time - fromDate.time)
        
        val diffDays = diffMs / (24 * 60 * 60 * 1000)
        val diffWeeks = diffDays / 7
        val diffMonths = diffDays / 30 // Approximate
        
        return AgeInfo(
            days = diffDays.toInt(),
            weeks = diffWeeks.toInt(),
            months = diffMonths.toInt()
        )
    }
    
    @Exclude
    private fun formatAge(ageInfo: AgeInfo): String {
        return when {
            ageInfo.days < 7 -> "${ageInfo.days} day${if (ageInfo.days != 1) "s" else ""} old"
            ageInfo.days < 60 -> "${ageInfo.weeks} week${if (ageInfo.weeks != 1) "s" else ""} old"
            else -> "${ageInfo.months} month${if (ageInfo.months != 1) "s" else ""} old"
        }
    }
}

data class AgeInfo(
    val days: Int,
    val weeks: Int,
    val months: Int
)
