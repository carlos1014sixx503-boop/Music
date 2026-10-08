package com.example.ui.screens

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Album
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.Album
import com.example.data.LibraryTab
import com.example.data.Song
import com.example.data.repository.ScanState
import com.example.state.MusicPlayerViewModel
import com.example.state.NavDestination
import com.example.ui.components.EmptyLibraryView
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.LibraryErrorView
import com.example.ui.components.LibraryPermissionPrompt
import com.example.ui.components.LibraryScanningView
import com.example.ui.components.LiquidGlassButton
import com.example.ui.components.LiquidGlassDivider
import com.example.ui.components.LiquidGlassIconButton
import com.example.ui.components.LiquidGlassPillButton
import com.example.ui.components.SongArtworkImage
import com.example.ui.components.YouTubeSearchSection
import com.example.ui.theme.LocalDistritoTheme

/**
 * Pantalla Principal "Escuchar ahora" de Distrito Music 503:
 * Réplica fiel de la composición y estructura de las capturas con Liquid Glass:
 * - Header superior con insignia "DISTRITO 503 / ONLYFANSEXY", Cast y sincronización.
 * - Chips de categorías horizontales: Todo, Energía, Relajante, Entrenamiento, Para concentrarse, Viaje diario.
 * - Sección "Vuelve a escucharlo" con tarjetas grandes tipo carátula cuadrada de alta definición.
 * - Sección "Favoritos rápidos" (Quick Picks) con cuadrícula de 4 filas con botón de 3 puntos (⋮) para acciones.
 * - Sección "Mixes para ti" / Álbumes locales.
 * - Módulo de búsqueda online YouTube Audio preservado.
 */
