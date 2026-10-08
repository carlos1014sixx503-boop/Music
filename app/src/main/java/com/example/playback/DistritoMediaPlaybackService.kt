package com.example.playback

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.example.MainActivity
import com.example.R
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture

/**
 * Servicio en segundo plano oficial para reproducción multimedia basado en AndroidX Media3.
 * Provee:
 * - Ciclo de vida independiente de la Activity / UI
 * - Notificación multimedia en primer plano (Foreground Service)
 * - Controles completos en pantalla bloqueada (Lock Screen)
 * - Compatibilidad con botones de audífonos (cableados y Bluetooth AVRCP)
 * - Gestión automática de foco de audio (pausa con llamadas y navegación)
 * - Desconexión segura (becoming noisy: pausa al desconectar auriculares)
 * - WakeLock local para mantener el audio activo con la pantalla apagada
 */
@androidx.annotation.OptIn(UnstableApi::class)
class DistritoMediaPlaybackService : MediaSessionService() {

    private var mediaSession: MediaSession? = null
    private var exoPlayer: ExoPlayer? = null

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "DistritoMediaPlaybackService creado (onCreate)")

        createNotificationChannel()

        // Configuración de ExoPlayer para audio en segundo plano
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MUSIC)
            .setUsage(C.USAGE_MEDIA)
            .build()

        val player = ExoPlayer.Builder(this)
            .setAudioAttributes(audioAttributes, /* handleAudioFocus = */ true)
            .setHandleAudioBecomingNoisy(true)
            .setWakeMode(C.WAKE_MODE_LOCAL)
            .build()

        exoPlayer = player

        // Intent para reabrir MainActivity desde la notificación multimedia o pantalla bloqueada
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val sessionActivityPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Callback para MediaSession: autoriza comandos de MediaController y reproducción
        val callback = object : MediaSession.Callback {
            override fun onConnect(
                session: MediaSession,
                controller: MediaSession.ControllerInfo
            ): MediaSession.ConnectionResult {
                Log.d(TAG, "MediaSession.Callback.onConnect desde: ${controller.packageName} (versión ${controller.controllerVersion})")
                val connectionResult = super.onConnect(session, controller)
                val availableSessionCommands = connectionResult.availableSessionCommands.buildUpon()
                val availablePlayerCommands = connectionResult.availablePlayerCommands.buildUpon()
                    .add(Player.COMMAND_PLAY_PAUSE)
                    .add(Player.COMMAND_SEEK_TO_NEXT)
                    .add(Player.COMMAND_SEEK_TO_PREVIOUS)
                    .add(Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
                    .add(Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
                    .add(Player.COMMAND_SEEK_BACK)
                    .add(Player.COMMAND_SEEK_FORWARD)
                    .add(Player.COMMAND_STOP)
                    .add(Player.COMMAND_SET_SPEED_AND_PITCH)
                    .add(Player.COMMAND_CHANGE_MEDIA_ITEMS)
                    .build()

                return MediaSession.ConnectionResult.accept(
                    availableSessionCommands.build(),
                    availablePlayerCommands
                )
            }

            override fun onAddMediaItems(
                mediaSession: MediaSession,
                controller: MediaSession.ControllerInfo,
                mediaItems: MutableList<MediaItem>
            ): ListenableFuture<MutableList<MediaItem>> {
                return Futures.immediateFuture(mediaItems)
            }
        }

        val session = MediaSession.Builder(this, player)
            .setId("distrito_music_session")
            .setSessionActivity(sessionActivityPendingIntent)
            .setCallback(callback)
            .build()

        mediaSession = session
        Log.d(TAG, "MediaSession creada y activa: id=${session.id}, sessionActivity configurada")

        // Registrar este player y session en DistritoAudioEngine para sincronización
        DistritoAudioEngine.getInstance(applicationContext).attachServicePlayer(player, session)

        // Configuración del proveedor oficial de notificación multimedia Media3
        try {
            val notificationProvider = DefaultMediaNotificationProvider.Builder(this)
                .setChannelId(CHANNEL_ID)
                .setChannelName(R.string.playback_notification_channel_name)
                .build()
            notificationProvider.setSmallIcon(R.drawable.ic_music_notification)
            setMediaNotificationProvider(notificationProvider)
            Log.d(TAG, "DefaultMediaNotificationProvider configurado exitosamente con canal: $CHANNEL_ID")
        } catch (e: Exception) {
            Log.e(TAG, "Error configurando MediaNotificationProvider: ${e.message}", e)
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "DistritoMediaPlaybackService onStartCommand (startId=$startId, action=${intent?.action})")
        super.onStartCommand(intent, flags, startId)
        return START_STICKY
    }

    override fun onUpdateNotification(session: MediaSession, startInForegroundRequired: Boolean) {
        Log.d(
            TAG,
            "onUpdateNotification: sesión=${session.id}, isPlaying=${session.player.isPlaying}, " +
                "playWhenReady=${session.player.playWhenReady}, itemsCount=${session.player.mediaItemCount}, " +
                "startInForegroundRequired=$startInForegroundRequired"
        )
        super.onUpdateNotification(session, startInForegroundRequired)
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        Log.d(TAG, "onGetSession solicitado por controller: ${controllerInfo.packageName}")
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            Log.d(TAG, "onTaskRemoved: deteniendo servicio (no hay reproducción activa)")
            stopSelf()
        } else {
            Log.d(TAG, "onTaskRemoved: manteniendo servicio activo en segundo plano (reproducción en curso)")
        }
    }

    override fun onDestroy() {
        Log.d(TAG, "DistritoMediaPlaybackService onDestroy: liberando MediaSession y ExoPlayer")
        DistritoAudioEngine.getInstance(applicationContext).detachServicePlayer()
        mediaSession?.run {
            release()
            mediaSession = null
        }
        exoPlayer?.run {
            release()
            exoPlayer = null
        }
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.playback_notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Reproducción de música en segundo plano y pantalla bloqueada"
                setShowBadge(false)
                lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
            Log.d(TAG, "Canal de notificación asegurado: $CHANNEL_ID")
        }
    }

    companion object {
        const val CHANNEL_ID = "distrito_media_playback_channel"
        private const val TAG = "DistritoMediaPlayback"
    }
}
