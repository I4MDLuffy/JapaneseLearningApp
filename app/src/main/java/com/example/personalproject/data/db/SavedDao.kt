package app.kotori.japanese.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedDao {

    // ── Item queries ───────────────────────────────────────────────────────────

    @Query("SELECT * FROM saved_items ORDER BY savedAt DESC")
    fun getAllItems(): Flow<List<SavedItemEntity>>

    @Query("SELECT * FROM saved_items WHERE type = :type ORDER BY savedAt DESC")
    fun getItemsByType(type: String): Flow<List<SavedItemEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_items WHERE type = :type AND itemId = :itemId)")
    fun isItemSaved(type: String, itemId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM saved_items WHERE type = :type AND itemId = :itemId)")
    suspend fun isItemSavedOnce(type: String, itemId: String): Boolean

    @Query("SELECT itemId FROM saved_items WHERE type = :type")
    suspend fun getSavedItemIds(type: String): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertItem(item: SavedItemEntity): Long

    @Query("DELETE FROM saved_items WHERE type = :type AND itemId = :itemId")
    suspend fun deleteItem(type: String, itemId: String)

    // ── Set queries ────────────────────────────────────────────────────────────

    @Query("SELECT * FROM saved_sets ORDER BY createdAt DESC")
    fun getAllSets(): Flow<List<SavedSetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSet(set: SavedSetEntity): Long

    @Query("DELETE FROM saved_sets WHERE id = :setId")
    suspend fun deleteSet(setId: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addItemToSet(item: SavedSetItemEntity)

    @Query("DELETE FROM saved_set_items WHERE setId = :setId AND savedItemId = :savedItemId")
    suspend fun removeItemFromSet(setId: Long, savedItemId: Long)

    @Query("""
        SELECT si.* FROM saved_items si
        INNER JOIN saved_set_items ssi ON si.id = ssi.savedItemId
        WHERE ssi.setId = :setId
        ORDER BY si.savedAt DESC
    """)
    fun getItemsForSet(setId: Long): Flow<List<SavedItemEntity>>
}
