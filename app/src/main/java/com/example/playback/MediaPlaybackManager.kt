package com.example.playback

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.session.MediaSession
import android.media.session.PlaybackState
import android.os.Build
import android.util.Log
import android.view.KeyEvent
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.Track

class MediaPlaybackManager(
    private val context: Context,
    private val onPlayRequested: () -> Unit,
    private val onPauseRequested: () -> Unit,
    private val onNextRequested: () -> Unit,
    private val onPreviousRequested: () -> Unit
) {

    private val tag = "MediaPlaybackManager"
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var mediaSession: MediaSession? = null
    private var audioFocusRequest: AudioFocusRequest? = null
    private var isNoisyReceiverRegistered = false

    private val becomingNoisyReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
                Log.d(tag, "Headphones/Speakers disconnected (AUDIO_BECOMING_NOISY). Pausing playback.")
                onPauseRequested()
            }
        }
    }

    private val audioFocusChangeListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        when (focusChange) {
            AudioManager.AUDIOFOCUS_LOSS -> {
                Log.d(tag, "Audio focus lost permanently. Pausing.")
                onPauseRequested()
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                Log.d(tag, "Audio focus lost transiently. Pausing.")
                onPauseRequested()
            }
            AudioManager.AUDIOFOCUS_GAIN -> {
                Log.d(tag, "Audio focus gained.")
            }
        }
    }

    init {
        setupMediaSession()
        registerNoisyReceiver()
    }

    private fun setupMediaSession() {
        try {
            mediaSession = MediaSession(context, "SoundWaveMediaSession").apply {
                setCallback(object : MediaSession.Callback() {
                    override fun onPlay() {
                        Log.d(tag, "MediaSession: onPlay received from headset/system")
                        requestAudioFocus()
                        onPlayRequested()
                    }

                    override fun onPause() {
                        Log.d(tag, "MediaSession: onPause received from headset/system")
                        onPauseRequested()
                    }

                    override fun onSkipToNext() {
                        Log.d(tag, "MediaSession: onSkipToNext received")
                        onNextRequested()
                    }

                    override fun onSkipToPrevious() {
                        Log.d(tag, "MediaSession: onSkipToPrevious received")
                        onPreviousRequested()
                    }

                    override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {
                        val keyEvent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT, KeyEvent::class.java)
                        } else {
                            @Suppress("DEPRECATION")
                            mediaButtonIntent.getParcelableExtra(Intent.EXTRA_KEY_EVENT)
                        }

                        if (keyEvent != null && keyEvent.action == KeyEvent.ACTION_DOWN) {
                            when (keyEvent.keyCode) {
                                KeyEvent.KEYCODE_MEDIA_PLAY -> {
                                    onPlay()
                                    return true
                                }
                                KeyEvent.KEYCODE_MEDIA_PAUSE -> {
                                    onPause()
                                    return true
                                }
                                KeyEvent.KEYCODE_HEADSETHOOK,
                                KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                                    // Toggle play/pause
                                    if (isPlayingState) {
                                        onPause()
                                    } else {
                                        onPlay()
                                    }
                                    return true
                                }
                                KeyEvent.KEYCODE_MEDIA_NEXT -> {
                                    onSkipToNext()
                                    return true
                                }
                                KeyEvent.KEYCODE_MEDIA_PREVIOUS -> {
                                    onSkipToPrevious()
                                    return true
                                }
                            }
                        }
                        return super.onMediaButtonEvent(mediaButtonIntent)
                    }
                })

                val mediaButtonReceiverIntent = Intent(Intent.ACTION_MEDIA_BUTTON).apply {
                    setPackage(context.packageName)
                }
                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    0,
                    mediaButtonReceiverIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
                setMediaButtonReceiver(pendingIntent)
                isActive = true
            }
        } catch (e: Exception) {
            Log.e(tag, "Error setting up MediaSession: ${e.message}", e)
        }
    }

    private var isPlayingState = false

    fun updatePlaybackState(isPlaying: Boolean, positionMs: Long) {
        isPlayingState = isPlaying
        try {
            val state = if (isPlaying) PlaybackState.STATE_PLAYING else PlaybackState.STATE_PAUSED
            val actions = PlaybackState.ACTION_PLAY or
                    PlaybackState.ACTION_PAUSE or
                    PlaybackState.ACTION_PLAY_PAUSE or
                    PlaybackState.ACTION_SKIP_TO_NEXT or
                    PlaybackState.ACTION_SKIP_TO_PREVIOUS or
                    PlaybackState.ACTION_SEEK_TO

            val playbackState = PlaybackState.Builder()
                .setActions(actions)
                .setState(state, positionMs, 1.0f)
                .build()

            mediaSession?.setPlaybackState(playbackState)
            if (isPlaying) {
                requestAudioFocus()
            }
        } catch (e: Exception) {
            Log.e(tag, "Failed to update playback state: ${e.message}")
        }
    }

    fun requestAudioFocus(): Boolean {
        return try {
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .build()

            val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
                .setAudioAttributes(audioAttributes)
                .setAcceptsDelayedFocusGain(true)
                .setOnAudioFocusChangeListener(audioFocusChangeListener)
                .build()

            audioFocusRequest = focusRequest
            val result = audioManager.requestAudioFocus(focusRequest)
            result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } catch (e: Exception) {
            Log.w(tag, "Audio focus request failed: ${e.message}")
            true
        }
    }

    fun abandonAudioFocus() {
        try {
            audioFocusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } catch (e: Exception) {
            Log.w(tag, "Error abandoning audio focus: ${e.message}")
        }
    }

    private fun registerNoisyReceiver() {
        if (!isNoisyReceiverRegistered) {
            try {
                val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
                context.registerReceiver(becomingNoisyReceiver, filter)
                isNoisyReceiverRegistered = true
            } catch (e: Exception) {
                Log.e(tag, "Failed to register becomingNoisyReceiver: ${e.message}")
            }
        }
    }

    fun release() {
        abandonAudioFocus()
        if (isNoisyReceiverRegistered) {
            try {
                context.unregisterReceiver(becomingNoisyReceiver)
                isNoisyReceiverRegistered = false
            } catch (e: Exception) {
                Log.w(tag, "Failed to unregister becomingNoisyReceiver: ${e.message}")
            }
        }
        mediaSession?.isActive = false
        mediaSession?.release()
        mediaSession = null
    }
}
