package com.jetbrains.spacetutorial.onboarding

interface OnboardingStorage {
    fun isOnboardingComplete(): Boolean
    fun setOnboardingComplete(value: Boolean)
}
