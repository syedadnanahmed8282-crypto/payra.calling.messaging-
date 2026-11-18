package com.payra.calling.designsystem

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File

private const val TAG = "PayraLinks"

/**
 * Opens a web link. On Android 11+ it first tries to open an installed app for the link (YouTube, Facebook...)
 * and only then falls back to the browser.
 */
fun openLink(context: Context, url: String) {
    val target = if (url.startsWith("http", ignoreCase = true)) url else "https://$url"
    val uri = try {
        Uri.parse(target)
    } catch (e: Exception) {
        Log.w(TAG, "bad link: $url", e)
        Toast.makeText(context, "This link cannot be opened", Toast.LENGTH_SHORT).show()
        return
    }
    if (Build.VERSION.SDK_INT >= 30) {
        try {
            val appOnly = Intent(Intent.ACTION_VIEW, uri)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REQUIRE_NON_BROWSER)
            context.startActivity(appOnly)
            return
        } catch (e: ActivityNotFoundException) {
            // no app for this link: open it in the browser below
        } catch (e: Exception) {
            Log.w(TAG, "app link failed: $target", e)
        }
    }
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: ActivityNotFoundException) {
        Log.w(TAG, "no browser for: $target", e)
        Toast.makeText(context, "No browser found", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Log.e(TAG, "open link failed: $target", e)
        Toast.makeText(context, "This link cannot be opened", Toast.LENGTH_SHORT).show()
    }
}

private fun mimeFor(context: Context, uri: Uri): String {
    try {
        val fromProvider = context.contentResolver.getType(uri)
        if (!fromProvider.isNullOrBlank() && fromProvider != "application/octet-stream") return fromProvider
    } catch (e: Exception) {
        Log.w(TAG, "getType failed for $uri", e)
    }
    val ext = MimeTypeMap.getFileExtensionFromUrl(uri.toString()).lowercase()
    val fromExt = if (ext.isNotEmpty()) MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext) else null
    return fromExt ?: "*/*"
}

/**
 * Opens a document / picture / video with another app. [target] can be a content:// address, a file path or a
 * file:// address. Files are shared through FileProvider, and a clear message is shown when nothing can open them.
 */
fun openFile(context: Context, target: String) {
    try {
        val uri: Uri = when {
            target.startsWith("content://") -> Uri.parse(target)
            target.startsWith("file://") -> FileProvider.getUriForFile(context, context.packageName + ".fileprovider", File(Uri.parse(target).path.orEmpty()))
            else -> FileProvider.getUriForFile(context, context.packageName + ".fileprovider", File(target))
        }
        val intent = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, mimeFor(context, uri))
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(Intent.createChooser(intent, "Open with").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    } catch (e: ActivityNotFoundException) {
        Log.w(TAG, "no app to open $target", e)
        Toast.makeText(context, "No app can open this file", Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        Log.e(TAG, "open file failed: $target", e)
        Toast.makeText(context, "Could not open the file", Toast.LENGTH_SHORT).show()
    }
}
