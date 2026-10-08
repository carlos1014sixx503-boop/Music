package com.example.data

import com.example.R

object SampleMusicData {

    val sampleSongs = listOf(
        Song(
            id = "song_1",
            title = "Noche en San Salvador",
            artist = "Distrito 503 Project",
            album = "Luces del Volcán",
            durationSeconds = 214,
            coverResId = R.drawable.cover_synthwave,
            genre = "Synthwave / Cyberpunk",
            audioFormat = "FLAC • 24-bit / 96kHz",
            isFavorite = true,
            folderName = "Distrito503 / Master Audio",
            playCount = 42,
            year = 2024
        ),
        Song(
            id = "song_2",
            title = "Ritmo de la Calle Real",
            artist = "Carlos Martínez",
            album = "Raíces Urbanas",
            durationSeconds = 188,
            coverResId = R.drawable.cover_urban,
            genre = "Latin Urban",
            audioFormat = "WAV • 32-bit Float / 48kHz",
            isFavorite = true,
            folderName = "Descargas / Urban Beats",
            playCount = 35,
            year = 2024
        ),
        Song(
            id = "song_3",
            title = "Galaxia 503",
            artist = "Nebula Dreams",
            album = "Horizontes Infinitos",
            durationSeconds = 265,
            coverResId = R.drawable.cover_space,
            genre = "Ambient / Space",
            audioFormat = "DSD64 • 2.8MHz",
            isFavorite = false,
            folderName = "Hi-Res / Audiophile",
            playCount = 19,
            year = 2023
        ),
        Song(
            id = "song_4",
            title = "Sueños de Cristal",
            artist = "Distrito 503 Project",
            album = "Luces del Volcán",
            durationSeconds = 202,
            coverResId = R.drawable.cover_synthwave,
            genre = "Synthwave / Cyberpunk",
            audioFormat = "FLAC • 24-bit / 192kHz",
            isFavorite = true,
            folderName = "Distrito503 / Master Audio",
            playCount = 28,
            year = 2024
        ),
        Song(
            id = "song_5",
            title = "Bajo el Cielo Añil",
            artist = "Sonsonate Sound",
            album = "Costa del Sol Vibes",
            durationSeconds = 230,
            coverResId = R.drawable.cover_latin_flaka,
            genre = "Electro Pop",
            audioFormat = "MP3 • 320 kbps CBR",
            isFavorite = false,
            folderName = "Música / Verano 503",
            playCount = 14,
            year = 2024
        ),
        Song(
            id = "song_6",
            title = "Frecuencia Urbana",
            artist = "Carlos Martínez",
            album = "Raíces Urbanas",
            durationSeconds = 175,
            coverResId = R.drawable.cover_urban,
            genre = "Latin Urban",
            audioFormat = "FLAC • 16-bit / 44.1kHz",
            isFavorite = false,
            folderName = "Descargas / Urban Beats",
            playCount = 22,
            year = 2024
        ),
        Song(
            id = "song_7",
            title = "Ecos de la Nebulosa",
            artist = "Nebula Dreams",
            album = "Horizontes Infinitos",
            durationSeconds = 290,
            coverResId = R.drawable.cover_neon_eye,
            genre = "Ambient / Space",
            audioFormat = "FLAC • 24-bit / 96kHz",
            isFavorite = true,
            folderName = "Hi-Res / Audiophile",
            playCount = 31,
            year = 2023
        ),
        Song(
            id = "song_8",
            title = "Medianoche en El Cafetal",
            artist = "Luna & Tradición",
            album = "Noches Tropicales",
            durationSeconds = 198,
            coverResId = R.drawable.distrito_logo,
            genre = "Acústico / Lo-Fi",
            audioFormat = "ALAC • 24-bit / 48kHz",
            isFavorite = false,
            folderName = "Música / Acústico",
            playCount = 9,
            year = 2024
        )
    )

    val sampleAlbums = listOf(
        Album(
            id = "album_1",
            title = "Luces del Volcán",
            artist = "Distrito 503 Project",
            year = 2024,
            coverResId = R.drawable.cover_synthwave,
            songsCount = 8
        ),
        Album(
            id = "album_2",
            title = "Raíces Urbanas",
            artist = "Carlos Martínez",
            year = 2024,
            coverResId = R.drawable.cover_urban,
            songsCount = 10
        ),
        Album(
            id = "album_3",
            title = "Horizontes Infinitos",
            artist = "Nebula Dreams",
            year = 2023,
            coverResId = R.drawable.cover_space,
            songsCount = 6
        ),
        Album(
            id = "album_4",
            title = "Costa del Sol Vibes",
            artist = "Sonsonate Sound",
            year = 2024,
            coverResId = R.drawable.distrito_logo,
            songsCount = 12
        ),
        Album(
            id = "album_5",
            title = "Noches Tropicales",
            artist = "Luna & Tradición",
            year = 2024,
            coverResId = R.drawable.cover_urban,
            songsCount = 7
        )
    )

    val sampleArtists = listOf(
        Artist(
            id = "artist_1",
            name = "Distrito 503 Project",
            songsCount = 8,
            albumsCount = 2
        ),
        Artist(
            id = "artist_2",
            name = "Carlos Martínez",
            songsCount = 14,
            albumsCount = 3
        ),
        Artist(
            id = "artist_3",
            name = "Nebula Dreams",
            songsCount = 6,
            albumsCount = 1
        ),
        Artist(
            id = "artist_4",
            name = "Sonsonate Sound",
            songsCount = 12,
            albumsCount = 2
        ),
        Artist(
            id = "artist_5",
            name = "Luna & Tradición",
            songsCount = 7,
            albumsCount = 1
        )
    )

    val sampleFolders = listOf(
        MusicFolder(
            id = "folder_1",
            name = "Distrito503 / Master Audio",
            path = "/storage/emulated/0/Music/Distrito503",
            songCount = 8
        ),
        MusicFolder(
            id = "folder_2",
            name = "Descargas / Urban Beats",
            path = "/storage/emulated/0/Download/UrbanBeats",
            songCount = 14
        ),
        MusicFolder(
            id = "folder_3",
            name = "Hi-Res / Audiophile",
            path = "/storage/emulated/0/Music/HiRes_Audio",
            songCount = 12
        ),
        MusicFolder(
            id = "folder_4",
            name = "Música / Verano 503",
            path = "/storage/emulated/0/Music/Verano503",
            songCount = 18
        ),
        MusicFolder(
            id = "folder_5",
            name = "Música / Acústico",
            path = "/storage/emulated/0/Music/Acustico",
            songCount = 7
        )
    )
}
