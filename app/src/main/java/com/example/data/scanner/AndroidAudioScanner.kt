package com.example.data.scanner

import android.content.ContentUris
import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import com.example.data.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

object AndroidAudioScanner {

    /**
     * Escanea los archivos de música reales del almacenamiento del dispositivo.
     */
    suspend fun scanDeviceAudio(context: Context): List<Song> = withContext(Dispatchers.IO) {
        val songsList = mutableListOf<Song>()
        val contentResolver = context.contentResolver

        val projection = mutableListOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.MIME_TYPE,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.ALBUM_ID
        )

        // Columnas opcionales en versiones modernas de Android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            projection.add(MediaStore.Audio.Media.GENRE)
            projection.add(MediaStore.Audio.Media.BITRATE)
        }

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= 5000"
        val sortOrder = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        val collectionUri: Uri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
        } else {
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        }

        try {
            contentResolver.query(
                collectionUri,
                projection.toTypedArray(),
                selection,
                null,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                val dataCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
                val displayNameCol = cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME)
                val mimeTypeCol = cursor.getColumnIndex(MediaStore.Audio.Media.MIME_TYPE)
                val sizeCol = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE)
                val dateAddedCol = cursor.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED)
                val yearCol = cursor.getColumnIndex(MediaStore.Audio.Media.YEAR)
                val albumIdCol = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)

                val genreCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    cursor.getColumnIndex(MediaStore.Audio.Media.GENRE)
                } else -1

                val bitrateCol = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    cursor.getColumnIndex(MediaStore.Audio.Media.BITRATE)
                } else -1

                while (cursor.moveToNext()) {
                    val mediaId = cursor.getLong(idCol)
                    val rawTitle = cursor.getString(titleCol) ?: ""
                    val rawArtist = cursor.getString(artistCol) ?: "<Desconocido>"
                    val rawAlbum = cursor.getString(albumCol) ?: "<Desconocido>"
                    val durationMs = cursor.getInt(durationCol)
                    val durationSeconds = if (durationMs > 0) (durationMs / 1000) else 0

                    val filePath = if (dataCol >= 0) cursor.getString(dataCol) ?: "" else ""
                    val displayName = if (displayNameCol >= 0) cursor.getString(displayNameCol) ?: "" else ""
                    val mimeType = if (mimeTypeCol >= 0) cursor.getString(mimeTypeCol) ?: "" else ""
                    val sizeBytes = if (sizeCol >= 0) cursor.getLong(sizeCol) else 0L
                    val dateAdded = if (dateAddedCol >= 0) cursor.getLong(dateAddedCol) else 0L
                    val year = if (yearCol >= 0) cursor.getInt(yearCol) else 2024
                    val albumId = if (albumIdCol >= 0) cursor.getLong(albumIdCol) else -1L

                    var rawGenre: String? = if (genreCol >= 0) cursor.getString(genreCol) else null
                    var rawBitrate: Int? = if (bitrateCol >= 0) cursor.getInt(bitrateCol).takeIf { it > 0 } else null

                    // Uri de contenido del audio
                    val contentUri = ContentUris.withAppendedId(collectionUri, mediaId).toString()

                    // Portada del álbum
                    val albumArtUri = if (albumId > 0) {
                        ContentUris.withAppendedId(
                            Uri.parse("content://media/external/audio/albumart"),
                            albumId
                        ).toString()
                    } else null

                    // Carpeta contenedora
                    val fileObj = if (filePath.isNotEmpty()) File(filePath) else null
                    val fileName = if (displayName.isNotBlank()) displayName else fileObj?.name ?: "Pista ${mediaId}"
                    val folderObj = fileObj?.parentFile
                    val folderName = folderObj?.name ?: "Almacenamiento"
                    val folderPath = folderObj?.absolutePath ?: ""

                    // Título limpio
                    val cleanTitle = if (rawTitle.isNotBlank()) {
                        rawTitle
                    } else {
                        fileName.substringBeforeLast(".")
                    }

                    val cleanArtist = if (rawArtist.isBlank() || rawArtist.equals("<unknown>", ignoreCase = true)) {
                        "Artista Desconocido"
                    } else rawArtist

                    val cleanAlbum = if (rawAlbum.isBlank() || rawAlbum.equals("<unknown>", ignoreCase = true)) {
                        "Álbum Desconocido"
                    } else rawAlbum

                    // Formato de audio legible
                    val extension = fileName.substringAfterLast(".", "").uppercase()
                    val formatLabel = when {
                        extension.isNotBlank() -> extension
                        mimeType.contains("flac", ignoreCase = true) -> "FLAC"
                        mimeType.contains("mpeg", ignoreCase = true) || mimeType.contains("mp3", ignoreCase = true) -> "MP3"
                        mimeType.contains("wav", ignoreCase = true) -> "WAV"
                        mimeType.contains("aac", ignoreCase = true) || mimeType.contains("mp4", ignoreCase = true) -> "AAC"
                        mimeType.contains("ogg", ignoreCase = true) -> "OGG"
                        else -> "AUDIO"
                    }

                    var sampleRateHz: Int? = null

                    // Si no tenemos bitrate o género, intentamos una lectura ligera con MediaMetadataRetriever
                    if ((rawBitrate == null || rawGenre == null) && filePath.isNotEmpty()) {
                        try {
                            val retriever = MediaMetadataRetriever()
                            retriever.setDataSource(filePath)
                            if (rawGenre == null) {
                                rawGenre = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_GENRE)
                            }
                            if (rawBitrate == null) {
                                rawBitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toIntOrNull()
                            }
                            sampleRateHz = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_SAMPLERATE)?.toIntOrNull()
                            } else null
                            retriever.release()
                        } catch (_: Exception) {
                            // Continuar sin metadatos extendidos si el archivo está protegido
                        }
                    }

                    val bitrateKbps = if (rawBitrate != null && rawBitrate > 0) {
                        rawBitrate / 1000
                    } else null

                    val audioFormatString = buildString {
                        append(formatLabel)
                        if (bitrateKbps != null && bitrateKbps > 0) {
                            append(" • ${bitrateKbps} kbps")
                        }
                        if (sampleRateHz != null && sampleRateHz > 0) {
                            append(" / ${(sampleRateHz / 1000.0)}kHz")
                        }
                    }

                    songsList.add(
                        Song(
                            id = mediaId.toString(),
                            title = cleanTitle,
                            artist = cleanArtist,
                            album = cleanAlbum,
                            durationSeconds = durationSeconds,
                            albumArtUri = albumArtUri,
                            genre = if (!rawGenre.isNullOrBlank()) rawGenre else "Música",
                            audioFormat = audioFormatString,
                            isFavorite = false,
                            folderName = folderName,
                            folderPath = folderPath,
                            fileName = fileName,
                            filePath = filePath,
                            contentUri = contentUri,
                            bitrateKbps = bitrateKbps,
                            sampleRateHz = sampleRateHz,
                            sizeBytes = sizeBytes,
                            year = if (year > 1900) year else 2024,
                            dateAdded = dateAdded
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        songsList
    }
}
