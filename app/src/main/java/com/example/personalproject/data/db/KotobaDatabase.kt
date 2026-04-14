package app.kotori.japanese.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL

// Migrations use androidx.sqlite.SQLiteConnection (Room KMP 2.7+ unified API).

internal val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            """CREATE TABLE IF NOT EXISTS `known_items` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `type` TEXT NOT NULL,
                `itemId` TEXT NOT NULL,
                `markedAt` INTEGER NOT NULL
            )"""
        )
        connection.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_known_items_type_itemId` ON `known_items` (`type`, `itemId`)"
        )
    }
}

internal val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL(
            """CREATE TABLE IF NOT EXISTS `kana_stats` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `kana` TEXT NOT NULL,
                `type` TEXT NOT NULL,
                `correct` INTEGER NOT NULL DEFAULT 0,
                `incorrect` INTEGER NOT NULL DEFAULT 0
            )"""
        )
    }
}

internal val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(connection: SQLiteConnection) {
        connection.execSQL("ALTER TABLE known_items ADD COLUMN srsInterval INTEGER NOT NULL DEFAULT 1")
        connection.execSQL("ALTER TABLE known_items ADD COLUMN srsRepetitions INTEGER NOT NULL DEFAULT 0")
        connection.execSQL("ALTER TABLE known_items ADD COLUMN srsEaseFactor REAL NOT NULL DEFAULT 2.5")
        connection.execSQL("ALTER TABLE known_items ADD COLUMN srsNextReview INTEGER NOT NULL DEFAULT 0")
    }
}

@Database(
    entities = [
        SavedItemEntity::class,
        SavedSetEntity::class,
        SavedSetItemEntity::class,
        KnownItemEntity::class,
        KanaStatsEntity::class,
    ],
    version = 4,
    exportSchema = true,
)
abstract class KotobaDatabase : RoomDatabase() {
    abstract fun savedDao(): SavedDao
    abstract fun knownDao(): KnownDao
    abstract fun kanaStatsDao(): KanaStatsDao
}
