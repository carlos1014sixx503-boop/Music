package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.youtube.YouTubeSearchState
import com.example.data.youtube.YouTubeVideo
import com.example.state.MusicPlayerViewModel
import com.example.ui.components.GlassmorphicCard
import com.example.ui.theme.LocalDistritoTheme

enum class SearchSourceFilter(val title: String, val iconLabel: String) {
    YOUTUBE("YouTube Music", "🎬"),
    ALL("Todo", "✨"),
    LOCAL("En dispositivo", "📱")
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SearchScreen(
    viewModel: MusicPlayerViewModel,
    modifier: Modifier = Modifier
) {
    val theme = LocalDistritoTheme.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val query = viewModel.searchQuery
    val searchSongs = viewModel.searchResultsSongs
    val searchAlbums = viewModel.searchResultsAlbums
    val searchArtists = viewModel.searchResultsArtists
    val totalLocalResults = searchSongs.size + searchAlbums.size + searchArtists.size

    val youtubeState = viewModel.youtubeSearchState

    var currentFilter by remember { mutableStateOf(SearchSourceFilter.YOUTUBE) }

    val musicGenresTags = listOf(
        "Reggaeton 2026", "Rock Clásico", "Pop Latino", "Bachata Urbana",
        "Trap Latino", "Electrónica 503", "Lo-Fi Beats", "Cumbia Sonidera",
        "Salsa Brava", "Acústico"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp)
            .testTag("search_screen")
    ) {
        // 1. Encabezado principal con insignia de red YouTube Data API
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFF0000).copy(alpha = 0.18f))
                        .border(1.2.dp, Color(0xFFFF334B), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SmartDisplay,
                        contentDescription = "YouTube Data API",
                        tint = Color(0xFFFF334B),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Buscar Música",
                        color = theme.textPrimary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "YouTube Data API v3 & Biblioteca Local",
                        color = theme.textMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFF334B).copy(alpha = 0.15f))
                    .border(1.dp, Color(0xFFFF334B).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = "RETROFIT v3",
                    color = Color(0xFFFF5566),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // 2. Barra de Búsqueda Reactiva con Retrofit
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(theme.surfaceColor)
                .border(
                    1.2.dp,
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFFFF334B).copy(alpha = 0.6f),
                            theme.primaryAccent.copy(alpha = 0.4f)
                        )
                    ),
                    RoundedCornerShape(16.dp)
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = query,
                    onValueChange = { newQuery ->
                        viewModel.updateSearchQuery(newQuery)
                        viewModel.updateYouTubeSearchQuery(newQuery)
                    },
                    placeholder = {
                        Text(
                            text = "Buscar canciones, artistas, géneros...",
                            color = theme.textMuted,
                            fontSize = 13.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Buscar",
                            tint = Color(0xFFFF334B)
                        )
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(
                                onClick = {
                                    viewModel.updateSearchQuery("")
                                    viewModel.updateYouTubeSearchQuery("")
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Limpiar",
                                    tint = theme.textSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = {
                            keyboardController?.hide()
                            if (query.isNotBlank()) {
                                viewModel.searchYouTube(query)
                            }
                        }
                    ),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = theme.textPrimary,
                        unfocusedTextColor = theme.textPrimary
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_text_input")
                )

                // Botón de acción Buscar
                Box(
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFFFF334B),
                                    Color(0xFFE50914)
                                )
                            )
                        )
                        .clickable {
                            keyboardController?.hide()
                            if (query.isNotBlank()) {
                                viewModel.searchYouTube(query)
                            }
                        }
                        .testTag("search_action_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Ejecutar búsqueda",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 3. Selector de Pestañas de Filtro (YouTube Music | Todo | En dispositivo)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SearchSourceFilter.values().forEach { filter ->
                val isSelected = (currentFilter == filter)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) {
                                if (filter == SearchSourceFilter.YOUTUBE) Color(0xFFFF0000).copy(alpha = 0.22f)
                                else theme.primaryAccent.copy(alpha = 0.22f)
                            } else Color.White.copy(alpha = 0.05f)
                        )
                        .border(
                            1.dp,
                            if (isSelected) {
                                if (filter == SearchSourceFilter.YOUTUBE) Color(0xFFFF334B)
                                else theme.primaryAccent
                            } else Color.White.copy(alpha = 0.10f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            currentFilter = filter
                            if (filter != SearchSourceFilter.LOCAL && query.isNotBlank() && youtubeState is YouTubeSearchState.Idle) {
                                viewModel.searchYouTube(query)
                            }
                        }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${filter.iconLabel} ${filter.title}",
                        color = if (isSelected) {
                            if (filter == SearchSourceFilter.YOUTUBE) Color(0xFFFF5566)
                            else theme.primaryAccent
                        } else theme.textSecondary,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 4. Contenido según el filtro activo
        when (currentFilter) {
            SearchSourceFilter.YOUTUBE -> {
                YouTubeMusicSearchContent(
                    viewModel = viewModel,
                    query = query,
                    youtubeState = youtubeState,
                    genreTags = musicGenresTags,
                    onTagClick = { tag ->
                        viewModel.updateSearchQuery(tag)
                        viewModel.updateYouTubeSearchQuery(tag)
                        viewModel.searchYouTube(tag)
                    },
                    onSearchSubmit = { viewModel.searchYouTube(query) }
                )
            }
            SearchSourceFilter.LOCAL -> {
                LocalDeviceSearchContent(
                    viewModel = viewModel,
                    query = query,
                    searchSongs = searchSongs,
                    searchAlbums = searchAlbums,
                    searchArtists = searchArtists,
                    totalResults = totalLocalResults
                )
            }
            SearchSourceFilter.ALL -> {
                UnifiedSearchContent(
                    viewModel = viewModel,
                    query = query,
                    youtubeState = youtubeState,
                    searchSongs = searchSongs,
                    searchAlbums = searchAlbums,
                    searchArtists = searchArtists,
                    onSearchYouTube = { viewModel.searchYouTube(query) }
                )
            }
        }
    }
}

