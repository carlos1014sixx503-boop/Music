package com.example.playback

import com.example.data.Song

data class AudioStats(
    val title: String,
    val artist: String,
    val album: String,
    val codec: String,
    val containerFormat: String,
    val sampleRateHz: Int,
    val bitDepthBits: Int,
    val bitrateKbps: Int,
    val channelCount: Int,
    val audioSessionId: Int,
    val bufferHealthPercent: Int,
    val bufferBufferedMs: Long,
    val currentPositionMs: Long,
    val totalDurationMs: Long,
    val playbackSpeed: Float,
    val isDspActive: Boolean,
    val equalizerPreset: String,
    val bassBoostPercent: Int,
    val virtualizerPercent: Int,
    val outputSink: String,
    val sourcePath: String,
    val fileSizeBytes: Long
) {
    val formattedSampleRate: String
        get() = when {
            sampleRateHz >= 1000 -> "%.1f kHz".format(sampleRateHz / 1000f)
            sampleRateHz > 0 -> "$sampleRateHz Hz"
            else -> "44.1 kHz (Estándar)"
        }

    val formattedBitrate: String
        get() = if (bitrateKbps > 0) "$bitrateKbps kbps" else "320 kbps CBR"

    val formattedBitDepth: String
        get() = if (bitDepthBits > 0) "$bitDepthBits-bit Hi-Res" else "16-bit PCM"

    val formattedChannels: String
        get() = when (channelCount) {
            1 -> "Mono 1.0"
            2 -> "Estéreo 2.0 (Direct)"
            6 -> "Surround 5.1"
            8 -> "Surround 7.1"
            else -> "Estéreo 2.0"
        }

    val formattedFileSize: String
        get() = when {
            fileSizeBytes >= 1024 * 1024 -> "%.2f MB".format(fileSizeBytes / (1024f * 1024f))
            fileSizeBytes >= 1024 -> "%.1f KB".format(fileSizeBytes / 1024f)
            fileSizeBytes > 0 -> "$fileSizeBytes B"
            else -> "~8.5 MB"
        }
}
