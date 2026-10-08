package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassDivider
import com.example.ui.components.LiquidGlassIconButton
import com.example.ui.components.LiquidGlassPillButton
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Album
import com.example.data.Artist
import com.example.data.LibraryTab
import com.example.data.MusicFolder
import com.example.data.Song
import com.example.data.repository.ScanState
import com.example.state.MusicPlayerViewModel
import com.example.ui.components.EmptyLibraryView
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.LibraryErrorView
import com.example.ui.components.LibraryPermissionPrompt
import com.example.ui.components.LibraryScanningView
import com.example.ui.components.SongArtworkImage
import com.example.ui.theme.LocalDistritoTheme

/**
 * Pantalla de Biblioteca de Distrito Music 503:
 * Réplica fiel de la estructura de las capturas:
 * - Header con título grande "Biblioteca", usuario Distrito 503 y botón de sincronizar.
 * - Chips de navegación: "Listas", "Canciones", "Álbumes", "Artistas", "Descargas".
 * - Fila de ordenamiento interactivo ("Actividad reciente ▾") y cambio de vista Cuadrícula/Lista.
 * - Tarjetas destacadas superiores: "Tus Me gusta" y "Descargas locales".
 * - Contenido adaptativo con menú contextual de 3 puntos (⋮) para cada canción.
 */
