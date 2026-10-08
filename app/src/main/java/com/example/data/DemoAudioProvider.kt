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

                // Generación de acordes synthwave armónicos (La menor: A, C, E con bajo pulsante)
                val buffer = ByteBuffer.allocate(4096).order(ByteOrder.LITTLE_ENDIAN)
                val baseFreqs = doubleArrayOf(220.0, 261.63, 329.63, 440.0) // A3, C4, E4, A4
                val bassFreq = 110.0 // A2 bajo

                var sampleIndex = 0
                while (sampleIndex < totalSamples) {
                    buffer.clear()
                    val chunkSize = minOf(buffer.capacity() / (numChannels * 2), totalSamples - sampleIndex)

                    for (i in 0 until chunkSize) {
                        val t = (sampleIndex + i).toDouble() / SAMPLE_RATE

                        // Pulso rítmico suave estilo Synthwave / Lo-Fi 503
                        val rhythmEnvelope = 0.6 + 0.4 * sin(2.0 * Math.PI * 2.0 * t) // 120 BPM
                        val bass = sin(2.0 * Math.PI * bassFreq * t) * 0.4

                        // Acordes ambientales armónicos
                        var chord = 0.0
                        for (freq in baseFreqs) {
                            chord += sin(2.0 * Math.PI * freq * t) * 0.15
                        }

                        // Canal Izquierdo y Derecho con ligera modulación estéreo
                        val sampleLeft = ((bass + chord * rhythmEnvelope) * 16000.0).toInt().coerceIn(-32767, 32767).toShort()
                        val sampleRight = ((bass + chord * (0.6 + 0.4 * sin(2.0 * Math.PI * 2.0 * t + 0.5))) * 16000.0).toInt().coerceIn(-32767, 32767).toShort()

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
