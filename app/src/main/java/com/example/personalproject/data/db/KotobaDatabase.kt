package com.example.personalproject.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

private val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE IF NOT EXISTS `known_items` (
                `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                `type` TEXT NOT NULL,
                `itemId` TEXT NOT NULL,
                `markedAt` INTEGER NOT NULL
            )"""
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_known_items_type_itemId` ON `known_items` (`type`, `itemId`)"
        )
    }
}

private val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
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

/** Adds SRS columns to known_items for spaced-repetition review scheduling. */
private val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE known_items ADD COLUMN srsInterval INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE known_items ADD COLUMN srsRepetitions INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE known_items ADD COLUMN srsEaseFactor REAL NOT NULL DEFAULT 2.5")
        db.execSQL("ALTER TABLE known_items ADD COLUMN srsNextReview INTEGER NOT NULL DEFAULT 0")
    }
}

@Database(
    entities = [SavedItemEntity::class, SavedSetEntity::class, SavedSetItemEntity::class, KnownItemEntity::class, KanaStatsEntity::class],
    version = 4,
    exportSchema = false,
)
abstract class KotobaDatabase : RoomDatabase() {
    abstract fun savedDao(): SavedDao
    abstract fun knownDao(): KnownDao
    abstract fun kanaStatsDao(): KanaStatsDao

    companion object {
        @Volatile private var INSTANCE: KotobaDatabase? = null

        fun getInstance(context: Context): KotobaDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    KotobaDatabase::class.java,
                    "kotoba_db",
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .build().also { INSTANCE = it }
            }
    }
}
