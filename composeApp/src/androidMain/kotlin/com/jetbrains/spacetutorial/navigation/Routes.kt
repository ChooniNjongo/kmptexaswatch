package com.jetbrains.spacetutorial.navigation

import kotlinx.serialization.Serializable

// Top-level bottom nav destinations
sealed interface TopLevelRoute

@Serializable
data object OffendersRoute : TopLevelRoute

@Serializable
data object MapRoute : TopLevelRoute

@Serializable
data object SettingsRoute : TopLevelRoute

