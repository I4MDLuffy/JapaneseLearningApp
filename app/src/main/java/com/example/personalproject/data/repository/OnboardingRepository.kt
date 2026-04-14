package app.kotori.japanese.data.repository

import com.russhwolf.settings.Settings
import com.russhwolf.settings.get
import com.russhwolf.settings.set

class OnboardingRepository(private val prefs: Settings) {

    val introSeen: Boolean get() = prefs["intro_seen", false]

    fun markIntroSeen() {
        prefs["intro_seen"] = true
    }

    fun isScreenSeen(screenKey: String): Boolean =
        prefs["screen_$screenKey", false]

    fun markScreenSeen(screenKey: String) {
        prefs["screen_$screenKey"] = true
    }
}