@Composable
fun HomeScreen(
    viewModel: MusicPlayerViewModel,
    onRequestPermission: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val theme = LocalDistritoTheme.current
    val recentlyPlayed = viewModel.recentlyPlayedSongs
    val allSongs = viewModel.songsList
    val favoriteSongs = allSongs.filter { viewModel.isSongFavorite(it.id) }
    val albums = viewModel.albums
    val scanState = viewModel.scanState

    val homeCategories = listOf(
        "Todo",
        "Energía",
        "Relajante",
        "Entrenamiento",
        "Para concentrarse",
        "Viaje diario"
    )

    // Filtrar canciones según categoría seleccionada para dinamismo
    val displayedSongs = when (viewModel.selectedHomeCategory) {
        "Energía" -> allSongs.sortedByDescending { it.durationSeconds }
        "Relajante" -> allSongs.sortedBy { it.title.length }
        "Entrenamiento" -> allSongs.reversed()
        "Para concentrarse" -> allSongs.sortedBy { it.artist }
        "Viaje diario" -> if (recentlyPlayed.isNotEmpty()) recentlyPlayed else allSongs
        else -> allSongs
    }

    // Dividir en columnas de 4 canciones para la cuadrícula horizontal "Favoritos rápidos"
    val quickPickColumns: List<List<Song>> = displayedSongs.chunked(4)

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen"),
        contentPadding = PaddingValues(bottom = 150.dp)
    ) {
        // 1. App Header & Profile Tag ("DISTRITO 503 / ONLYFANSEXY")
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 18.dp, top = 10.dp, bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Insignia del usuario y logotipo
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(26.dp))
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Color(0xFF22222B).copy(alpha = 0.90f),
                                    Color(0xFF16161D).copy(alpha = 0.85f)
                                )
                            )
                        )
                        .border(
                            1.2.dp,
                            Brush.horizontalGradient(
                                colors = listOf(
                                    theme.primaryAccent.copy(alpha = 0.6f),
                                    Color.White.copy(alpha = 0.15f)
                                )
                            ),
                            RoundedCornerShape(26.dp)
                        )
                        .clickable { viewModel.setDestination(NavDestination.SETTINGS) }
                        .padding(start = 4.dp, top = 4.dp, bottom = 4.dp, end = 14.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .shadow(6.dp, CircleShape, ambientColor = theme.glowColor, spotColor = theme.glowColor)
                            .clip(CircleShape)
                            .background(theme.surfaceColor)
                            .border(1.5.dp, theme.primaryAccent, CircleShape)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.distrito_logo),
                            contentDescription = "Distrito Music 503 Logo",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "DISTRITO 503",
                                color = theme.textPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "• PRO",
                                color = theme.primaryAccent,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "ONLYFANSEXY • EL SALVADOR",
                            color = theme.textSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            letterSpacing = 0.3.sp
                        )
                    }
                }

                // Acciones: Cast, Ecualizador y Refrescar con estilo Liquid Glass
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Botón Cast / Transmisión
                    LiquidGlassIconButton(
                        onClick = { viewModel.showStatsForNerds(true) },
                        icon = Icons.Default.Cast,
                        contentDescription = "Transmitir audio",
                        size = 38.dp,
                        iconSize = 18.dp,
                        tint = theme.textPrimary
                    )

                    // Botón Ecualizador DSP
                    LiquidGlassIconButton(
                        onClick = { viewModel.showEqualizerModal(true) },
                        icon = Icons.Default.Equalizer,
                        contentDescription = "Ecualizador DSP",
                        size = 38.dp,
                        iconSize = 18.dp,
                        tint = theme.primaryAccent,
                        isHighlighted = true
                    )

                    // Botón Sincronizar biblioteca
                    LiquidGlassIconButton(
                        onClick = { viewModel.scanLibrary() },
                        icon = Icons.Default.Refresh,
                        contentDescription = "Escanear música",
                        size = 38.dp,
                        iconSize = 18.dp,
                        tint = theme.textPrimary
                    )
                }
            }
        }

        // 2. Filtros Horizontales Liquid Glass ("Todo", "Energía", "Relajante", "Entrenamiento", etc.)
        item {
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                contentPadding = PaddingValues(horizontal = 18.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(homeCategories) { category ->
                    val isSelected = viewModel.selectedHomeCategory == category
                    LiquidGlassPillButton(
                        onClick = { viewModel.selectHomeCategory(category) },
                        isSelected = isSelected,
                        text = category,
                        accentColor = theme.primaryAccent,
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // 3. Estado de la biblioteca (Permiso, Escaneo, Error o Vacío)
        item {
            when {
                scanState is ScanState.PermissionRequired -> {
                    Box(modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)) {
                        LibraryPermissionPrompt(onRequestPermission = onRequestPermission)
                    }
                }
                scanState is ScanState.Scanning -> {
                    Box(modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)) {
                        LibraryScanningView()
                    }
                }
                scanState is ScanState.Error -> {
                    Box(modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)) {
                        LibraryErrorView(
                            errorMessage = scanState.error,
                            onRetry = { viewModel.scanLibrary() }
                        )
                    }
                }
                allSongs.isEmpty() -> {
                    Box(modifier = Modifier.padding(horizontal = 18.dp, vertical = 6.dp)) {
                        EmptyLibraryView(onRescan = { viewModel.scanLibrary() })
                    }
                }
            }
        }

        // 4. SECCIÓN 1: "Vuelve a escucharlo" (Tarjetas Grandes Cuadradas con Liquid Glass)
        val cardsToDisplay = if (recentlyPlayed.isNotEmpty()) recentlyPlayed else allSongs.take(8)
        if (cardsToDisplay.isNotEmpty()) {
            item {
                SectionHeaderWithProfile(
                    tag = "ESCUCHAR DE NUEVO",
                    title = "Vuelve a escucharlo",
                    onSeeAll = {
                        viewModel.setLibraryTab(LibraryTab.RECENT)
                        viewModel.setDestination(NavDestination.LIBRARY)
                    }
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(cardsToDisplay) { song ->
                        LargeAlbumCard(
                            song = song,
                            isCurrentPlaying = viewModel.currentSong?.id == song.id && viewModel.isPlaying,
                            onPlay = { viewModel.playSong(song) },
                            onOpenMenu = { viewModel.openSongActionMenu(song) }
                        )
                    }
                }
            }
        }

        // 5. SECCIÓN 2: "Favoritos rápidos" (Cuadrícula de 4 filas al estilo exacto de las capturas)
        if (displayedSongs.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SELECCIÓN RÁPIDA",
                            color = theme.primaryAccent,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = "Favoritos rápidos",
                            color = theme.textPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Botón "Reproducir todo" Liquid Glass
                    LiquidGlassButton(
                        onClick = {
                            val first = displayedSongs.firstOrNull()
                            if (first != null) viewModel.playSong(first, displayedSongs)
                        },
                        cornerRadius = 18.dp,
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = theme.primaryAccent,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Reproducir todo",
                            color = theme.textPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Cuadrícula Horizontal de columnas de 4 canciones
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(quickPickColumns) { columnSongs ->
                        Column(
                            modifier = Modifier.width(310.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            columnSongs.forEach { song ->
                                QuickPickSongRowItem(
                                    song = song,
                                    isCurrent = viewModel.currentSong?.id == song.id,
                                    isPlaying = viewModel.isPlaying,
                                    onPlay = { viewModel.playSong(song, displayedSongs) },
                                    onOpenMenu = { viewModel.openSongActionMenu(song) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 6. SECCIÓN 3: "Álbumes y sencillos recomendados"
        if (albums.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                SectionHeaderWithProfile(
                    tag = "DISCOGRAFÍA",
                    title = "Álbumes y colecciones",
                    onSeeAll = {
                        viewModel.setLibraryTab(LibraryTab.ALBUMS)
                        viewModel.setDestination(NavDestination.LIBRARY)
                    }
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(albums.take(8)) { album ->
                        AlbumItemCard(
                            album = album,
                            onClick = {
                                val song = allSongs.firstOrNull { it.album == album.title }
                                if (song != null) viewModel.playSong(song)
                            }
                        )
                    }
                }
            }
        }

        // 7. SECCIÓN 4: Canciones Favoritas (Acceso directo con tarjeta Liquid Glass)
        if (favoriteSongs.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(18.dp))
                SectionHeaderWithProfile(
                    tag = "TUS ME GUSTA",
                    title = "Canciones favoritas",
                    onSeeAll = { viewModel.setDestination(NavDestination.FAVORITES) }
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    favoriteSongs.take(3).forEach { song ->
                        QuickPickSongRowItem(
                            song = song,
                            isCurrent = viewModel.currentSong?.id == song.id,
                            isPlaying = viewModel.isPlaying,
                            onPlay = { viewModel.playSong(song, favoriteSongs) },
                            onOpenMenu = { viewModel.openSongActionMenu(song) }
                        )
                    }
                }
            }
        }

        // 8. Separador Informativo: Distrito Music 503 Audio Pro
        item {
            Spacer(modifier = Modifier.height(10.dp))
            LiquidGlassDivider(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp),
                accentGlow = theme.primaryAccent
            )
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF1E1E26).copy(alpha = 0.85f),
                                Color(0xFF14141A).copy(alpha = 0.85f)
                            )
                        )
                    )
                    .border(
                        1.dp,
                        Brush.horizontalGradient(
                            listOf(
                                theme.primaryAccent.copy(alpha = 0.4f),
                                Color.White.copy(alpha = 0.1f)
                            )
                        ),
                        RoundedCornerShape(18.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(theme.primaryAccent.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Headphones,
                            contentDescription = null,
                            tint = theme.primaryAccent,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "Distrito Music 503 • Hi-Res Audio Engine",
                            color = theme.textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Motor DSP • Sin interrupciones • Segundo plano",
                            color = theme.primaryAccent,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        // 9. Búsqueda y streaming online de YouTube
        item {
            Spacer(modifier = Modifier.height(12.dp))
            YouTubeSearchSection(
                searchQuery = viewModel.youtubeSearchQuery,
                searchState = viewModel.youtubeSearchState,
                onQueryChange = { viewModel.updateYouTubeSearchQuery(it) },
                onSearch = { viewModel.searchYouTube(it) },
                onVideoClick = { viewModel.playYouTubeVideo(it) }
            )
        }
    }
}

/**
 * Encabezado de sección con subtítulo estilizado
 */
@Composable
fun SectionHeaderWithProfile(
    tag: String,
    title: String,
    onSeeAll: (() -> Unit)? = null
) {
    val theme = LocalDistritoTheme.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 18.dp, end = 18.dp, top = 14.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column {
            Text(
                text = tag,
                color = theme.primaryAccent,
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp
            )
            Text(
                text = title,
                color = theme.textPrimary,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (onSeeAll != null) {
            Text(
                text = "Más",
                color = theme.primaryAccent,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onSeeAll)
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}

/**
 * Tarjeta grande de carátula cuadrada para la sección "Vuelve a escucharlo"
 */
@Composable
fun LargeAlbumCard(
    song: Song,
    isCurrentPlaying: Boolean,
    onPlay: () -> Unit,
    onOpenMenu: () -> Unit
) {
    val theme = LocalDistritoTheme.current

    Column(
        modifier = Modifier
            .width(152.dp)
            .clickable(onClick = onPlay)
    ) {
        Box(
            modifier = Modifier
                .size(152.dp)
                .shadow(
                    elevation = 14.dp,
                    shape = RoundedCornerShape(18.dp),
                    ambientColor = if (isCurrentPlaying) theme.primaryAccent.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.6f),
                    spotColor = if (isCurrentPlaying) theme.primaryAccent.copy(alpha = 0.4f) else Color.Black.copy(alpha = 0.6f)
                )
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFF1E1E26))
                .border(
                    width = 1.2.dp,
                    color = if (isCurrentPlaying) theme.primaryAccent else Color.White.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(18.dp)
                )
        ) {
            SongArtworkImage(
                albumArtUri = song.albumArtUri,
                coverResId = song.coverResId,
                contentDescription = song.title,
                modifier = Modifier.fillMaxSize()
            )

            // Gradiente sombreado inferior
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f)),
                            startY = 180f
                        )
                    )
            )

            // Botón flotante de reproducción en esquina inferior derecha
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(10.dp)
                    .size(38.dp)
                    .shadow(6.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        if (isCurrentPlaying) theme.primaryAccent else Color.Black.copy(alpha = 0.75f)
                    )
                    .border(
                        1.dp,
                        if (isCurrentPlaying) theme.primaryAccent else Color.White.copy(alpha = 0.35f),
                        CircleShape
                    )
                    .clickable(onClick = onPlay),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isCurrentPlaying) Icons.Default.GraphicEq else Icons.Default.PlayArrow,
                    contentDescription = "Reproducir",
                    tint = if (isCurrentPlaying) theme.materialColorScheme.onPrimary else Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = song.title,
            color = if (isCurrentPlaying) theme.primaryAccent else theme.textPrimary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = "${song.artist} • ${song.album}",
            color = theme.textSecondary,
            fontSize = 11.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Fila compacta para "Favoritos rápidos" (Quick Picks) con botón de 3 puntos (⋮)
 */
