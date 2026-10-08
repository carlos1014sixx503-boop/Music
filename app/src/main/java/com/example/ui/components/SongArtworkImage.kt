package com.example.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.theme.LocalDistritoTheme

@Composable
fun SongArtworkImage(
    albumArtUri: String?,
    @DrawableRes coverResId: Int? = null,
    contentDescription: String? = null,
    modifier: Modifier = Modifier
) {
    val theme = LocalDistritoTheme.current

    if (!albumArtUri.isNullOrBlank()) {
        AsyncImage(
            model = albumArtUri,
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier,
            error = painterResource(id = coverResId ?: R.drawable.distrito_logo),
            placeholder = painterResource(id = coverResId ?: R.drawable.distrito_logo)
        )
    } else if (coverResId != null) {
        Image(
            painter = painterResource(id = coverResId),
            contentDescription = contentDescription,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier.background(theme.surfaceVariantColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                tint = theme.primaryAccent,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
