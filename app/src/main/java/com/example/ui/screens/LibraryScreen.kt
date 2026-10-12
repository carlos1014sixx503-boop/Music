package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import coil.compose.AsyncImage
import com.example.R
import com.example.data.model.Track
import com.example.state.MusicPlayerState
import com.example.ui.theme.GlassSurfaceUltraDark
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

/**
 * Screen 2: Library Screen ("Your library") matching reference image:
 * - Top header with Alex avatar, search and notification icons.
 * - Headline "Your library".
 * - Filter pills: All, Liked Songs, Playlists, Downloads.
 * - Track list matching Screen 2:
 *   Save Your Tears (3.35), Happier Than Ever (4.57), Sunflower (2.40),
 *   Believer (3.25), Positions (3.02), Shivers (3.28), Ghost (3.12).
 * - Circular dark glass play button on each row.
 */
@Composable
fun LibraryScreen(
    playerState: MusicPlayerState,
    onNavigateToSearch: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val playlist by playerState.playlist.collectAsState()
    val favoritesIds by playerState.favorites.collectAsState()
    var selectedFilterIndex by remember { mutableIntStateOf(0) }
    val filters = listOf("All", "Liked Songs", "Playlists", "Downloads")

    // Curated catalog matching Screen 2
    val libraryTracks = remember(playlist) {
        listOf(
            Track(
                id = "track_save_your_tears",
                title = "Save Your Tears",
                artist = "The Weeknd",
                durationMs = 215000L,
                albumArtUrl = "https://images.unsplash.com/photo-1514525253161-7a46d19cd819?w=600&auto=format&fit=crop&q=80",
                genre = "Pop"
            ),
            Track(
                id = "track_happier_than_ever",
                title = "Happier Than Ever",
                artist = "Billie Eilish",
                durationMs = 297000L,
                albumArtUrl = "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=600&auto=format&fit=crop&q=80",
                genre = "Alternative"
            ),
            Track(
                id = "track_sunflower",
                title = "Sunflower",
                artist = "Post Malone",
                durationMs = 160000L,
                albumArtUrl = "https://images.unsplash.com/photo-1501386761578-eac5c94b800a?w=600&auto=format&fit=crop&q=80",
                genre = "Hip-Hop"
            ),
            Track(
                id = "track_believer",
                title = "Believer",
                artist = "Imagine Dragons",
                durationMs = 205000L,
                albumArtUrl = "https://images.unsplash.com/photo-1465847899084-d164df4dedc6?w=600&auto=format&fit=crop&q=80",
                genre = "Rock"
            ),
            Track(
                id = "track_positions",
                title = "Positions",
                artist = "Ariana Grande",
                durationMs = 182000L,
                albumArtUrl = "https://images.unsplash.com/photo-1470225620780-dba8ba36b745?w=600&auto=format&fit=crop&q=80",
                genre = "R&B"
            ),
            Track(
                id = "track_shivers",
                title = "Shivers",
                artist = "Ed Sheeran",
                durationMs = 208000L,
                albumArtUrl = "https://images.unsplash.com/photo-1508700115892-45ecd05ae2ad?w=600&auto=format&fit=crop&q=80",
                genre = "Pop"
            ),
            Track(
                id = "track_ghost",
                title = "Ghost",
                artist = "Justin Bieber",
                durationMs = 192000L,
                albumArtUrl = "https://images.unsplash.com/photo-1520523839898-507127cd53c3?w=600&auto=format&fit=crop&q=80",
                genre = "Pop"
            )
        )
    }

    val displayedTracks = remember(selectedFilterIndex, libraryTracks, favoritesIds) {
        when (selectedFilterIndex) {
            1 -> libraryTracks.filter { favoritesIds.contains(it.id) || it.title.contains("Save", ignoreCase = true) }
            else -> libraryTracks
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Top Header: Alex Avatar + Search & Bell Buttons
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Avatar Alex
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(12.dp, CircleShape, ambientColor = NeonPurple, spotColor = NeonPurple)
                        .clip(CircleShape)
                        .border(1.8.dp, NeonPurple, CircleShape)
                        .testTag("library_avatar_alex"),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_avatar_alex),
                        contentDescription = "Alex Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    // Search Circle Button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(GlassSurfaceUltraDark)
                            .border(
                                1.dp,
                                Brush.verticalGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.22f),
                                        Color.White.copy(alpha = 0.05f)
                                    )
                                ),
                                CircleShape
                            )
                            .clickable { onNavigateToSearch() }
                            .testTag("library_search_icon_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TextWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Bell Circle Button
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(GlassSurfaceUltraDark)
                            .border(
                                1.dp,
                                Brush.verticalGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.22f),
                                        Color.White.copy(alpha = 0.05f)
                                    )
                                ),
                                CircleShape
                            )
                            .clickable { /* Notifications */ }
                            .testTag("library_bell_icon_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications",
                            tint = TextWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Title: "Your library"
        item {
            Text(
                text = "Your library",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                color = TextWhite,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
        }

        // Filter Pills Carousel (All, Liked Songs, Playlists, Downloads)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                filters.forEachIndexed { index, title ->
                    val isSelected = selectedFilterIndex == index
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .then(
                                if (isSelected) {
                                    Modifier
                                        .shadow(12.dp, RoundedCornerShape(24.dp), ambientColor = NeonPurple, spotColor = NeonPurple)
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(
                                                    Color(0xFFB042FF),
                                                    Color(0xFF7E22CE)
                                                )
                                            )
                                        )
                                        .border(
                                            1.dp,
                                            Color.White.copy(alpha = 0.5f),
                                            RoundedCornerShape(24.dp)
                                        )
                                } else {
                                    Modifier
                                        .background(GlassSurfaceUltraDark)
                                        .border(
                                            1.dp,
                                            Color.White.copy(alpha = 0.15f),
                                            RoundedCornerShape(24.dp)
                                        )
                                }
                            )
                            .clickable { selectedFilterIndex = index }
                            .padding(horizontal = 18.dp, vertical = 9.dp)
                            .testTag("library_filter_chip_$index"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp
                            ),
                            color = if (isSelected) TextWhite else TextMuted
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(6.dp))
        }

        // Tracks List matching Screen 2
        items(displayedTracks, key = { it.id }) { track ->
            LibraryMockupTrackRow(
                track = track,
                onPlay = {
                    playerState.playTrack(track)
                    playerState.showFullScreenPlayer()
                }
            )
        }
    }
}

