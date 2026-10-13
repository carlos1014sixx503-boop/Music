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
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.example.ui.theme.GlassSurfaceHigh
import com.example.ui.theme.GlassSurfaceUltraDark
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonPurpleGlow
import com.example.ui.theme.NeonPurpleLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextWhite

/**
 * Screen 1: Home Screen ("Hello, Alex") matching reference image:
 * - Circular avatar Alex on left with neon purple ring.
 * - Circular glass action buttons on right (Search & Bell).
 * - "Hello, Alex" greeting headline.
 * - Filter pills carousel (All, New Artists, Hot Tracks, Editor's Picks).
 * - "For you" glossy glass card featuring "Feel the Beat" with DJ artwork.
 * - "Popular" list with "Show all >" and circular glass play buttons.
 */
@Composable
fun HomeScreen(
    playerState: MusicPlayerState,
    onNavigateToSearch: () -> Unit,
    onNavigateToLibrary: () -> Unit = {},
    onNavigateToEqualizer: () -> Unit = {},
    onNavigateToSettings: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val playlist by playerState.playlist.collectAsState()
    var selectedFilterIndex by remember { mutableIntStateOf(0) }
    val filters = listOf("All", "New Artists", "Hot Tracks", "Editor's Picks")

    val popularTracks = remember(playlist) {
        if (playlist.isNotEmpty()) {
            listOf(
                playlist[0].copy(title = "Blinding Light", genre = "Top Hit"),
                if (playlist.size > 2) playlist[2].copy(title = "Ocean Eyes", genre = "Soft Vibe") else playlist[0],
                if (playlist.size > 3) playlist[3].copy(title = "Circles Run", genre = "Fan Fav") else playlist[0],
                if (playlist.size > 1) playlist[1].copy(title = "Ghost", genre = "Top Chart") else playlist[0]
            )
        } else emptyList()
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 120.dp)
    ) {
        // Top Header: Avatar + Search & Bell Buttons
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Profile Avatar Alex with glowing neon purple halo (tap to go to Settings & Account)
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .shadow(12.dp, CircleShape, ambientColor = NeonPurple, spotColor = NeonPurple)
                        .clip(CircleShape)
                        .border(1.8.dp, NeonPurple, CircleShape)
                        .clickable { onNavigateToSettings() }
                        .testTag("home_avatar_alex"),
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
                            .testTag("home_search_icon_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TextWhite,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Equalizer DSP Button (Always available in app)
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(GlassSurfaceUltraDark)
                            .border(
                                1.dp,
                                Brush.verticalGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.28f),
                                        NeonPurple.copy(alpha = 0.35f)
                                    )
                                ),
                                CircleShape
                            )
                            .clickable { onNavigateToEqualizer() }
                            .testTag("home_equalizer_icon_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Ecualizador y Audio DSP",
                            tint = NeonPurpleLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // Bell Circle Button (Ajustes y Cuenta)
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
                            .clickable { onNavigateToSettings() }
                            .testTag("home_bell_icon_btn"),
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

        // Greeting Title: "Hello, Alex"
        item {
            Text(
                text = "Hello, Alex",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                color = TextWhite,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
            )
        }

        // Filter Pills Carousel (All, New Artists, Hot Tracks, Editor's Picks)
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
                            .testTag("home_filter_chip_$index"),
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

        // "For you" Section Title
        item {
            Text(
                text = "For you",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                ),
                color = TextWhite,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )
        }

        // Hero Card Carousel ("Feel the Beat")
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Main Hero Card with 3D liquid glass frame
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(180.dp)
                        .shadow(20.dp, RoundedCornerShape(24.dp), ambientColor = Color.Black, spotColor = NeonPurpleGlow)
                        .clip(RoundedCornerShape(24.dp))
                        .border(
                            1.5.dp,
                            Brush.verticalGradient(
                                listOf(
                                    Color(0x99A855F7),
                                    Color(0x3338BDF8)
                                )
                            ),
                            RoundedCornerShape(24.dp)
                        )
                        .background(Color(0xE6100624))
                        .testTag("hero_for_you_card")
                ) {
                    // Background DJ artwork
                    Image(
                        painter = painterResource(id = R.drawable.img_feel_the_beat),
                        contentDescription = "Feel the Beat",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .align(Alignment.CenterEnd),
                        alpha = 0.50f
                    )

                    // Gradient tint over artwork
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        Color(0xF0120529),
                                        Color(0xBB180838),
                                        Color(0x40100624)
                                    )
                                )
                            )
                    )

                    // Overlay Content
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(18.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "Feel the Beat",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                                color = TextWhite
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                            Text(
                                text = "Explore trending tracks and\nhidden gems curated just\nfor you.",
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                ),
                                color = Color(0xFFDDD2F3),
                                maxLines = 3
                            )
                        }

                        // "Start Listening" pill button
                        Button(
                            onClick = {
                                if (playlist.isNotEmpty()) {
                                    playerState.playTrack(playlist.first())
                                    playerState.showFullScreenPlayer()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0x66441A88),
                                contentColor = TextWhite
                            ),
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Color.White.copy(alpha = 0.25f)
                            ),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("start_listening_btn")
                        ) {
                            Text(
                                "Start Listening",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                            )
                        }
                    }
                }

                // Peeking edge of next card
                Box(
                    modifier = Modifier
                        .width(36.dp)
                        .height(180.dp)
                        .clip(RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp))
                        .border(
                            1.dp,
                            Color.White.copy(alpha = 0.15f),
                            RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)
                        )
                        .background(Color(0xDD1B0936))
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.cover_synthwave),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        alpha = 0.45f,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // "Popular" Header with "Show all >"
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 24.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Popular",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = TextWhite
                )

                Text(
                    text = "Show all >",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = TextMuted,
                        fontSize = 13.sp
                    ),
                    modifier = Modifier
                        .clickable { onNavigateToLibrary() }
                        .testTag("popular_show_all_btn")
                )
            }
        }

        // Popular Tracks List
        items(popularTracks, key = { it.id }) { track ->
            PopularTrackItem(
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
fun PopularTrackItem(
    track: Track,
    onPlay: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onPlay() }
            .padding(horizontal = 20.dp, vertical = 7.dp)
            .testTag("popular_track_${track.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Rounded Square Album Art
        Box(
            modifier = Modifier
                .size(54.dp)
                .clip(RoundedCornerShape(14.dp))
                .border(
                    1.dp,
                    Color.White.copy(alpha = 0.15f),
                    RoundedCornerShape(14.dp)
                )
                .background(Color(0xFF16092E))
        ) {
            if (track.title.contains("Blinding", ignoreCase = true)) {
                Image(
                    painter = painterResource(id = R.drawable.img_cover_blinding_lights),
                    contentDescription = track.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                AsyncImage(
                    model = track.albumArtUrl,
                    contentDescription = track.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Title & Category Subtitle
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
                text = track.genre,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 12.sp
                ),
                color = TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

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
                .testTag("popular_play_btn_${track.id}"),
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
