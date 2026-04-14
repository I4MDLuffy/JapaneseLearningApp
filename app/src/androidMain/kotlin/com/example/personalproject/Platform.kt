package app.kotori.japanese

import android.content.Context
import androidx.room.Room
import app.kotori.japanese.data.db.KotobaDatabase
import app.kotori.japanese.data.db.MIGRATION_1_2
import app.kotori.japanese.data.db.MIGRATION_2_3
import app.kotori.japanese.data.db.MIGRATION_3_4
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings

/**
 * Holds the Android application context. Initialised once in MainActivity.onCreate()
 * before any repository is accessed.
 */
internal object AndroidPlatform {
    lateinit var context: Context
        private set

    fun init(context: Context) {
        this.context = context.applicationContext
    }
}

private var dbInstance: KotobaDatabase? = null

internal actual fun createDatabase(): KotobaDatabase =
    dbInstance ?: synchronized(KotobaDatabase::class) {
        dbInstance ?: Room.databaseBuilder<KotobaDatabase>(
            context = AndroidPlatform.context,
            name = AndroidPlatform.context.getDatabasePath("kotoba_db.db").absolutePath,
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
            .build()
            .also { dbInstance = it }
    }

internal actual fun createSettings(name: String): Settings =
    SharedPreferencesSettings(
        AndroidPlatform.context.getSharedPreferences(name, Context.MODE_PRIVATE),
    )
