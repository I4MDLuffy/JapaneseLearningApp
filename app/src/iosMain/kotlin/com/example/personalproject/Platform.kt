package app.kotori.japanese

import androidx.room.Room
import androidx.room.RoomDatabase
import app.kotori.japanese.data.db.KotobaDatabase
import app.kotori.japanese.data.db.MIGRATION_1_2
import app.kotori.japanese.data.db.MIGRATION_2_3
import app.kotori.japanese.data.db.MIGRATION_3_4
import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDefaults
import platform.Foundation.NSUserDomainMask

private fun dbPath(): String {
    val urls = NSFileManager.defaultManager.URLsForDirectory(
        directory = NSDocumentDirectory,
        inDomains = NSUserDomainMask,
    )
    val dir = (urls.firstOrNull() as? platform.Foundation.NSURL)?.path ?: ""
    return "$dir/kotoba_db.db"
}

private var dbInstance: KotobaDatabase? = null

internal actual fun createDatabase(): KotobaDatabase =
    dbInstance ?: Room.databaseBuilder<KotobaDatabase>(name = dbPath())
        .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
        .build()
        .also { dbInstance = it }

internal actual fun createSettings(name: String): Settings =
    NSUserDefaultsSettings(NSUserDefaults(suiteName = name)!!)