/**
 * Contenido especializado en la búsqueda online de pistas musicales con YouTube Data API.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun YouTubeMusicSearchContent(
    viewModel: MusicPlayerViewModel,
    query: String,
    youtubeState: YouTubeSearchState,
    genreTags: List<String>,
    onTagClick: (String) -> Unit,
    onSearchSubmit: () -> Unit
) {
    val theme = LocalDistritoTheme.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Chips de géneros musicales para descubrimiento rápido
        item {
            Text(
                text = "GÉNEROS Y ESTILOS MUSICALES EN YOUTUBE",
                color = theme.textMuted,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                genreTags.forEach { tag ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(theme.surfaceColor)
                            .border(1.dp, Color(0xFFFF334B).copy(alpha = 0.35f), RoundedCornerShape(10.dp))
                            .clickable { onTagClick(tag) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = "#$tag",
                            color = Color(0xFFFF6677),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Estado de la búsqueda en YouTube Data API
        when (youtubeState) {
            is YouTubeSearchState.Idle -> {
                item {
                    GlassmorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 16.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartDisplay,
                                contentDescription = null,
                                tint = Color(0xFFFF334B),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Búsqueda de Pistas con YouTube Data API",
                                color = theme.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Escribe una canción o artista en la barra de búsqueda, o toca cualquiera de los géneros arriba para escuchar streaming online de inmediato.",
                                color = theme.textSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            is YouTubeSearchState.Loading -> {
                item {
                    GlassmorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 16.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color = Color(0xFFFF334B),
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.5.dp
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = "Consultando YouTube Data API v3 con Retrofit...",
                                color = theme.textPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            is YouTubeSearchState.NoInternet -> {
                item {
                    GlassmorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 16.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.WifiOff,
                                contentDescription = null,
                                tint = Color(0xFFFF5252),
                                modifier = Modifier.size(38.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Sin conexión a Internet",
                                color = theme.textPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Verifica tu conexión Wi-Fi o datos móviles para buscar canciones en YouTube Data API.",
                                color = theme.textMuted,
                                fontSize = 12.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = onSearchSubmit,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF334B).copy(alpha = 0.25f),
                                    contentColor = Color(0xFFFF6677)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Reintentar búsqueda")
                            }
                        }
                    }
                }
            }

            is YouTubeSearchState.QuotaExceeded -> {
                item {
                    GlassmorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 16.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CloudOff, contentDescription = null, tint = Color(0xFFFFB74D))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Límite de cuota diaria de YouTube alcanzado",
                                    color = Color(0xFFFFB74D),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "La cuota gratuita de consultas de YouTube Data API se ha consumido temporalmente por hoy. Puedes agregar una clave con cuota propia en el panel de Secretos de AI Studio.",
                                color = theme.textSecondary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }
            }

            is YouTubeSearchState.Empty -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.SearchOff, contentDescription = null, tint = theme.textMuted, modifier = Modifier.size(36.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "No se encontraron canciones para \"${youtubeState.query}\"",
                                color = theme.textSecondary,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            is YouTubeSearchState.Error -> {
                item {
                    GlassmorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        cornerRadius = 16.dp
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ErrorOutline, contentDescription = null, tint = Color(0xFFFF5252))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Error de red al consultar YouTube",
                                    color = Color(0xFFFF5252),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = youtubeState.message,
                                color = theme.textSecondary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = onSearchSubmit,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFFF5252).copy(alpha = 0.2f),
                                    contentColor = Color(0xFFFF8A80)
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("Reintentar", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }

            is YouTubeSearchState.Success -> {
                if (youtubeState.isDemoFallback) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(theme.surfaceColor)
                                .border(1.dp, Color(0xFFFF334B).copy(alpha = 0.35f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "💡 Modo Demostración Activo: Mostrando pistas oficiales seleccionadas. Para buscar en vivo cualquier canción del mundo, agrega tu YOUTUBE_API_KEY en el panel de Secretos de AI Studio.",
                                color = theme.textMuted,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "PISTAS EN YOUTUBE MUSIC (${youtubeState.videos.size})",
                            color = theme.textMuted,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Toca para reproducir ▶",
                            color = Color(0xFFFF5566),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                items(youtubeState.videos, key = { it.id }) { video ->
                    YouTubeTrackRowItem(
                        video = video,
                        onClick = { viewModel.playYouTubeVideo(video) }
                    )
                }
            }
        }
    }
}

/**
 * Tarjeta horizontal de pista musical de YouTube con miniatura de alta resolución,
 * distintivo de duración, vistas y botón de reproducción directa.
 */
