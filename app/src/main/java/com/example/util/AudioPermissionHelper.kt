package com.example.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat

object AudioPermissionHelper {

    /**
     * Retorna el permiso requerido según la versión de Android.
     * Android 13+ (API 33+) utiliza READ_MEDIA_AUDIO.
     * Android 12 y anteriores utilizan READ_EXTERNAL_STORAGE.
     */
    val requiredPermission: String
        get() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

    /**
     * Verifica si el permiso de lectura de audio ha sido concedido por el usuario.
     */
    fun hasPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            requiredPermission
        ) == PackageManager.PERMISSION_GRANTED
    }
}
