package com.example.data.repository

import android.content.Context
import com.example.data.SampleMusicData
import com.example.data.Song
import com.example.data.local.AppDatabase
import com.example.data.local.FavoriteDao
import com.example.data.local.FavoriteEntity
import com.example.data.local.SongDao
import com.example.data.local.SongEntity
import com.example.data.scanner.AndroidAudioScanner
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

sealed class ScanState {
    object Idle : ScanState()
    object Scanning : ScanState()
    data class Success(val songsCount: Int, val timestamp: Long = System.currentTimeMillis()) : ScanState()
    data class Empty(val message: String = "Kalin no encontró música en tu dispositivo. Agrega algunas canciones y vuelve a intentarlo. 🎵") : ScanState()
    data class PermissionRequired(val message: String) : ScanState()
    data class Error(val error: String) : ScanState()
}

class SongRepository(
    private val songDao: SongDao,
    private val favoriteDao: FavoriteDao
) {
    val allSongs: Flow<List<Song>> = songDao.getAllSongs().map { list ->
        list.map { it.toSong() }
    }

    val favoriteSongs: Flow<List<Song>> = songDao.getFavoriteSongsOrderedByAdded().map { list ->
        list.map { it.toSong() }
    }

    val favoriteSongIds: Flow<List<String>> = favoriteDao.getAllFavoriteSongIds()

    val favoriteCount: Flow<Int> = favoriteDao.getFavoriteCount()

    val recentlyAddedSongs: Flow<List<Song>> = songDao.getRecentlyAddedSongs().map { list ->
        list.map { it.toSong() }
    }

    fun searchSongs(query: String): Flow<List<Song>> {
        return songDao.searchSongs(query).map { list ->
            list.map { it.toSong() }
        }
    }

    suspend fun getSongCount(): Int = withContext(Dispatchers.IO) {
        songDao.getSongCount()
    }

    /**
     * Marca o desmarca una canción en favoritos en la base de datos local Room.
     * Actualiza tanto la tabla de favoritos (con timestamp) como la entidad de la canción.
     */
    suspend fun toggleFavorite(songId: String, isFavorite: Boolean) = withContext(Dispatchers.IO) {
        if (isFavorite) {
            favoriteDao.addFavorite(FavoriteEntity(songId = songId, addedAt = System.currentTimeMillis()))
            songDao.updateFavorite(songId, true)
        } else {
            favoriteDao.removeFavorite(songId)
            songDao.updateFavorite(songId, false)
        }
    }

    suspend fun isSongFavorite(songId: String): Boolean = withContext(Dispatchers.IO) {
        favoriteDao.isFavoriteSync(songId)
    }

    suspend fun clearAllFavorites() = withContext(Dispatchers.IO) {
        favoriteDao.clearAllFavorites()
        val all = songDao.getFavoriteSongIds()
        all.forEach { id ->
            songDao.updateFavorite(id, false)
        }
    }

    suspend fun incrementPlayCount(songId: String) = withContext(Dispatchers.IO) {
        songDao.incrementPlayCount(songId)
    }

    /**
     * Carga canciones iniciales de Distrito 503 en Room si la base de datos está vacía,
     * permitiendo probar inmediatamente la funcionalidad de Favoritos y reproducción.
     */
    suspend fun seedInitialDataIfEmpty(): Int = withContext(Dispatchers.IO) {
        val currentCount = songDao.getSongCount()
        if (currentCount == 0) {
            val entities = SampleMusicData.sampleSongs.map { SongEntity.fromSong(it) }
            songDao.insertAll(entities)

            // Guardar en la tabla de favoritos de Room las pistas marcadas como favoritas de inicio
            entities.filter { it.isFavorite }.forEach { entity ->
                favoriteDao.addFavorite(
                    FavoriteEntity(
                        songId = entity.id,
                        addedAt = System.currentTimeMillis() - (entities.indexOf(entity) * 60_000L)
                    )
                )
            }
            entities.size
        } else {
            currentCount
        }
    }

    /**
     * Escanea el almacenamiento del dispositivo y sincroniza la base de datos Room,
     * preservando intactos los favoritos previamente guardados por el usuario.
     */
    suspend fun scanDeviceLibrary(context: Context): ScanState = withContext(Dispatchers.IO) {
        try {
            val scannedSongs = AndroidAudioScanner.scanDeviceAudio(context)
            if (scannedSongs.isEmpty()) {
                val currentCount = songDao.getSongCount()
                if (currentCount == 0) {
                    // Si no hay canciones físicas en el dispositivo, sembrar el catálogo de Distrito 503
                    val entities = SampleMusicData.sampleSongs.map { SongEntity.fromSong(it) }
                    songDao.insertAll(entities)
                    entities.filter { it.isFavorite }.forEach { entity ->
                        favoriteDao.addFavorite(FavoriteEntity(entity.id, System.currentTimeMillis()))
                    }
                    return@withContext ScanState.Success(entities.size)
                } else {
                    return@withContext ScanState.Success(currentCount)
                }
            }

            val entities = scannedSongs.map { SongEntity.fromSong(it) }
            songDao.syncLibrary(entities)

            // Sincronizar y preservar todos los favoritos registrados en FavoriteDao
            val storedFavoriteIds = favoriteDao.getAllFavoriteSongIdsSync().toSet()
            storedFavoriteIds.forEach { favId ->
                songDao.updateFavorite(favId, true)
            }

            val finalCount = songDao.getSongCount()
            ScanState.Success(finalCount)
        } catch (e: SecurityException) {
            ScanState.PermissionRequired("Se requieren permisos de almacenamiento para acceder a tus canciones.")
        } catch (e: Exception) {
            ScanState.Error(e.localizedMessage ?: "Error desconocido durante el escaneo de música.")
        }
    }

    companion object {
        @Volatile
        private var INSTANCE: SongRepository? = null

        fun getInstance(context: Context): SongRepository {
            return INSTANCE ?: synchronized(this) {
                val db = AppDatabase.getInstance(context)
                val repo = SongRepository(db.songDao(), db.favoriteDao())
                INSTANCE = repo
                repo
            }
        }
    }
}
