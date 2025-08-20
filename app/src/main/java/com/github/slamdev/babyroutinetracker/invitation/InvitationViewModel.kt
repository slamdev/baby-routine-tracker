package com.github.slamdev.babyroutinetracker.invitation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.slamdev.babyroutinetracker.model.Baby
import com.github.slamdev.babyroutinetracker.model.Invitation
import com.github.slamdev.babyroutinetracker.service.InvitationService
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

data class InvitationUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val invitation: Invitation? = null,
    val baby: Baby? = null,
    val babies: List<Baby> = emptyList(),
    val invitationCode: String = "",
    val babyName: String = "",
    val babyBirthDate: Timestamp = Timestamp.now(),
    val selectedBabyId: String = ""
)

class InvitationViewModel : ViewModel() {
    private val invitationService = InvitationService()
    
    private val _uiState = MutableStateFlow(InvitationUiState())
    val uiState: StateFlow<InvitationUiState> = _uiState.asStateFlow()

    init {
        observeUserBabies()
    }

    /**
     * Observe real-time updates for user's baby profiles
     */
    private fun observeUserBabies() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            
            invitationService.getUserBabiesFlow()
                .catch { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                }
                .collect { result ->
                    result.onSuccess { babies ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            babies = babies,
                            selectedBabyId = if (_uiState.value.selectedBabyId.isEmpty()) {
                                babies.firstOrNull()?.id ?: ""
                            } else {
                                _uiState.value.selectedBabyId
                            }
                        )
                    }.onFailure { error ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            errorMessage = error.message
                        )
                    }
                }
        }
    }

    /**
     * Create a new baby profile
     */
    fun createBabyProfile(name: String, birthDate: Timestamp) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )
            
            invitationService.createBabyProfile(name, birthDate)
                .onSuccess { baby ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        baby = baby,
                        successMessage = "Baby profile created successfully!"
                    )
                    // Real-time listener will automatically update the babies list
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                }
        }
    }

    /**
     * Generate an invitation for the selected baby
     */
    fun generateInvitation(babyId: String? = null) {
        val targetBabyId = babyId ?: _uiState.value.selectedBabyId
        if (targetBabyId.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Please select a baby profile first"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )
            
            invitationService.createInvitation(targetBabyId)
                .onSuccess { invitation ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        invitation = invitation,
                        successMessage = "Invitation created successfully!"
                    )
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                }
        }
    }

    /**
     * Accept an invitation using the provided code
     */
    fun acceptInvitation(invitationCode: String) {
        if (invitationCode.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                errorMessage = "Please enter an invitation code"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null,
                successMessage = null
            )
            
            invitationService.acceptInvitation(invitationCode.uppercase())
                .onSuccess { baby ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        baby = baby,
                        successMessage = "Successfully joined ${baby.name}'s profile!"
                    )
                    // Real-time listener will automatically update the babies list
                }
                .onFailure { error ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = error.message
                    )
                }
        }
    }

    /**
     * Update the invitation code in the UI state
     */
    fun updateInvitationCode(code: String) {
        _uiState.value = _uiState.value.copy(invitationCode = code)
    }

    /**
     * Update baby name in UI state
     */
    fun updateBabyName(name: String) {
        _uiState.value = _uiState.value.copy(babyName = name)
    }

    /**
     * Update baby birth date in UI state
     */
    fun updateBabyBirthDate(birthDate: Timestamp) {
        _uiState.value = _uiState.value.copy(babyBirthDate = birthDate)
    }

    /**
     * Update selected baby ID
     */
    fun updateSelectedBabyId(babyId: String) {
        _uiState.value = _uiState.value.copy(selectedBabyId = babyId)
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }

    /**
     * Clear success message
     */
    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    /**
     * Clear current invitation
     */
    fun clearInvitation() {
        _uiState.value = _uiState.value.copy(invitation = null)
    }

    /**
     * Get share text for invitation
     */
    fun getShareText(invitation: Invitation): String {
        val babyName = _uiState.value.babies.find { it.id == invitation.babyId }?.name ?: "Baby"
        return "You're invited to track $babyName's routine! Use code: ${invitation.invitationCode} in the Baby Routine Tracker app."
    }
}