@Composable
fun LibraryMockupTrackRow(
    track: Track,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onPlay() }
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .testTag("library_track_row_${track.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rounded Square Album Thumbnail
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(
                    1.dp,
                    Color.White.copy(alpha = 0.15f),
                    RoundedCornerShape(14.dp)
                )
                .background(Color(0xFF16092E))
        ) {
            AsyncImage(
                model = track.albumArtUrl,
                contentDescription = track.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Title & Artist
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = track.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp
                ),
                color = TextWhite,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = track.artist,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp
                ),
                color = TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Duration string matching format in mockup (e.g. "3.35", "4.57")
        val formattedMinSec = remember(track.durationMs) {
            val totalSec = track.durationMs / 1000
            val min = totalSec / 60
            val sec = totalSec % 60
            String.format("%d.%02d", min, sec)
        }

        Text(
            text = formattedMinSec,
            style = MaterialTheme.typography.bodySmall.copy(
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            ),
            color = TextMuted,
            modifier = Modifier.padding(end = 12.dp)
        )

        // Circular Dark Glass Play Button
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color(0x80250E49))
                .border(
                    1.dp,
                    Color.White.copy(alpha = 0.22f),
                    CircleShape
                )
                .clickable { onPlay() }
                .testTag("library_play_btn_${track.id}"),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = "Play",
                tint = TextWhite,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