@Composable
fun YouTubeTrackRowItem(
    video: YouTubeVideo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val theme = LocalDistritoTheme.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(theme.surfaceColor)
            .border(
                1.dp,
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFFFF334B).copy(alpha = 0.25f),
                        theme.surfaceBorderColor
                    )
                ),
                RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(10.dp)
            .testTag("youtube_track_row_${video.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Miniatura 16:9 con badge de duración
        Box(
            modifier = Modifier
                .width(88.dp)
                .aspectRatio(16f / 9f)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.Black)
        ) {
            AsyncImage(
                model = video.thumbnailUrl,
                contentDescription = video.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Duración sobre la imagen
            if (!video.durationFormatted.isNullOrBlank()) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(4.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.80f))
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = video.durationFormatted,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Ícono de play translúcido
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Detalles del track (Título, Canal, Vistas)
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = video.title,
                color = theme.textPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(3.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Color(0xFFFF5566),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = video.channelTitle,
                    color = theme.textMuted,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (!video.viewCountFormatted.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "▶ ${video.viewCountFormatted}",
                    color = theme.textSecondary.copy(alpha = 0.8f),
                    fontSize = 10.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Botón de reproducción rápida
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(
                    Brush.linearGradient(
                        listOf(
                            Color(0xFFFF334B),
                            Color(0xFFE50914)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Reproducir pista",
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Contenido de búsqueda en la biblioteca local del dispositivo.
 */
@Composable
private fun LocalDeviceSearchContent(
    viewModel: MusicPlayerViewModel,
    query: String,
    searchSongs: List<com.example.data.Song>,
    searchAlbums: List<com.example.data.Album>,
    searchArtists: List<com.example.data.Artist>,
    totalResults: Int
) {
    val theme = LocalDistritoTheme.current

    if (query.isBlank()) {
        LazyColumn(
            contentPadding = PaddingValues(bottom = 140.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                Text(
                    text = "CANCIÓN DESTACADA EN TU DISPOSITIVO",
                    color = theme.textMuted,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            items(viewModel.songsList.take(8)) { song ->
                SongListRowItem(
                    song = song,
                    isCurrent = viewModel.currentSong?.id == song.id,
                    isPlaying = viewModel.isPlaying,
                    onPlay = { viewModel.playSong(song) },
                    onToggleFavorite = { viewModel.toggleFavorite(song.id) },
                    isFavorite = viewModel.isSongFavorite(song.id),
                    onOpenMenu = { viewModel.openSongActionMenu(song) }
                )
            }
        }
    } else {
        if (totalResults == 0) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No se encontraron archivos locales para \"$query\"",
                    color = theme.textSecondary,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 140.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (searchArtists.isNotEmpty()) {
                    item {
                        Text(
                            text = "Artistas (${searchArtists.size})",
                            color = theme.primaryAccent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(searchArtists) { artist ->
                        ArtistRowItem(
                            artist = artist,
                            onClick = {
                                val song = viewModel.songsList.firstOrNull { it.artist == artist.name }
                                if (song != null) viewModel.playSong(song)
                            }
                        )
                    }
                }

                if (searchSongs.isNotEmpty()) {
                    item {
                        Text(
                            text = "Canciones (${searchSongs.size})",
                            color = theme.primaryAccent,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    items(searchSongs) { song ->
                        SongListRowItem(
                            song = song,
                            isCurrent = viewModel.currentSong?.id == song.id,
                            isPlaying = viewModel.isPlaying,
                            onPlay = { viewModel.playSong(song) },
                            onToggleFavorite = { viewModel.toggleFavorite(song.id) },
                            isFavorite = viewModel.isSongFavorite(song.id),
                            onOpenMenu = { viewModel.openSongActionMenu(song) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Búsqueda combinada: presenta tanto pistas online de YouTube como archivos del dispositivo.
 */
@Composable
private fun UnifiedSearchContent(
    viewModel: MusicPlayerViewModel,
    query: String,
    youtubeState: YouTubeSearchState,
    searchSongs: List<com.example.data.Song>,
    searchAlbums: List<com.example.data.Album>,
    searchArtists: List<com.example.data.Artist>,
    onSearchYouTube: () -> Unit
) {
    val theme = LocalDistritoTheme.current

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 140.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Sección 1: Pistas en YouTube Music
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SmartDisplay,
                        contentDescription = null,
                        tint = Color(0xFFFF334B),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "YOUTUBE MUSIC ONLINE",
                        color = Color(0xFFFF5566),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                if (query.isNotBlank() && youtubeState !is YouTubeSearchState.Loading) {
                    Text(
                        text = "Buscar en YouTube",
                        color = theme.primaryAccent,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onSearchYouTube() }
                    )
                }
            }
        }

        when (youtubeState) {
            is YouTubeSearchState.Loading -> {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = Color(0xFFFF334B),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            }
            is YouTubeSearchState.Success -> {
                items(youtubeState.videos.take(5), key = { "unified_${it.id}" }) { video ->
                    YouTubeTrackRowItem(
                        video = video,
                        onClick = { viewModel.playYouTubeVideo(video) }
                    )
                }
            }
            else -> {
                item {
                    Text(
                        text = if (query.isBlank()) "Escribe arriba para buscar canciones en vivo en YouTube Data API." else "Toca 'Buscar en YouTube' para consultar pistas online.",
                        color = theme.textMuted,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Sección 2: Archivos Locales
        item {
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = theme.primaryAccent,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "ARCHIVOS LOCALES EN ESTE DISPOSITIVO (${searchSongs.size})",
                    color = theme.primaryAccent,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        if (searchSongs.isEmpty()) {
            item {
                Text(
                    text = "No hay canciones locales que coincidan con la búsqueda.",
                    color = theme.textMuted,
                    fontSize = 12.sp
                )
            }
        } else {
            items(searchSongs.take(6)) { song ->
                SongListRowItem(
                    song = song,
                    isCurrent = viewModel.currentSong?.id == song.id,
                    isPlaying = viewModel.isPlaying,
                    onPlay = { viewModel.playSong(song) },
                    onToggleFavorite = { viewModel.toggleFavorite(song.id) },
                    isFavorite = viewModel.isSongFavorite(song.id),
                    onOpenMenu = { viewModel.openSongActionMenu(song) }
                )
            }
        }
    }
}