@Composable
fun LibraryScreen(
    viewModel: MusicPlayerViewModel,
    onRequestPermission: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val theme = LocalDistritoTheme.current
    val currentTab = viewModel.selectedLibraryTab
    val scanState = viewModel.scanState
    val allSongs = viewModel.songsList
    val favoriteSongs = allSongs.filter { viewModel.isSongFavorite(it.id) }
    val albums = viewModel.albums
    val artists = viewModel.artists
    val folders = viewModel.folders

    var sortOrder by remember { mutableStateOf("Actividad reciente") }
    var isSortMenuExpanded by remember { mutableStateOf(false) }
    var isGridView by remember { mutableStateOf(false) }

    val sortedSongs = remember(allSongs, sortOrder) {
        when (sortOrder) {
            "Alfabético" -> allSongs.sortedBy { it.title.lowercase() }
            "Artista" -> allSongs.sortedBy { it.artist.lowercase() }
            "Duración" -> allSongs.sortedByDescending { it.durationSeconds }
            else -> allSongs
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("library_screen"),
        contentPadding = PaddingValues(bottom = 150.dp)
    ) {
        // 1. Top Header con Título, Insignia Distrito 503 y Refrescar
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Biblioteca",
                        color = theme.textPrimary,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "${allSongs.size} canciones en tu dispositivo",
                        color = theme.textSecondary,
                        fontSize = 12.sp
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Insignia Distrito 503
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFF1E1E26))
                            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = "503 MUSIC",
                            color = theme.primaryAccent,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                    }

                    // Botón Sincronizar Liquid Glass
                    LiquidGlassIconButton(
                        onClick = { viewModel.scanLibrary() },
                        icon = Icons.Default.Refresh,
                        contentDescription = "Escanear música",
                        size = 38.dp,
                        iconSize = 18.dp,
                        tint = theme.primaryAccent
                    )
                }
            }
        }

        // 2. Chips de Categorías Horizontales ("Listas", "Favoritos", "Canciones", "Álbumes", "Artistas", "Descargas")
        item {
            val tabs = listOf(
                LibraryTab.PLAYLISTS to "Listas",
                LibraryTab.FAVORITES to "Favoritos",
                LibraryTab.SONGS to "Canciones",
                LibraryTab.ALBUMS to "Álbumes",
                LibraryTab.ARTISTS to "Artistas",
                LibraryTab.FOLDERS to "Descargas"
            )

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(tabs) { (tab, label) ->
                    val isSelected = currentTab == tab
                    LiquidGlassPillButton(
                        onClick = { viewModel.setLibraryTab(tab) },
                        isSelected = isSelected,
                        text = label,
                        accentColor = theme.primaryAccent,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // 3. Fila de Ordenamiento y Cambio de Vista ("Actividad reciente ▾" y Cuadrícula/Lista)
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Selector de Orden
                Box {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { isSortMenuExpanded = true }
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = null,
                            tint = theme.primaryAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = sortOrder,
                            color = theme.textPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = theme.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = isSortMenuExpanded,
                        onDismissRequest = { isSortMenuExpanded = false }
                    ) {
                        listOf("Actividad reciente", "Alfabético", "Artista", "Duración").forEach { opt ->
                            DropdownMenuItem(
                                text = { Text(opt, fontSize = 13.sp) },
                                onClick = {
                                    sortOrder = opt
                                    isSortMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // Botón alternar Cuadrícula / Lista para álbumes y canciones
                if (currentTab == LibraryTab.SONGS || currentTab == LibraryTab.ALBUMS) {
                    IconButton(
                        onClick = { isGridView = !isGridView },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = if (isGridView) Icons.AutoMirrored.Filled.ViewList else Icons.Default.GridView,
                            contentDescription = "Cambiar vista",
                            tint = theme.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // 4. Estados de verificación de la biblioteca (Permiso, Escaneo, Error o Vacío)
        item {
            when {
                scanState is ScanState.PermissionRequired -> {
                    Box(modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)) {
                        LibraryPermissionPrompt(onRequestPermission = onRequestPermission)
                    }
                }
                scanState is ScanState.Scanning -> {
                    Box(modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)) {
                        LibraryScanningView()
                    }
                }
                scanState is ScanState.Error -> {
                    Box(modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)) {
                        LibraryErrorView(
                            errorMessage = scanState.error,
                            onRetry = { viewModel.scanLibrary() }
                        )
                    }
                }
                allSongs.isEmpty() -> {
                    Box(modifier = Modifier.padding(horizontal = 18.dp, vertical = 8.dp)) {
                        EmptyLibraryView(onRescan = { viewModel.scanLibrary() })
                    }
                }
            }
        }

        // 5. Botones de Reproducir Todo y Aleatorio
        if (allSongs.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LiquidGlassButton(
                        onClick = {
                            val first = sortedSongs.firstOrNull()
                            if (first != null) viewModel.playSong(first, sortedSongs)
                        },
                        isPrimary = true,
                        cornerRadius = 16.dp,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = theme.materialColorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Reproducir",
                            color = theme.materialColorScheme.onPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    LiquidGlassButton(
                        onClick = {
                            if (!viewModel.isShuffle) viewModel.toggleShuffle()
                            val randomSong = sortedSongs.randomOrNull()
                            if (randomSong != null) viewModel.playSong(randomSong, sortedSongs)
                        },
                        isPrimary = false,
                        cornerRadius = 16.dp,
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = null,
                            tint = theme.primaryAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Aleatorio",
                            color = theme.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 6. Tarjetas Destacadas Superiores: "Tus Me gusta" y "Descargas locales"
        if (currentTab == LibraryTab.PLAYLISTS || currentTab == LibraryTab.SONGS) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Tarjeta "Tus Me gusta"
                    FeaturedPlaylistCard(
                        icon = Icons.Default.Favorite,
                        iconBackground = Brush.linearGradient(listOf(Color(0xFFFF3366), Color(0xFFFF6584))),
                        title = "Tus Me gusta",
                        subtitle = "Base de datos Room • ${favoriteSongs.size} canciones",
                        onClick = { viewModel.setDestination(com.example.state.NavDestination.FAVORITES) }
                    )

                    // Tarjeta "Descargas y almacenamiento local"
                    FeaturedPlaylistCard(
                        icon = Icons.Default.DownloadDone,
                        iconBackground = Brush.linearGradient(listOf(theme.primaryAccent, Color(0xFF007ACC))),
                        title = "Descargas y archivos locales",
                        subtitle = "Almacenamiento del teléfono • ${allSongs.size} pistas",
                        onClick = { viewModel.setLibraryTab(LibraryTab.FOLDERS) }
                    )
                }
            }
        }

        // Separador Liquid Glass
        item {
            LiquidGlassDivider(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                accentGlow = theme.primaryAccent
            )
        }

        // 7. Contenido Dinámico según la pestaña activa
        when (currentTab) {
            LibraryTab.FAVORITES -> {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Canciones favoritas en Room (${favoriteSongs.size})",
                        color = theme.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
                    )
                }

                if (favoriteSongs.isEmpty()) {
                    item {
                        Text(
                            text = "Aún no tienes canciones favoritas guardadas en Room. Toca el corazón en cualquier canción para verla aquí.",
                            color = theme.textSecondary,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
                        )
                    }
                } else {
                    items(favoriteSongs) { song ->
                        Box(modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp)) {
                            LibrarySongRow(
                                song = song,
                                isCurrent = viewModel.currentSong?.id == song.id,
                                isPlaying = viewModel.isPlaying,
                                onPlay = { viewModel.playSong(song, favoriteSongs) },
                                onToggleFavorite = { viewModel.toggleFavorite(song.id) },
                                isFavorite = true,
                                onOpenMenu = { viewModel.openSongActionMenu(song) }
                            )
                        }
                    }
                }
            }

            LibraryTab.SONGS -> {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Todas las canciones (${sortedSongs.size})",
                        color = theme.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
                    )
                }

                items(sortedSongs) { song ->
                    Box(modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp)) {
                        LibrarySongRow(
                            song = song,
                            isCurrent = viewModel.currentSong?.id == song.id,
                            isPlaying = viewModel.isPlaying,
                            onPlay = { viewModel.playSong(song, sortedSongs) },
                            onToggleFavorite = { viewModel.toggleFavorite(song.id) },
                            isFavorite = viewModel.isSongFavorite(song.id),
                            onOpenMenu = { viewModel.openSongActionMenu(song) }
                        )
                    }
                }
            }

            LibraryTab.PLAYLISTS -> {
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Listas de reproducción",
                        color = theme.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
                    )
                }

                // Lista de reproducciones recientes
                item {
                    Box(modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp)) {
                        FeaturedPlaylistCard(
                            icon = Icons.Default.History,
                            iconBackground = Brush.linearGradient(listOf(Color(0xFF8A2BE2), Color(0xFF4A00E0))),
                            title = "Reproducidas recientemente",
                            subtitle = "Historial • ${viewModel.recentlyPlayedSongs.size} canciones",
                            onClick = {
                                val first = viewModel.recentlyPlayedSongs.firstOrNull()
                                if (first != null) viewModel.playSong(first, viewModel.recentlyPlayedSongs)
                            }
                        )
                    }
                }
            }

            LibraryTab.ALBUMS -> {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Álbumes (${albums.size})",
                        color = theme.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
                    )
                }

                item {
                    if (albums.isEmpty()) {
                        Text(
                            text = "No se encontraron álbumes en el dispositivo.",
                            color = theme.textSecondary,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 12.dp)
                        )
                    } else {
                        // Renderizado en filas de 2 álbumes
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 18.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            albums.chunked(2).forEach { pair ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    pair.forEach { album ->
                                        Box(modifier = Modifier.weight(1f)) {
                                            AlbumGridItem(
                                                album = album,
                                                onClick = {
                                                    val song = allSongs.firstOrNull { it.album == album.title }
                                                    if (song != null) viewModel.playSong(song)
                                                }
                                            )
                                        }
                                    }
                                    if (pair.size == 1) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            LibraryTab.ARTISTS -> {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Artistas (${artists.size})",
                        color = theme.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
                    )
                }

                items(artists) { artist ->
                    Box(modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp)) {
                        ArtistRowItem(
                            artist = artist,
                            onClick = {
                                val song = allSongs.firstOrNull { it.artist == artist.name }
                                if (song != null) viewModel.playSong(song)
                            }
                        )
                    }
                }
            }

            LibraryTab.FOLDERS -> {
                item {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Carpetas de audio (${folders.size})",
                        color = theme.textPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)
                    )
                }

                items(folders) { folder ->
                    Box(modifier = Modifier.padding(horizontal = 18.dp, vertical = 4.dp)) {
                        FolderRowItem(
                            folder = folder,
                            onClick = {
                                val song = allSongs.firstOrNull { it.folderPath == folder.path || it.folderName == folder.name }
                                if (song != null) viewModel.playSong(song)
                            }
                        )
                    }
                }
            }

            else -> {}
        }
    }
}

