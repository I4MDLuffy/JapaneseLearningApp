package app.kotori.japanese.data.repository

import app.kotori.japanese.data.db.KotobaDatabase
import app.kotori.japanese.data.db.SavedDao
import app.kotori.japanese.data.db.SavedItemEntity
import app.kotori.japanese.data.db.SavedSetEntity
import app.kotori.japanese.data.db.SavedSetItemEntity
import kotlinx.coroutines.flow.Flow

class SavedRepository(private val dao: SavedDao) {

    companion object {
        fun create(db: KotobaDatabase): SavedRepository = SavedRepository(db.savedDao())
    }

    // ── Item state ─────────────────────────────────────────────────────────────

    /** Reactive saved state for a specific item — use in detail screens via collectAsStateWithLifecycle. */
    fun isItemSavedFlow(type: String, itemId: String): Flow<Boolean> =
        dao.isItemSaved(type, itemId)

    /** One-shot check inside a coroutine. */
    suspend fun isSaved(type: String, itemId: String): Boolean =
        dao.isItemSavedOnce(type, itemId)

    /**
     * Toggle saved state. Saves with full metadata on first save.
     * Returns true if the item is now saved.
     */
    suspend fun toggle(
        type: String,
        itemId: String,
        title: String,
        reading: String = "",
        meaning: String = "",
    ): Boolean {
        return if (dao.isItemSavedOnce(type, itemId)) {
            dao.deleteItem(type, itemId)
            false
        } else {
            dao.insertItem(
                SavedItemEntity(
                    type = type,
                    itemId = itemId,
                    title = title,
                    reading = reading,
                    meaning = meaning,
                )
            )
            true
        }
    }

    // ── Item retrieval ─────────────────────────────────────────────────────────

    fun getAllItems(): Flow<List<SavedItemEntity>> = dao.getAllItems()

    fun getItemsByType(type: String): Flow<List<SavedItemEntity>> = dao.getItemsByType(type)

    /** Returns saved item IDs for a given type — for use in game/study loading. */
    suspend fun getSavedItemIds(type: String): Set<String> =
        dao.getSavedItemIds(type).toSet()

    // ── Set management ─────────────────────────────────────────────────────────

    fun getAllSets(): Flow<List<SavedSetEntity>> = dao.getAllSets()

    suspend fun createSet(name: String, description: String = ""): Long =
        dao.insertSet(SavedSetEntity(name = name, description = description))

    suspend fun deleteSet(setId: Long) = dao.deleteSet(setId)

    suspend fun addItemToSet(setId: Long, savedItemId: Long) =
        dao.addItemToSet(SavedSetItemEntity(setId = setId, savedItemId = savedItemId))

    suspend fun removeItemFromSet(setId: Long, savedItemId: Long) =
        dao.removeItemFromSet(setId, savedItemId)

    fun getItemsForSet(setId: Long): Flow<List<SavedItemEntity>> =
        dao.getItemsForSet(setId)
}
