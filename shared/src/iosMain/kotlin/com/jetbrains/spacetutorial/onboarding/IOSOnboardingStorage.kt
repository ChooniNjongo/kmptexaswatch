package com.jetbrains.spacetutorial.onboarding

import platform.Foundation.NSUserDefaults

class IOSOnboardingStorage : OnboardingStorage {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun isOnboardingComplete(): Boolean =
        defaults.boolForKey("onboarding_complete")

    override fun setOnboardingComplete(value: Boolean) {
        defaults.setBool(value, forKey = "onboarding_complete")
    }
}
