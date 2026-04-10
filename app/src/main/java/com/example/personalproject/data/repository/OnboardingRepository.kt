package com.example.personalproject.data.repository

import android.content.Context

class OnboardingRepository(context: Context) {

    private val prefs = context.getSharedPreferences("kotoba_onboarding", Context.MODE_PRIVATE)

    val introSeen: Boolean get() = prefs.getBoolean("intro_seen", false)

    fun markIntroSeen() {
        prefs.edit().putBoolean("intro_seen", true).apply()
    }

    fun isScreenSeen(screenKey: String): Boolean =
        prefs.getBoolean("screen_$screenKey", false)

    fun markScreenSeen(screenKey: String) {
        prefs.edit().putBoolean("screen_$screenKey", true).apply()
    }
}
