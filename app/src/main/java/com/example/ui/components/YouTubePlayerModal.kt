package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.UnfoldLess
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.R
import com.example.data.youtube.YouTubePlaybackMode
import com.example.data.youtube.YouTubeVideo
import com.example.ui.theme.LocalDistritoTheme

/**
 * Pantalla de reproducción de música rediseñada con estilo premium Crystal Glass
 * para Distrito Music 503.
 *
 * Incluye:
 * - Selector compacto y discreto Audio / Video en la esquina superior derecha
 * - Transición fluida integrada entre reproducción de sólo audio y video panorámico
 * - Portada prominente con iluminación ambiental y reflejos de cristal en modo Audio
 * - Controles ergonómicos de alta gama (SkipPrevious, Play/Pause brillante con efecto especular, SkipNext)
 * - Diseño completamente responsive que respeta insets de navegación y barra de estado
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubePlayerModal(
    video: YouTubeVideo?,
    mode: YouTubePlaybackMode,
    visible: Boolean,
    isPlaying: Boolean,
    hasPrevious: Boolean = true,
    hasNext: Boolean = true,
    onModeChange: (YouTubePlaybackMode) -> Unit,
    onTogglePlayPause: () -> Unit,
    onPlayPrevious: () -> Unit,
    onPlayNext: () -> Unit,
    onMinimize: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!visible || video == null) return

    val theme = LocalDistritoTheme.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    // Simulación de progreso visual para pistas online
    var simulatedProgress by remember(video.id) { mutableFloatStateOf(0.12f) }

    // Animación suave de brillo pulsante en la iluminación ambiental
    val infiniteTransition = rememberInfiniteTransition(label = "ambient_glow_pulse")
    val pulseGlow by infiniteTransition.animateFloat(
        initialValue = 0.22f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_glow"
    )

    // Animación de rotación para halo vinilo cuando se reproduce audio
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ambient_rotation"
    )

    BackHandler {
        onMinimize()
    }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it }) + fadeIn(tween(350)),
        exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(tween(300)),
        modifier = modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF05070D))
                .statusBarsPadding()
                .navigationBarsPadding()
                .testTag("youtube_player_modal")
        ) {
            // Fondo ambiental cinematográfico con gradiente profundo
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color(0xFF090D1A),
                                Color(0xFF060812),
                                Color(0xFF04050A)
                            )
                        )
                    )
            )

            // Iluminación ambiental difusa Morado & Cian detrás del escenario central
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 90.dp)
                    .size(330.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                Color(0xFF8A2BE2).copy(alpha = pulseGlow),
                                Color(0xFF00E5FF).copy(alpha = pulseGlow * 0.45f),
                                Color.Transparent
                            )
                        )
                    )
            )

            // Contenedor principal con scroll suave responsive
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // ==========================================
                // 1. BARRA SUPERIOR: Minimizar + Distrito 503 + Selector Audio / Video
                // ==========================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón discreto para minimizar / cerrar reproductor
                    IconButton(
                        onClick = onMinimize,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(Color(0x2E1E293B))
                            .border(1.dp, Color(0x40FFFFFF), CircleShape)
                            .testTag("minimize_player_top_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Minimizar reproductor",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    // Insignia central Distrito Music 503
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0x20152033))
                            .border(
                                1.dp,
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFF8A2BE2).copy(alpha = 0.5f),
                                        Color(0xFF00E5FF).copy(alpha = 0.5f)
                                    )
                                ),
                                RoundedCornerShape(20.dp)
                            )
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF8A2BE2)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(11.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "DISTRITO MUSIC 503",
                            color = Color(0xFFE2E8F0),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 0.8.sp
                        )
                    }

                    // ==========================================
                    // SELECTOR COMPACTO Y DISCRETO AUDIO / VIDEO
                    // ==========================================
                    AudioVideoGlassSelector(
                        currentMode = mode,
                        onModeSelect = onModeChange,
                        modifier = Modifier.testTag("audio_video_top_selector")
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // ==========================================
                // 2. ELEMENTO CENTRAL: Portada / Video con transición fluida
                // ==========================================
                AnimatedContent(
                    targetState = mode,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(320)) + slideInVertically(
                            animationSpec = tween(320),
                            initialOffsetY = { 20 }
                        )).togetherWith(
                            fadeOut(animationSpec = tween(220))
                        )
                    },
                    label = "audio_video_content_transition",
                    modifier = Modifier.fillMaxWidth()
                ) { currentMode ->
                    if (currentMode == YouTubePlaybackMode.VIDEO) {
                        // 🎬 MODO VIDEO: Marco panorámico 16:9 con borde luminoso de cristal
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(16f / 9f)
                                .shadow(
                                    elevation = 20.dp,
                                    shape = RoundedCornerShape(22.dp),
                                    ambientColor = Color(0xFFFF334B),
                                    spotColor = Color(0xFF8A2BE2)
                                )
                                .clip(RoundedCornerShape(22.dp))
                                .background(Color.Black)
                                .border(
                                    width = 1.5.dp,
                                    brush = Brush.linearGradient(
                                        listOf(
                                            Color(0xFFFF334B).copy(alpha = 0.8f),
                                            Color(0xFF8A2BE2).copy(alpha = 0.6f),
                                            Color(0xFF00E5FF).copy(alpha = 0.5f)
                                        )
                                    ),
                                    shape = RoundedCornerShape(22.dp)
                                )
                        ) {
                            val htmlVideoContent = remember(video.id) {
                                """
                                <!DOCTYPE html>
                                <html>
                                <head>
                                    <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
                                    <style>
                                        body { margin: 0; padding: 0; background-color: #000000; overflow: hidden; }
                                        iframe { width: 100%; height: 100%; border: none; }
                                    </style>
                                </head>
                                <body>
                                    <iframe 
                                        id="ytplayer"
                                        src="https://www.youtube-nocookie.com/embed/${video.id}?autoplay=1&playsinline=1&enablejsapi=1&rel=0&modestbranding=1" 
                                        frameborder="0" 
                                        allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" 
                                        allowfullscreen>
                                    </iframe>
                                    <script>
                                        var player = document.getElementById('ytplayer');
                                        function playVideo() { player.contentWindow.postMessage('{"event":"command","func":"playVideo","args":""}', '*'); }
                                        function pauseVideo() { player.contentWindow.postMessage('{"event":"command","func":"pauseVideo","args":""}', '*'); }
                                    </script>
                                </body>
                                </html>
                                """.trimIndent()
                            }

                            AndroidView(
                                factory = { ctx ->
                                    WebView(ctx).apply {
                                        layoutParams = ViewGroup.LayoutParams(
                                            ViewGroup.LayoutParams.MATCH_PARENT,
                                            ViewGroup.LayoutParams.MATCH_PARENT
                                        )
                                        settings.apply {
                                            javaScriptEnabled = true
                                            domStorageEnabled = true
                                            mediaPlaybackRequiresUserGesture = false
                                            loadWithOverviewMode = true
                                            useWideViewPort = true
                                            cacheMode = WebSettings.LOAD_DEFAULT
                                        }
                                        webChromeClient = WebChromeClient()
                                        webViewClient = object : WebViewClient() {}
                                        loadDataWithBaseURL(
                                            "https://www.youtube.com",
                                            htmlVideoContent,
                                            "text/html",
                                            "UTF-8",
                                            null
                                        )
                                        webViewRef = this
                                    }
                                },
                                update = { webView ->
                                    webViewRef = webView
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    } else {
                        // 🎧 MODO AUDIO: Portada prominente con efecto Crystal Glass
                        // Se mantiene WebView en segundo plano (1dp) para audio ininterrumpido oficial
                        val htmlAudioContent = remember(video.id) {
                            """
                            <!DOCTYPE html>
                            <html>
                            <head>
                                <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
                                <style>
                                    body { margin: 0; padding: 0; background-color: #000000; overflow: hidden; }
                                    iframe { width: 100%; height: 100%; border: none; }
                                </style>
                            </head>
                            <body>
                                <iframe 
                                    id="ytplayer"
                                    src="https://www.youtube-nocookie.com/embed/${video.id}?autoplay=1&playsinline=1&enablejsapi=1&rel=0&modestbranding=1" 
                                    frameborder="0" 
                                    allow="accelerometer; autoplay; clipboard-write; encrypted-media; gyroscope; picture-in-picture" 
                                    allowfullscreen>
                                </iframe>
                                <script>
                                    var player = document.getElementById('ytplayer');
                                    function playVideo() { player.contentWindow.postMessage('{"event":"command","func":"playVideo","args":""}', '*'); }
                                    function pauseVideo() { player.contentWindow.postMessage('{"event":"command","func":"pauseVideo","args":""}', '*'); }
                                </script>
                            </body>
                            </html>
                            """.trimIndent()
                        }

                        Box(
                            modifier = Modifier
                                .size(1.dp)
                                .alpha(0.01f)
                        ) {
                            AndroidView(
                                factory = { ctx ->
                                    WebView(ctx).apply {
                                        layoutParams = ViewGroup.LayoutParams(1, 1)
                                        settings.apply {
                                            javaScriptEnabled = true
                                            domStorageEnabled = true
                                            mediaPlaybackRequiresUserGesture = false
                                            loadWithOverviewMode = true
                                            useWideViewPort = true
                                        }
                                        webChromeClient = WebChromeClient()
                                        webViewClient = object : WebViewClient() {}
                                        loadDataWithBaseURL(
                                            "https://www.youtube.com",
                                            htmlAudioContent,
                                            "text/html",
                                            "UTF-8",
                                            null
                                        )
                                        webViewRef = this
                                    }
                                },
                                update = { webView ->
                                    webViewRef = webView
                                }
                            )
                        }

                        // Portada principal estilizada
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            // Halo ambiental giratorio suave
                            Box(
                                modifier = Modifier
                                    .size(290.dp)
                                    .clip(RoundedCornerShape(32.dp))
                                    .rotate(if (isPlaying) rotation else 0f)
                                    .background(
                                        Brush.sweepGradient(
                                            listOf(
                                                Color(0xFF8A2BE2).copy(alpha = 0.28f),
                                                Color(0xFF00E5FF).copy(alpha = 0.20f),
                                                Color(0xFFFF334B).copy(alpha = 0.15f),
                                                Color(0xFF8A2BE2).copy(alpha = 0.28f)
                                            )
                                        )
                                    )
                            )

                            // Contenedor de Carátula Crystal Glass
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(0.86f)
                                    .widthIn(max = 300.dp)
                                    .aspectRatio(1f)
                                    .shadow(
                                        elevation = 28.dp,
                                        shape = RoundedCornerShape(26.dp),
                                        ambientColor = Color(0xFF8A2BE2),
                                        spotColor = Color(0xFF00E5FF)
                                    )
                                    .clip(RoundedCornerShape(26.dp))
                                    .background(Color(0xFF101422))
                                    .border(
                                        width = 1.8.dp,
                                        brush = Brush.linearGradient(
                                            listOf(
                                                Color.White.copy(alpha = 0.6f),
                                                Color(0xFF8A2BE2).copy(alpha = 0.45f),
                                                Color(0xFF00E5FF).copy(alpha = 0.6f)
                                            )
                                        ),
                                        shape = RoundedCornerShape(26.dp)
                                    )
                            ) {
                                AsyncImage(
                                    model = video.thumbnailUrl,
                                    contentDescription = video.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )

                                // Reflejo especular superior de cristal
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(110.dp)
                                        .background(
                                            Brush.verticalGradient(
                                                listOf(
                                                    Color.White.copy(alpha = 0.22f),
                                                    Color.Transparent
                                                )
                                            )
                                        )
                                )

                                // Insignia de Calidad de Audio Crystal Glass
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(14.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xCC070B16))
                                        .border(
                                            1.dp,
                                            Brush.horizontalGradient(
                                                listOf(
                                                    Color(0xFF8A2BE2).copy(alpha = 0.6f),
                                                    Color(0xFF00E5FF).copy(alpha = 0.6f)
                                                )
                                            ),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .padding(horizontal = 10.dp, vertical = 5.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Headphones,
                                            contentDescription = null,
                                            tint = Color(0xFF00E5FF),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "AUDIO HD 503",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ==========================================
                // 3. INFORMACIÓN DE LA CANCIÓN: Título, Canal y Duración
                // ==========================================
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Text(
                        text = video.title,
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 26.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = video.channelTitle,
                            color = Color(0xFF94A3B8),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (!video.durationFormatted.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0x331E293B))
                                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = video.durationFormatted,
                                    color = Color(0xFF00E5FF),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // ==========================================
                // 4. BARRA DE PROGRESO & CONTROLES DE TIEMPO (Animada estilo iStock 1289638909)
                // ==========================================
                Column(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AnimatedWaveformProgressBar(
                        progress = simulatedProgress,
                        isPlaying = isPlaying,
                        onSeek = { simulatedProgress = it },
                        height = 38.dp,
                        style = WaveformProgressStyle.NEON_EQUALIZER_BARS,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .testTag("online_player_seek_slider")
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "01:15",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )

                        // Botones de salto rápido -10s / +10s
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x221E293B))
                                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                                    .clickable {
                                        simulatedProgress = (simulatedProgress - 0.05f).coerceAtLeast(0f)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Replay10,
                                        contentDescription = "Retroceder 10s",
                                        tint = Color(0xFFE2E8F0),
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "-10s",
                                        color = Color(0xFFE2E8F0),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0x221E293B))
                                    .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(12.dp))
                                    .clickable {
                                        simulatedProgress = (simulatedProgress + 0.05f).coerceAtMost(1f)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "+10s",
                                        color = Color(0xFFE2E8F0),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Icon(
                                        imageVector = Icons.Default.Forward10,
                                        contentDescription = "Adelantar 10s",
                                        tint = Color(0xFFE2E8F0),
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = video.durationFormatted ?: "03:45",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // ==========================================
                // 5. CONTROLES DE REPRODUCCIÓN (Previo, Gran Play/Pause, Siguiente)
                // ==========================================
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón Anterior
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color(0x241E293B))
                            .border(1.dp, Color(0x40FFFFFF), CircleShape)
                            .clickable(enabled = hasPrevious, onClick = onPlayPrevious)
                            .testTag("audio_previous_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Canción anterior",
                            tint = if (hasPrevious) Color.White else Color(0x66FFFFFF),
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Botón Central Grand Play / Pause con reflejo especular Liquid Glass
                    Box(
                        modifier = Modifier
                            .size(74.dp)
                            .shadow(
                                elevation = 22.dp,
                                shape = CircleShape,
                                ambientColor = Color(0xFF8A2BE2),
                                spotColor = Color(0xFF00E5FF)
                            )
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF8A2BE2),
                                        Color(0xFF00C6FF)
                                    )
                                )
                            )
                            .border(
                                width = 1.6.dp,
                                brush = Brush.verticalGradient(
                                    colors = listOf(
                                        Color.White.copy(alpha = 0.75f),
                                        Color.White.copy(alpha = 0.20f)
                                    )
                                ),
                                shape = CircleShape
                            )
                            .clickable {
                                onTogglePlayPause()
                                if (isPlaying) {
                                    webViewRef?.evaluateJavascript("pauseVideo();", null)
                                } else {
                                    webViewRef?.evaluateJavascript("playVideo();", null)
                                }
                            }
                            .testTag("audio_play_pause_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        // Reflejo de cristal superior
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            Color.White.copy(alpha = 0.35f),
                                            Color.Transparent
                                        ),
                                        startY = 0f,
                                        endY = 36f
                                    )
                                )
                        )

                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pausar" else "Reproducir",
                            tint = Color.White,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    // Botón Siguiente
                    Box(
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .background(Color(0x241E293B))
                            .border(1.dp, Color(0x40FFFFFF), CircleShape)
                            .clickable(enabled = hasNext, onClick = onPlayNext)
                            .testTag("audio_next_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Siguiente canción",
                            tint = if (hasNext) Color.White else Color(0x66FFFFFF),
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // ==========================================
                // 6. ACCIONES COMPLEMENTARIAS: Minimizar & Abrir en YouTube
                // ==========================================
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botón Minimizar y continuar
                    Button(
                        onClick = onMinimize,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0x241E293B),
                            contentColor = Color(0xFF00E5FF)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFF00E5FF).copy(alpha = 0.45f), Color(0xFF8A2BE2).copy(alpha = 0.45f))
                            )
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("minimize_for_navigation_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.UnfoldLess,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Minimizar",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Botón Abrir en YouTube app
                    Button(
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(video.watchUrl)).apply {
                                setPackage("com.google.android.youtube")
                            }
                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(video.watchUrl)))
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0x241E293B),
                            contentColor = Color(0xFFFF5252)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.horizontalGradient(
                                listOf(Color(0xFFFF334B).copy(alpha = 0.5f), Color(0xFFFF5252).copy(alpha = 0.3f))
                            )
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("open_in_youtube_app_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "YouTube ↗",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }

    DisposableEffect(video.id) {
        onDispose {
            webViewRef?.destroy()
            webViewRef = null
        }
    }
}

/**
 * Selector compacto, elegante y discreto de modo Audio / Video con diseño Crystal Glass.
 * Ubicado en la esquina superior derecha del reproductor sin estorbar la portada ni controles.
 */