/**
 * Tarjeta destacada tipo Liquid Glass para playlists principales como "Tus Me gusta"
 */
@Composable
fun FeaturedPlaylistCard(
    icon: ImageVector,
    iconBackground: Brush,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    val theme = LocalDistritoTheme.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(12.dp, RoundedCornerShape(18.dp), ambientColor = Color.Black.copy(alpha = 0.5f))
            .clip(RoundedCornerShape(18.dp))
            .background(
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF22222B).copy(alpha = 0.85f),
                        Color(0xFF16161D).copy(alpha = 0.85f)
                    )
                )
            )
            .border(
                1.dp,
                Brush.horizontalGradient(
                    listOf(Color.White.copy(alpha = 0.18f), Color.White.copy(alpha = 0.05f))
                ),
                RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(54.dp)
                .shadow(8.dp, RoundedCornerShape(14.dp))
                .clip(RoundedCornerShape(14.dp))
                .background(iconBackground),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = theme.textPrimary,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                color = theme.textSecondary,
                fontSize = 12.sp
            )
        }

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Reproducir",
                tint = theme.primaryAccent,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Fila de canción de Biblioteca con botón de 3 puntos (⋮)
 */
@Composable
fun LibrarySongRow(
    song: Song,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onToggleFavorite: () -> Unit,
    isFavorite: Boolean,
    onOpenMenu: () -> Unit
) {
    val theme = LocalDistritoTheme.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isCurrent) theme.surfaceColor.copy(alpha = 0.85f)
                else Color(0xFF1C1C24).copy(alpha = 0.6f)
            )
            .border(
                1.dp,
                if (isCurrent) theme.primaryAccent.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.08f),
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onPlay)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(theme.surfaceVariantColor)
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
        ) {
            SongArtworkImage(
                albumArtUri = song.albumArtUri,
                coverResId = song.coverResId,
                contentDescription = song.title,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = song.title,
                color = if (isCurrent) theme.primaryAccent else theme.textPrimary,
                fontSize = 13.sp,
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${song.artist} • ${song.durationFormatted}",
                color = theme.textSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        IconButton(
            onClick = onOpenMenu,
            modifier = Modifier.size(34.dp)
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Opciones",
                tint = theme.textSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Item de cuadrícula para Álbum
 */
@Composable
fun AlbumGridItem(
    album: Album,
    onClick: () -> Unit
) {
    val theme = LocalDistritoTheme.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .shadow(12.dp, RoundedCornerShape(16.dp))
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFF1E1E26))
                .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(16.dp))
        ) {
            SongArtworkImage(
                albumArtUri = album.albumArtUri,
                coverResId = album.coverResId,
                contentDescription = album.title,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = album.title,
            color = theme.textPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = "${album.artist} • ${album.songsCount} pistas",
            color = theme.textSecondary,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Fila de Artista
 */
@Composable
fun ArtistRowItem(
    artist: Artist,
    onClick: () -> Unit
) {
    val theme = LocalDistritoTheme.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF1C1C24).copy(alpha = 0.6f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(theme.primaryAccent.copy(alpha = 0.2f))
                .border(1.5.dp, theme.primaryAccent, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = null,
                tint = theme.primaryAccent,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = artist.name,
                color = theme.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${artist.songsCount} canciones • ${artist.albumsCount} álbumes",
                color = theme.textSecondary,
                fontSize = 11.sp
            )
        }
    }
}

/**
 * Fila de Carpeta Local
 */
@Composable
fun FolderRowItem(
    folder: MusicFolder,
    onClick: () -> Unit
) {
    val theme = LocalDistritoTheme.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF1C1C24).copy(alpha = 0.6f))
            .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(theme.surfaceColor)
                .border(1.dp, theme.surfaceBorderColor, RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = theme.primaryAccent,
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = folder.name,
                color = theme.textPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${folder.songCount} canciones • ${folder.path}",
                color = theme.textSecondary,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
