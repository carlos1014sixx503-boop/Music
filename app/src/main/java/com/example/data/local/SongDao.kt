package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface SongDao {

    @Query("SELECT * FROM songs ORDER BY title COLLATE NOCASE ASC")
    fun getAllSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE isFavorite = 1 ORDER BY title COLLATE NOCASE ASC")
    fun getFavoriteSongs(): Flow<List<SongEntity>>

    @Query("SELECT s.* FROM songs s INNER JOIN favorites f ON s.id = f.songId ORDER BY f.addedAt DESC")
    fun getFavoriteSongsOrderedByAdded(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE id = :id LIMIT 1")
    suspend fun getSongById(id: String): SongEntity?

    @Query("SELECT * FROM songs ORDER BY dateAdded DESC LIMIT 30")
    fun getRecentlyAddedSongs(): Flow<List<SongEntity>>

    @Query("SELECT * FROM songs WHERE title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%' OR album LIKE '%' || :query || '%' ORDER BY title COLLATE NOCASE ASC")
    fun searchSongs(query: String): Flow<List<SongEntity>>

    @Query("SELECT id FROM songs WHERE isFavorite = 1")
    suspend fun getFavoriteSongIds(): List<String>

    @Query("SELECT id, playCount FROM songs WHERE playCount > 0")
    suspend fun getPlayCounts(): List<PlayCountTuple>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(songs: List<SongEntity>)

    @Query("DELETE FROM songs WHERE id NOT IN (:validIds)")
    suspend fun deleteRemovedSongs(validIds: List<String>)

    @Query("UPDATE songs SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun updateFavorite(id: String, isFavorite: Boolean)

    @Query("UPDATE songs SET playCount = playCount + 1 WHERE id = :id")
    suspend fun incrementPlayCount(id: String)

    @Query("SELECT COUNT(*) FROM songs")
    suspend fun getSongCount(): Int

    @Query("DELETE FROM songs")
    suspend fun clearAll()

    @Transaction
    suspend fun syncLibrary(scannedSongs: List<SongEntity>) {
        if (scannedSongs.isEmpty()) {
            clearAll()
            return
        }

        // Preserve existing user favorites and play counts
        val favoriteIds = getFavoriteSongIds().toSet()
        val playCounts = getPlayCounts().associate { it.id to it.playCount }

        val updatedSongs = scannedSongs.map { song ->
            song.copy(
                isFavorite = favoriteIds.contains(song.id),
                playCount = playCounts[song.id] ?: 0
            )
        }

        insertAll(updatedSongs)
        deleteRemovedSongs(scannedSongs.map { it.id })
    }
}

data class PlayCountTuple(
    val id: String,
    val playCount: Int
)
