package com.example.personalproject.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [SavedItemEntity::class, SavedSetEntity::class, SavedSetItemEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class KotobaDatabase : RoomDatabase() {
    abstract fun savedDao(): SavedDao

    companion object {
        @Volatile private var INSTANCE: KotobaDatabase? = null

        fun getInstance(context: Context): KotobaDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    KotobaDatabase::class.java,
                    "kotoba_db",
                ).build().also { INSTANCE = it }
            }
    }
}
