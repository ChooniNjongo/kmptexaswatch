package com.jetbrains.spacetutorial.onboarding

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NotificationPrefs(
    val sessionReminders: Boolean = false,
    val appUpdates: Boolean = false,
)

class OnboardingViewModel(
    private val storage: OnboardingStorage,
) : ViewModel() {

    private val _notificationPrefs = MutableStateFlow(NotificationPrefs())
    val notificationPrefs: StateFlow<NotificationPrefs> = _notificationPrefs.asStateFlow()

    fun isOnboardingComplete(): Boolean = storage.isOnboardingComplete()

    fun acceptPrivacyAndProceed() {
        // nothing to persist at this step — privacy accepted means we move on
    }

    fun setNotificationPrefs(prefs: NotificationPrefs) {
        _notificationPrefs.value = prefs
    }

    fun completeOnboarding() {
        storage.setOnboardingComplete(true)
    }
}
