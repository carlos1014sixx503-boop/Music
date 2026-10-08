package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.Song

@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey val id: String,
    val title: String,
    val artist: String,
    val album: String,
    val durationSeconds: Int,
    val genre: String,
    val audioFormat: String,
    val fileName: String,
    val filePath: String,
    val contentUri: String,
    val albumArtUri: String?,
    val bitrateKbps: Int?,
    val sampleRateHz: Int?,
    val sizeBytes: Long,
    val folderName: String,
    val folderPath: String,
    val year: Int,
    val dateAdded: Long,
    val isFavorite: Boolean = false,
    val playCount: Int = 0
) {
    fun toSong(): Song {
        return Song(
            id = id,
            title = title,
            artist = artist,
            album = album,
            durationSeconds = durationSeconds,
            genre = genre,
            audioFormat = audioFormat,
            fileName = fileName,
            filePath = filePath,
            contentUri = contentUri,
            albumArtUri = albumArtUri,
            bitrateKbps = bitrateKbps,
            sampleRateHz = sampleRateHz,
            sizeBytes = sizeBytes,
            folderName = folderName,
            folderPath = folderPath,
            year = year,
            dateAdded = dateAdded,
            isFavorite = isFavorite,
            playCount = playCount
        )
    }

    companion object {
        fun fromSong(song: Song): SongEntity {
            return SongEntity(
                id = song.id,
                title = song.title,
                artist = song.artist,
                album = song.album,
                durationSeconds = song.durationSeconds,
                genre = song.genre,
                audioFormat = song.audioFormat,
                fileName = song.fileName,
                filePath = song.filePath,
                contentUri = song.contentUri,
                albumArtUri = song.albumArtUri,
                bitrateKbps = song.bitrateKbps,
                sampleRateHz = song.sampleRateHz,
                sizeBytes = song.sizeBytes,
                folderName = song.folderName,
                folderPath = song.folderPath,
                year = song.year,
                dateAdded = song.dateAdded,
                isFavorite = song.isFavorite,
                playCount = song.playCount
            )
        }
    }
}
