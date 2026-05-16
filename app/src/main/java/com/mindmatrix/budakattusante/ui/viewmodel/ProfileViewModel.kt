package com.mindmatrix.budakattusante.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.mindmatrix.budakattusante.data.local.dao.UserProfileDao
import com.mindmatrix.budakattusante.data.local.entity.toEntity
import com.mindmatrix.budakattusante.data.local.entity.toModel
import com.mindmatrix.budakattusante.data.model.UserProfile
import com.mindmatrix.budakattusante.data.remote.FirebaseGateway
import com.mindmatrix.budakattusante.ui.screens.UserRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Requirement 1: Profile management with offline support.
 */
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val firebaseGateway: FirebaseGateway,
    private val userProfileDao: UserProfileDao
) : ViewModel() {

    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile: StateFlow<UserProfile?> = _userProfile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadProfile() {
        val userId = Firebase.auth.currentUser?.uid ?: return
        viewModelScope.launch {
            // Requirement 10: Load from local cache first
            val cachedProfile = userProfileDao.getProfile(userId)
            if (cachedProfile != null) {
                _userProfile.value = cachedProfile.toModel()
            }

            _isLoading.value = true
            try {
                val remoteProfile = firebaseGateway.getUserProfile(userId)
                if (remoteProfile != null) {
                    _userProfile.value = remoteProfile
                    userProfileDao.insertProfile(remoteProfile.toEntity())
                } else if (_userProfile.value == null) {
                    // Create default profile if not exists anywhere
                    val user = Firebase.auth.currentUser
                    val newProfile = UserProfile(
                        userId = userId,
                        name = user?.displayName ?: "Tribal User",
                        email = user?.email ?: "",
                        phoneNumber = user?.phoneNumber ?: ""
                    )
                    _userProfile.value = newProfile
                    firebaseGateway.saveUserProfile(newProfile)
                    userProfileDao.insertProfile(newProfile.toEntity())
                }
            } catch (e: Exception) {
                // Offline fallback - handled by initial cache load
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateProfile(profile: UserProfile) {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                userProfileDao.insertProfile(profile.toEntity())
                _userProfile.value = profile
                firebaseGateway.saveUserProfile(profile)
            } catch (e: Exception) {
                // Background sync will handle remote update later if offline
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateRole(role: UserRole) {
        val current = _userProfile.value ?: return
        updateProfile(current.copy(role = role.name))
    }
}
