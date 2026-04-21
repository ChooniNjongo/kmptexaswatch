package com.jetbrains.spacetutorial.onboarding

import android.content.Context

class AndroidOnboardingStorage(context: Context) : OnboardingStorage {
    private val prefs = context.getSharedPreferences("onboarding_prefs", Context.MODE_PRIVATE)

    override fun isOnboardingComplete(): Boolean =
        prefs.getBoolean("onboarding_complete", false)

    override fun setOnboardingComplete(value: Boolean) {
        prefs.edit().putBoolean("onboarding_complete", value).apply()
    }
}
