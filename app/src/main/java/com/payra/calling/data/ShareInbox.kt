package com.payra.calling.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.File

data class SharedFile(val uri: String, val name: String, val info: String)

/** Something another app shared with Payra: text, pictures / videos, or documents. */
data class SharedPayload(val text: String, val items: List<MediaItem>, val files: List<SharedFile>)

/**
 * Catches "Share > Payra" from other apps. Shared files are copied into the app's own storage straight away
 * (the sending app's permission to read them is only temporary).
 */
object ShareInbox {
    var pending by mutableStateOf<SharedPayload?>(null)

    @Suppress("DEPRECATION")
    private fun streamOf(intent: Intent): Uri? =
        if (Build.VERSION.SDK_INT >= 33) intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        else intent.getParcelableExtra(Intent.EXTRA_STREAM)

    @Suppress("DEPRECATION")
    private fun streamsOf(intent: Intent): List<Uri> =
        (if (Build.VERSION.SDK_INT >= 33) intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)
        else intent.getParcelableArrayListExtra<Uri>(Intent.EXTRA_STREAM)) ?: emptyList()

    fun handle(context: Context, intent: Intent?) {
        if (intent == null) return
        val action = intent.action
        if (action != Intent.ACTION_SEND && action != Intent.ACTION_SEND_MULTIPLE) return
        val app = context.applicationContext
        val text = intent.getStringExtra(Intent.EXTRA_TEXT).orEmpty()
        val declaredType = intent.type.orEmpty()
        val uris = if (action == Intent.ACTION_SEND) listOfNotNull(streamOf(intent)) else streamsOf(intent)
        intent.action = null // handled, do not handle it again

        if (uris.isEmpty()) {
            if (text.isNotBlank()) pending = SharedPayload(text, emptyList(), emptyList())
            return
        }
        Thread {
            val items = mutableListOf<MediaItem>()
            val files = mutableListOf<SharedFile>()
            val dir = File(app.filesDir, "shared").apply { mkdirs() }
            uris.take(10).forEachIndexed { index, uri ->
                try {
                    val (name, info) = DeviceData.fileInfo(app, uri)
                    val mime = try { app.contentResolver.getType(uri) } catch (e: Exception) { null } ?: declaredType
                    val target = File(dir, "${System.currentTimeMillis()}_${index}_${name.replace('/', '_')}")
                    app.contentResolver.openInputStream(uri)?.use { input -> target.outputStream().use { input.copyTo(it) } }
                    if (target.exists() && target.length() > 0L) {
                        val local = Uri.fromFile(target)
                        when {
                            mime.startsWith("image/") -> items.add(MediaItem(0, false, "", local.toString()))
                            mime.startsWith("video/") -> items.add(MediaItem(0, true, DeviceData.videoDurationLabel(app, local), local.toString()))
                            else -> files.add(SharedFile(local.toString(), name, info))
                        }
                    }
                } catch (e: Exception) {
                    Log.w("PayraShare", "could not take $uri", e)
                }
            }
            if (text.isNotBlank() || items.isNotEmpty() || files.isNotEmpty()) pending = SharedPayload(text, items, files)
        }.start()
    }
}
