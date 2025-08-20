package com.github.slamdev.babyroutinetracker.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.Exclude

enum class InvitationStatus {
    PENDING, ACCEPTED, EXPIRED
}

data class Invitation(
    val id: String = "",
    val babyId: String = "",
    val invitedBy: String = "",
    val invitationCode: String = "",
    val status: String = InvitationStatus.PENDING.name,
    val createdAt: Timestamp = Timestamp.now(),
    val expiresAt: Timestamp = Timestamp.now()
) {
    @Exclude
    fun getStatusEnum(): InvitationStatus {
        return try {
            InvitationStatus.valueOf(status)
        } catch (e: IllegalArgumentException) {
            InvitationStatus.PENDING
        }
    }
    
    @Exclude
    fun isExpired(): Boolean {
        return expiresAt.toDate().before(java.util.Date()) ||
                getStatusEnum() == InvitationStatus.EXPIRED
    }
    
    @Exclude
    fun isPending(): Boolean {
        return getStatusEnum() == InvitationStatus.PENDING && !isExpired()
    }
}