@Composable
fun QuickPickSongRowItem(
    song: Song,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onOpenMenu: () -> Unit
) {
    val theme = LocalDistritoTheme.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (isCurrent) theme.surfaceColor.copy(alpha = 0.9f)
                else Color(0xFF1E1E26).copy(alpha = 0.65f)
            )
            .border(
                width = 1.dp,
                color = if (isCurrent) theme.primaryAccent.copy(alpha = 0.8f) else Color.White.copy(alpha = 0.08f),
                shape = RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onPlay)
            .padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Thumbnail de la canción
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

            if (isCurrent && isPlaying) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = theme.primaryAccent,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
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

        // Botón de 3 puntos (⋮) para el menú contextual de opciones
        IconButton(
            onClick = onOpenMenu,
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Opciones de canción",
                tint = theme.textSecondary,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * Tarjeta para visualización de álbumes en carrusel
 */
@Composable
fun AlbumItemCard(
    album: Album,
    onClick: () -> Unit = {}
) {
    val theme = LocalDistritoTheme.current

    Column(
        modifier = Modifier
            .width(135.dp)
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(135.dp)
                .shadow(10.dp, RoundedCornerShape(16.dp), ambientColor = theme.glowColor, spotColor = theme.glowColor)
                .clip(RoundedCornerShape(16.dp))
                .background(theme.surfaceColor)
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
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Text(
            text = "${album.artist} • ${album.songsCount} canciones",
            color = theme.textSecondary,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SongListRowItem(
    song: Song,
    isCurrent: Boolean,
    isPlaying: Boolean,
    onPlay: () -> Unit,
    onToggleFavorite: () -> Unit,
    isFavorite: Boolean,
    onOpenMenu: (() -> Unit)? = null
) {
    val theme = LocalDistritoTheme.current

    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 16.dp,
        borderWidth = if (isCurrent) 1.5.dp else 1.dp,
        onClick = onPlay
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(theme.surfaceVariantColor)
                    .border(1.dp, theme.surfaceBorderColor, RoundedCornerShape(12.dp))
            ) {
                SongArtworkImage(
                    albumArtUri = song.albumArtUri,
                    coverResId = song.coverResId,
                    contentDescription = song.title,
                    modifier = Modifier.fillMaxSize()
                )

                if (isCurrent && isPlaying) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = null,
                            tint = theme.primaryAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = song.title,
                    color = if (isCurrent) theme.primaryAccent else theme.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))

                val formatInfo = buildString {
                    append(song.artist)
                    append(" • ")
                    append(song.durationFormatted)
                    if (song.audioFormat.isNotBlank()) {
                        append(" • ")
                        append(song.audioFormat)
                    }
                }

                Text(
                    text = formatInfo,
                    color = theme.textSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(34.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Favorito",
                        tint = if (isFavorite) Color(0xFFFF3366) else theme.surfaceBorderColor.copy(alpha = 0.5f),
                        modifier = Modifier.size(18.dp)
                    )
                }

                if (onOpenMenu != null) {
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
        }
    }
}
