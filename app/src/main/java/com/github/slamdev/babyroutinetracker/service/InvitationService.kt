package com.github.slamdev.babyroutinetracker.service

import android.util.Log
import com.github.slamdev.babyroutinetracker.model.Baby
import com.github.slamdev.babyroutinetracker.model.Invitation
import com.github.slamdev.babyroutinetracker.model.InvitationStatus
import com.github.slamdev.babyroutinetracker.model.User
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.UUID

class InvitationService {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "InvitationService"
        private const val USERS_COLLECTION = "users"
        private const val BABIES_COLLECTION = "babies"
        private const val INVITATIONS_COLLECTION = "invitations"
        private const val INVITATION_EXPIRY_DAYS = 7
    }

    /**
     * Create a baby profile for the current user
     */
    suspend fun createBabyProfile(name: String, birthDate: Timestamp): Result<Baby> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("User not authenticated"))

            // First, ensure user document exists
            createOrUpdateUserDocument(currentUser.uid, currentUser.displayName ?: "", currentUser.email ?: "")

            val babyId = UUID.randomUUID().toString()
            val baby = Baby(
                id = babyId,
                name = name,
                birthDate = birthDate,
                parentIds = listOf(currentUser.uid),
                createdAt = Timestamp.now(),
                updatedAt = Timestamp.now()
            )

            firestore.collection(BABIES_COLLECTION)
                .document(babyId)
                .set(baby)
                .await()

            Log.i(TAG, "Baby profile created successfully: ${baby.name}")
            Result.success(baby)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create baby profile: $name", e)
            Result.failure(e)
        }
    }

    /**
     * Generate a unique invitation code and create invitation document
     */
    suspend fun createInvitation(babyId: String): Result<Invitation> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("User not authenticated"))

            // Verify the user is a parent of this baby
            val baby = getBabyProfile(babyId)
                ?: return Result.failure(Exception("Baby profile not found"))

            if (!baby.parentIds.contains(currentUser.uid)) {
                return Result.failure(Exception("You don't have permission to invite for this baby"))
            }

            // Generate unique invitation code
            val invitationCode = generateInvitationCode()
            val invitationId = UUID.randomUUID().toString()

            // Set expiration date (7 days from now)
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, INVITATION_EXPIRY_DAYS)
            val expiresAt = Timestamp(calendar.time)

            val invitation = Invitation(
                id = invitationId,
                babyId = babyId,
                invitedBy = currentUser.uid,
                invitationCode = invitationCode,
                status = InvitationStatus.PENDING.name,
                createdAt = Timestamp.now(),
                expiresAt = expiresAt
            )

            firestore.collection(INVITATIONS_COLLECTION)
                .document(invitationId)
                .set(invitation)
                .await()

            Log.i(TAG, "Invitation created successfully: ${invitation.invitationCode} for baby: ${babyId}")
            Result.success(invitation)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create invitation for baby: ${babyId}", e)
            Result.failure(e)
        }
    }

    /**
     * Accept an invitation using invitation code
     */
    suspend fun acceptInvitation(invitationCode: String): Result<Baby> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("User not authenticated"))

            // Find invitation by code
            val querySnapshot = firestore.collection(INVITATIONS_COLLECTION)
                .whereEqualTo("invitationCode", invitationCode)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                return Result.failure(Exception("Invalid invitation code"))
            }

            val invitationDoc = querySnapshot.documents.first()
            val invitation = invitationDoc.toObject<Invitation>()
                ?: return Result.failure(Exception("Could not parse invitation"))

            // Check if invitation is valid
            if (!invitation.isPending()) {
                return Result.failure(Exception("Invitation is expired or already used"))
            }

            // Use a batch to update documents atomically without reading baby first
            val batch = firestore.batch()
            
            // Add user to baby's parentIds using arrayUnion (doesn't require reading first)
            val babyDocRef = firestore.collection(BABIES_COLLECTION).document(invitation.babyId)
            batch.update(
                babyDocRef,
                mapOf(
                    "parentIds" to FieldValue.arrayUnion(currentUser.uid),
                    "updatedAt" to Timestamp.now()
                )
            )

            // Update invitation status
            batch.update(
                firestore.collection(INVITATIONS_COLLECTION).document(invitation.id),
                "status", InvitationStatus.ACCEPTED.name
            )

            // Ensure user document exists
            batch.set(
                firestore.collection(USERS_COLLECTION).document(currentUser.uid),
                User(
                    id = currentUser.uid,
                    displayName = currentUser.displayName ?: "",
                    email = currentUser.email ?: "",
                    profileImageUrl = currentUser.photoUrl?.toString(),
                    createdAt = Timestamp.now(),
                    lastActiveAt = Timestamp.now()
                )
            )

            // Commit all changes atomically
            batch.commit().await()

            // Now read the baby profile to return it (user should have access after being added)
            val updatedBaby = getBabyProfile(invitation.babyId)
                ?: return Result.failure(Exception("Baby profile not found after update"))

            Log.i(TAG, "Invitation accepted successfully: ${invitationCode} for baby: ${updatedBaby.name}")
            Result.success(updatedBaby)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to accept invitation: $invitationCode", e)
            Result.failure(e)
        }
    }

    /**
     * Get baby profile by ID
     */
    suspend fun getBabyProfile(babyId: String): Baby? {
        return try {
            val document = firestore.collection(BABIES_COLLECTION)
                .document(babyId)
                .get()
                .await()

            document.toObject<Baby>()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get baby profile: $babyId", e)
            null
        }
    }

    /**
     * Get all baby profiles for current user
     */
    suspend fun getUserBabies(): Result<List<Baby>> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(Exception("User not authenticated"))

            val querySnapshot = firestore.collection(BABIES_COLLECTION)
                .whereArrayContains("parentIds", currentUser.uid)
                .get()
                .await()

            val babies = querySnapshot.documents.mapNotNull { it.toObject<Baby>() }
            Log.d(TAG, "Retrieved ${babies.size} baby profiles for current user")
            Result.success(babies)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get baby profiles for current user", e)
            Result.failure(e)
        }
    }

    /**
     * Generate a unique invitation code
     */
    private fun generateInvitationCode(): String {
        // Generate a 6-character alphanumeric code
        val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"
        return (1..6)
            .map { chars.random() }
            .joinToString("")
    }

    /**
     * Create or update user document in Firestore
     */
    private suspend fun createOrUpdateUserDocument(
        userId: String,
        displayName: String,
        email: String
    ) {
        try {
            val user = User(
                id = userId,
                displayName = displayName,
                email = email,
                profileImageUrl = auth.currentUser?.photoUrl?.toString(),
                createdAt = Timestamp.now(),
                lastActiveAt = Timestamp.now()
            )

            firestore.collection(USERS_COLLECTION)
                .document(userId)
                .set(user)
                .await()
            Log.d(TAG, "User document created/updated successfully: $userId")
        } catch (e: Exception) {
            // Log error but don't fail the operation since this is auxiliary
            Log.w(TAG, "Failed to create user document: $userId", e)
        }
    }
}
