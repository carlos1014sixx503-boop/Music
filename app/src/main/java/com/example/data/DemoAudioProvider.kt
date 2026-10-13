package com.example.data

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sin

/**
 * Generador y proveedor de audio de demostración local para Distrito Music 503.
 * Provee un archivo PCM WAV sintético de alta fidelidad guardado en el almacenamiento
 * interno de la app para permitir pruebas inmediatas de reproducción en segundo plano
 * (pantalla bloqueada, notificaciones y controles Bluetooth) sin requerir transferir
 * archivos MP3 manualmente al emulador.
 */
object DemoAudioProvider {

    private const val DEMO_FILE_NAME = "distrito_503_master_audio.wav"
    private const val SAMPLE_RATE = 44100
    private const val DURATION_SECONDS = 30 // 30 segundos de loop musical estéreo

    fun getOrCreateDemoAudioUri(context: Context): Uri {
        val file = File(context.filesDir, DEMO_FILE_NAME)
        if (!file.exists() || file.length() < 1000L) {
            generateSyntheticAudioFile(file)
        }
        return Uri.fromFile(file)
    }

    private fun generateSyntheticAudioFile(targetFile: File) {
        try {
            val totalSamples = SAMPLE_RATE * DURATION_SECONDS
            val numChannels = 2 // Estéreo
            val bitsPerSample = 16
            val byteRate = SAMPLE_RATE * numChannels * (bitsPerSample / 8)
            val dataSize = totalSamples * numChannels * (bitsPerSample / 8)

            FileOutputStream(targetFile).use { fos ->
                // Cabecera WAV (44 bytes)
                val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
                header.put("RIFF".toByteArray())
                header.putInt(36 + dataSize)
                header.put("WAVE".toByteArray())
                header.put("fmt ".toByteArray())
                header.putInt(16) // Subchunk1Size para PCM
                header.putShort(1) // AudioFormat PCM = 1
                header.putShort(numChannels.toShort())
                header.putInt(SAMPLE_RATE)
                header.putInt(byteRate)
                header.putShort((numChannels * (bitsPerSample / 8)).toShort()) // BlockAlign
                header.putShort(bitsPerSample.toShort())
                header.put("data".toByteArray())
                header.putInt(dataSize)

                fos.write(header.array())

                // Generación de progresión de acordes musicales suaves Lo-Fi / Synthwave (sin pulsos de radar)
                val buffer = ByteBuffer.allocate(4096).order(ByteOrder.LITTLE_ENDIAN)
                // Acordes: Dm7 (293, 349, 440, 523) -> Cmaj7 (261, 329, 392, 493) -> Am7 (220, 261, 329, 392)
                val chordD = doubleArrayOf(146.83, 293.66, 349.23, 440.0, 523.25)
                val chordC = doubleArrayOf(130.81, 261.63, 329.63, 392.0, 493.88)
                val chordA = doubleArrayOf(110.00, 220.00, 261.63, 329.63, 440.0)

                var sampleIndex = 0
                while (sampleIndex < totalSamples) {
                    buffer.clear()
                    val chunkSize = minOf(buffer.capacity() / (numChannels * 2), totalSamples - sampleIndex)

                    for (i in 0 until chunkSize) {
                        val t = (sampleIndex + i).toDouble() / SAMPLE_RATE
                        // Progresión cada 6 segundos
                        val chordPhase = (t % 18.0)
                        val activeChord = when {
                            chordPhase < 6.0 -> chordD
                            chordPhase < 12.0 -> chordC
                            else -> chordA
                        }

                        // Acordes suaves con desvanecimiento gradual armónico
                        var pad = 0.0
                        for (freq in activeChord) {
                            pad += sin(2.0 * Math.PI * freq * t) * 0.12
                            // Armónico superior sutil para calidez musical
                            pad += sin(2.0 * Math.PI * (freq * 2.0) * t) * 0.03
                        }

                        // Ondulación lenta estéreo tipo chorus/reverb de 0.2 Hz (no pulsos de radar)
                        val slowLfo = 0.85 + 0.15 * sin(2.0 * Math.PI * 0.15 * t)
                        val slowLfoR = 0.85 + 0.15 * sin(2.0 * Math.PI * 0.15 * t + 1.2)

                        val sampleLeft = ((pad * slowLfo) * 14000.0).toInt().coerceIn(-32767, 32767).toShort()
                        val sampleRight = ((pad * slowLfoR) * 14000.0).toInt().coerceIn(-32767, 32767).toShort()

                        buffer.putShort(sampleLeft)
                        buffer.putShort(sampleRight)
                    }

                    sampleIndex += chunkSize
                    fos.write(buffer.array(), 0, chunkSize * numChannels * 2)
                }
            }
        } catch (_: Exception) {}
    }
}
