package app.kotori.japanese

import app.kotori.japanese.data.db.KotobaDatabase
import com.russhwolf.settings.Settings

/**
 * Platform-specific database factory.
 * Android: uses Room.databaseBuilder with Context.
 * iOS: uses Room.databaseBuilder with a file-system path.
 */
internal expect fun createDatabase(): KotobaDatabase

/**
 * Platform-specific key-value settings store.
 * Android: SharedPreferences. iOS: NSUserDefaults.
 */
internal expect fun createSettings(name: String): Settings