@Composable
fun AudioVideoGlassSelector(
    currentMode: YouTubePlaybackMode,
    onModeSelect: (YouTubePlaybackMode) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0x33101828))
            .border(
                1.dp,
                Brush.horizontalGradient(
                    listOf(
                        Color(0xFF8A2BE2).copy(alpha = 0.6f),
                        Color(0xFF00E5FF).copy(alpha = 0.6f)
                    )
                ),
                RoundedCornerShape(20.dp)
            )
            .padding(3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Opción 🎧 Audio
        val isAudio = currentMode == YouTubePlaybackMode.AUDIO_ONLY
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .then(
                    if (isAudio) {
                        Modifier.background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFF8A2BE2),
                                    Color(0xFF6A11CB)
                                )
                            )
                        )
                    } else {
                        Modifier
                    }
                )
                .clickable { onModeSelect(YouTubePlaybackMode.AUDIO_ONLY) }
                .padding(horizontal = 9.dp, vertical = 6.dp)
                .testTag("selector_audio_mode"),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Headphones,
                    contentDescription = "Modo Audio",
                    tint = if (isAudio) Color.White else Color(0xFF94A3B8),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Audio",
                    color = if (isAudio) Color.White else Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = if (isAudio) FontWeight.Bold else FontWeight.Medium
                )
            }
        }

        // Opción 🎬 Video
        val isVideo = currentMode == YouTubePlaybackMode.VIDEO
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .then(
                    if (isVideo) {
                        Modifier.background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFFFF334B),
                                    Color(0xFFD61A3C)
                                )
                            )
                        )
                    } else {
                        Modifier
                    }
                )
                .clickable { onModeSelect(YouTubePlaybackMode.VIDEO) }
                .padding(horizontal = 9.dp, vertical = 6.dp)
                .testTag("selector_video_mode"),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.SmartDisplay,
                    contentDescription = "Modo Video",
                    tint = if (isVideo) Color.White else Color(0xFF94A3B8),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Video",
                    color = if (isVideo) Color.White else Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = if (isVideo) FontWeight.Bold else FontWeight.Medium
                )
            }
        }
    }
}
