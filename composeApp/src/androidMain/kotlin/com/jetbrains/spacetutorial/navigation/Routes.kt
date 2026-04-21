package com.jetbrains.spacetutorial.navigation

import kotlinx.serialization.Serializable

// Onboarding routes (shown only on first launch)
@Serializable
data object OnboardingPrivacyRoute

@Serializable
data object OnboardingNotificationsRoute

// Top-level bottom nav destinations
sealed interface TopLevelRoute

@Serializable
data object OffendersRoute : TopLevelRoute

@Serializable
data object SearchRoute

@Serializable
data object MapRoute : TopLevelRoute

@Serializable
data object RouteRoute : TopLevelRoute

@Serializable
data object SettingsRoute : TopLevelRoute

