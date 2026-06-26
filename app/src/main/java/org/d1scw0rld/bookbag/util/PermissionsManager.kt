package org.d1scw0rld.bookbag.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.Settings
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Returns true if the app has the necessary storage permissions for the current Android version.
     */
    fun hasStoragePermission(): Boolean {
        return if (isAndroidRorAbove()) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }
    }

    /**
     * Returns the permission string needed for legacy storage access.
     */
    fun getStoragePermissionRequest(): String = Manifest.permission.WRITE_EXTERNAL_STORAGE

    /**
     * Returns the Intent needed to request "Manage All Files" access on Android 11+.
     */
    fun getManageStorageIntent(): Intent {
        return try {
            Intent(
                Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                Uri.fromParts("package", context.packageName, null)
            )
        } catch (e: Exception) {
            Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
        }
    }
    
    /**
     * Utility to check if the current device is Android 11 (API 30) or above.
     */
    fun isAndroidRorAbove(): Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
}
