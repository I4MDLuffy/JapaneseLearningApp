package com.example.personalproject.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "saved_set_items",
    primaryKeys = ["setId", "savedItemId"],
    foreignKeys = [
        ForeignKey(
            entity = SavedSetEntity::class,
            parentColumns = ["id"],
            childColumns = ["setId"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = SavedItemEntity::class,
            parentColumns = ["id"],
            childColumns = ["savedItemId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("savedItemId")],
)
data class SavedSetItemEntity(
    val setId: Long,
    val savedItemId: Long,
)
