package com.example.ui.components

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlaylistAdd
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.youtube.YouTubeVideo
import com.example.state.MusicPlayerState
import com.example.ui.theme.AccentPink
import com.example.ui.theme.DeepVioletBackground
import com.example.ui.theme.DeepVioletCard
import com.example.ui.theme.DeepVioletElevated
import com.example.ui.theme.NeonPurple
import com.example.ui.theme.NeonPurpleLight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class YouTubeBridge(
    private val onTimeUpdate: (Float, Float) -> Unit,
    private val onStateChange: (Int) -> Unit,
    private val onReady: () -> Unit
) {
    @JavascriptInterface
    fun onTime(current: Float, total: Float) {
        onTimeUpdate(current, total)
    }

    @JavascriptInterface
    fun onState(state: Int) {
        onStateChange(state)
    }

    @JavascriptInterface
    fun onPlayerReady() {
        onReady()
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun YouTubePlayerModal(
    video: YouTubeVideo,
    playerState: MusicPlayerState,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var webViewInstance by remember { mutableStateOf<WebView?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isPlayerReady by remember { mutableStateOf(false) }

    // Dynamic video tracking
    var currentSec by remember { mutableFloatStateOf(0f) }
    var totalDurationSec by remember { mutableFloatStateOf((video.durationMs / 1000f).coerceAtLeast(1f)) }
    var isVideoPlaying by remember { mutableStateOf(true) }

    val isFavorite = playerState.isTrackFavorite("yt_${video.videoId}")

    BackHandler {
        onDismiss()
    }

    val htmlData = remember(video.videoId) {
        """
        <!DOCTYPE html>
        <html>
        <head>
          <meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no">
          <style>
            * { margin: 0; padding: 0; box-sizing: border-box; }
            html, body { width: 100%; height: 100%; background: #000; overflow: hidden; }
            #player { width: 100%; height: 100%; }
          </style>
        </head>
        <body>
          <div id="player"></div>
          <script src="https://www.youtube.com/iframe_api"></script>
          <script>
            var player;
            var pollTimer;

            function onYouTubeIframeAPIReady() {
              player = new YT.Player('player', {
                videoId: '${video.videoId}',
                playerVars: {
                  'autoplay': 1,
                  'playsinline': 1,
                  'controls': 1,
                  'modestbranding': 1,
                  'rel': 0,
                  'fs': 1,
                  'origin': 'https://www.youtube.com'
                },
                events: {
                  'onReady': onPlayerReady,
                  'onStateChange': onPlayerStateChange
                }
              });
            }

            function onPlayerReady(event) {
              if (window.AndroidBridge && window.AndroidBridge.onPlayerReady) {
                window.AndroidBridge.onPlayerReady();
              }
              startPolling();
            }

            function onPlayerStateChange(event) {
              if (window.AndroidBridge && window.AndroidBridge.onState) {
                window.AndroidBridge.onState(event.data);
              }
              if (event.data === YT.PlayerState.PLAYING) {
                startPolling();
              } else {
                sendTime();
              }
            }

            function startPolling() {
              if (pollTimer) clearInterval(pollTimer);
              pollTimer = setInterval(sendTime, 300);
            }

            function sendTime() {
              if (player && typeof player.getCurrentTime === 'function' && typeof player.getDuration === 'function') {
                var c = player.getCurrentTime();
                var d = player.getDuration();
                if (window.AndroidBridge && window.AndroidBridge.onTime) {
                  window.AndroidBridge.onTime(c, d);
                }
              }
            }

            function playVideo() { if (player && player.playVideo) player.playVideo(); }
            function pauseVideo() { if (player && player.pauseVideo) player.pauseVideo(); }
            function seek(s) { if (player && player.seekTo) player.seekTo(s, true); }
          </script>
        </body>
        </html>
        """.trimIndent()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = DeepVioletBackground
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("dismiss_modal_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Minimize player",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "STREAMING FROM YOUTUBE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                letterSpacing = 1.sp,
                                fontWeight = FontWeight.Bold
                            ),
                            color = NeonPurpleLight
                        )
                        Text(
                            text = video.channelTitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.7f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    IconButton(
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://www.youtube.com/watch?v=${video.videoId}")
                            )
                            context.startActivity(intent)
                        },
                        modifier = Modifier.testTag("open_external_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Open in YouTube",
                            tint = Color.White
                        )
                    }
                }

                // Video Player Container (16:9 Aspect Ratio)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color.Black)
                        .aspectRatio(16f / 9f),
                    contentAlignment = Alignment.Center
                ) {
                    AndroidView(
                        factory = { ctx ->
                            WebView(ctx).apply {
                                settings.apply {
                                    javaScriptEnabled = true
                                    domStorageEnabled = true
                                    mediaPlaybackRequiresUserGesture = false
                                    loadWithOverviewMode = true
                                    useWideViewPort = true
                                    cacheMode = WebSettings.LOAD_DEFAULT
                                }
                                webChromeClient = WebChromeClient()
                                webViewClient = object : WebViewClient() {
                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                        isLoading = false
                                    }
                                }
                                addJavascriptInterface(
                                    YouTubeBridge(
                                        onTimeUpdate = { curr, total ->
                                            currentSec = curr
                                            if (total > 0f) totalDurationSec = total
                                            coroutineScope.launch(Dispatchers.Main) {
                                                playerState.syncYouTubeProgress(curr, total, isVideoPlaying)
                                            }
                                        },
                                        onStateChange = { state ->
                                            // YT.PlayerState: 1 = PLAYING, 2 = PAUSED, 0 = ENDED
                                            isVideoPlaying = (state == 1)
                                            coroutineScope.launch(Dispatchers.Main) {
                                                playerState.setPlaying(isVideoPlaying)
                                                if (state == 0) {
                                                    playerState.playNext()
                                                }
                                            }
                                        },
                                        onReady = {
                                            isPlayerReady = true
                                            isLoading = false
                                        }
                                    ),
                                    "AndroidBridge"
                                )
                                loadDataWithBaseURL("https://www.youtube.com", htmlData, "text/html", "UTF-8", null)
                                webViewInstance = this
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    if (isLoading) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.6f)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = NeonPurple)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Title and Channel Card
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = video.title,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                color = Color.White,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${video.channelTitle} • ${video.viewCountFormatted}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = { playerState.toggleFavorite("yt_${video.videoId}") },
                            modifier = Modifier.testTag("toggle_favorite_button")
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (isFavorite) AccentPink else Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Synchronized Waveform Progress Bar
                val currentPositionMs = (currentSec * 1000f).toLong()
                val durationMs = (totalDurationSec * 1000f).toLong().coerceAtLeast(1000L)

                Box(modifier = Modifier.padding(horizontal = 24.dp)) {
                    AnimatedWaveformProgressBar(
                        currentPositionMs = currentPositionMs,
                        durationMs = durationMs,
                        isPlaying = isVideoPlaying,
                        onSeek = { targetMs ->
                            val targetSec = targetMs / 1000f
                            currentSec = targetSec
                            webViewInstance?.evaluateJavascript("seek($targetSec);", null)
                            playerState.seekTo(targetMs)
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Player Controls Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Replay 10s
                    IconButton(
                        onClick = {
                            val newSec = (currentSec - 10f).coerceAtLeast(0f)
                            currentSec = newSec
                            webViewInstance?.evaluateJavascript("seek($newSec);", null)
                        },
                        modifier = Modifier.testTag("seek_backward_10_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay10,
                            contentDescription = "Rewind 10 seconds",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Skip Previous
                    IconButton(
                        onClick = { playerState.playPrevious() },
                        modifier = Modifier.testTag("previous_track_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous Track",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Primary Play/Pause Button
                    FilledIconButton(
                        onClick = {
                            if (isVideoPlaying) {
                                webViewInstance?.evaluateJavascript("pauseVideo();", null)
                                isVideoPlaying = false
                                playerState.setPlaying(false)
                            } else {
                                webViewInstance?.evaluateJavascript("playVideo();", null)
                                isVideoPlaying = true
                                playerState.setPlaying(true)
                            }
                        },
                        modifier = Modifier
                            .size(68.dp)
                            .testTag("play_pause_button"),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = NeonPurple,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(
                            imageVector = if (isVideoPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isVideoPlaying) "Pause" else "Play",
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    // Skip Next
                    IconButton(
                        onClick = { playerState.playNext() },
                        modifier = Modifier.testTag("next_track_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next Track",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Forward 10s
                    IconButton(
                        onClick = {
                            val newSec = (currentSec + 10f).coerceAtMost(totalDurationSec)
                            currentSec = newSec
                            webViewInstance?.evaluateJavascript("seek($newSec);", null)
                        },
                        modifier = Modifier.testTag("seek_forward_10_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forward10,
                            contentDescription = "Forward 10 seconds",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Bottom Queue / Info Card
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = DeepVioletElevated
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Add to Playback Queue",
                                style = MaterialTheme.typography.titleMedium,
                                color = Color.White
                            )
                            Text(
                                text = "Keep music playing seamlessly",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = {
                                playerState.addToQueue(video.toTrack())
                            },
                            modifier = Modifier.testTag("add_to_queue_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlaylistAdd,
                                contentDescription = "Add to queue",
                                tint = NeonPurpleLight
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    DisposableEffect(video.videoId) {
        onDispose {
            webViewInstance?.destroy()
            webViewInstance = null
        }
    }
}
