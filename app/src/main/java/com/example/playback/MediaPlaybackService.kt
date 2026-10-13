package com.example.playback

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import com.example.data.model.Track

class MediaPlaybackService : Service() {

    companion object {
        const val CHANNEL_ID = "soundwave_playback_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_PLAY = "com.example.ACTION_PLAY"
        const val ACTION_PAUSE = "com.example.ACTION_PAUSE"
        const val ACTION_NEXT = "com.example.ACTION_NEXT"
        const val ACTION_PREV = "com.example.ACTION_PREV"
        const val ACTION_STOP = "com.example.ACTION_STOP"

        var serviceInstance: MediaPlaybackService? = null
    }

    private val binder = LocalBinder()
    var playbackManager: MediaPlaybackManager? = null

    var onPlayAction: (() -> Unit)? = null
    var onPauseAction: (() -> Unit)? = null
    var onNextAction: (() -> Unit)? = null
    var onPreviousAction: (() -> Unit)? = null

    inner class LocalBinder : Binder() {
        fun getService(): MediaPlaybackService = this@MediaPlaybackService
    }

    override fun onCreate() {
        super.onCreate()
        serviceInstance = this
        createNotificationChannel()

        playbackManager = MediaPlaybackManager(
            context = this,
            onPlayRequested = { onPlayAction?.invoke() },
            onPauseRequested = { onPauseAction?.invoke() },
            onNextRequested = { onNextAction?.invoke() },
            onPreviousRequested = { onPreviousAction?.invoke() }
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY -> onPlayAction?.invoke()
            ACTION_PAUSE -> onPauseAction?.invoke()
            ACTION_NEXT -> onNextAction?.invoke()
            ACTION_PREV -> onPreviousAction?.invoke()
            ACTION_STOP -> {
                onPauseAction?.invoke()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_NOT_STICKY
    }

    var mediaSessionToken: android.media.session.MediaSession.Token? = null

    fun updateNotification(track: Track?, isPlaying: Boolean) {
        if (track == null) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            return
        }

        playbackManager?.updateTrack(track)

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val playPauseActionIntent = Intent(this, MediaPlaybackService::class.java).apply {
            action = if (isPlaying) ACTION_PAUSE else ACTION_PLAY
        }
        val playPausePendingIntent = PendingIntent.getService(
            this, 1, playPauseActionIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val nextActionIntent = Intent(this, MediaPlaybackService::class.java).apply {
            action = ACTION_NEXT
        }
        val nextPendingIntent = PendingIntent.getService(
            this, 2, nextActionIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val prevActionIntent = Intent(this, MediaPlaybackService::class.java).apply {
            action = ACTION_PREV
        }
        val prevPendingIntent = PendingIntent.getService(
            this, 3, prevActionIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val effectiveToken = mediaSessionToken ?: playbackManager?.sessionToken

        val prevAction = Notification.Action.Builder(
            android.graphics.drawable.Icon.createWithResource(this, android.R.drawable.ic_media_previous),
            "Anterior",
            prevPendingIntent
        ).build()

        val playPauseAction = Notification.Action.Builder(
            android.graphics.drawable.Icon.createWithResource(
                this,
                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
            ),
            if (isPlaying) "Pausa" else "Reproducir",
            playPausePendingIntent
        ).build()

        val nextAction = Notification.Action.Builder(
            android.graphics.drawable.Icon.createWithResource(this, android.R.drawable.ic_media_next),
            "Siguiente",
            nextPendingIntent
        ).build()

        val builder = Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(track.title)
            .setContentText("${track.artist} • Distrito Music 503")
            .setContentIntent(contentPendingIntent)
            .setVisibility(Notification.VISIBILITY_PUBLIC)
            .setOngoing(isPlaying)
            .addAction(prevAction)
            .addAction(playPauseAction)
            .addAction(nextAction)

        if (effectiveToken != null) {
            val mediaStyle = Notification.MediaStyle()
                .setMediaSession(effectiveToken)
                .setShowActionsInCompactView(0, 1, 2)
            builder.setStyle(mediaStyle)
        }

        val notification = builder.build()
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Distrito Music 503 Reproducción",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Controles de reproducción multimedia y pantalla bloqueada"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        serviceInstance = null
        playbackManager?.release()
        playbackManager = null
        super.onDestroy()
    }
}
