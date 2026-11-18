package com.payra.calling.data

import android.content.ContentUris
import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.ContactsContract
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.format.Formatter
import java.io.File

/** One real photo or video from the phone's gallery. */
data class GalleryItem(
    val uri: Uri,
    val isVideo: Boolean,
    val durationMs: Long,
    val dateAdded: Long,
) {
    val durationLabel: String
        get() {
            val total = (durationMs / 1000).toInt()
            return "${total / 60}:${(total % 60).toString().padStart(2, '0')}"
        }
}

data class DeviceContact(val name: String, val phone: String)

object DeviceData {

    /** Photos and videos on the phone, newest first (needs the gallery permission). */
    fun loadGallery(context: Context, limit: Int = 400): List<GalleryItem> {
        val result = mutableListOf<GalleryItem>()
        try {
            context.contentResolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DATE_ADDED),
                null, null, "${MediaStore.Images.Media.DATE_ADDED} DESC",
            )?.use { c ->
                val idCol = c.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
                val dateCol = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
                var count = 0
                while (c.moveToNext() && count < limit) {
                    val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, c.getLong(idCol))
                    result.add(GalleryItem(uri, false, 0L, c.getLong(dateCol)))
                    count++
                }
            }
        } catch (e: Exception) {
            // no permission or no storage: ignore, the list stays empty
        }
        try {
            context.contentResolver.query(
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                arrayOf(MediaStore.Video.Media._ID, MediaStore.Video.Media.DATE_ADDED, MediaStore.Video.Media.DURATION),
                null, null, "${MediaStore.Video.Media.DATE_ADDED} DESC",
            )?.use { c ->
                val idCol = c.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
                val dateCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED)
                val durCol = c.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
                var count = 0
                while (c.moveToNext() && count < limit) {
                    val uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, c.getLong(idCol))
                    result.add(GalleryItem(uri, true, c.getLong(durCol), c.getLong(dateCol)))
                    count++
                }
            }
        } catch (e: Exception) {
            // ignore
        }
        return result.sortedByDescending { it.dateAdded }
    }

    /** Names and phone numbers from the phone's contacts (needs the contacts permission). */
    fun loadContacts(context: Context): List<DeviceContact> {
        val result = mutableListOf<DeviceContact>()
        try {
            context.contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER,
                ),
                null, null, "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC",
            )?.use { c ->
                val nameCol = c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numCol = c.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                val seen = HashSet<String>()
                while (c.moveToNext()) {
                    val name = c.getString(nameCol) ?: continue
                    val number = c.getString(numCol) ?: continue
                    if (seen.add("$name|$number")) result.add(DeviceContact(name, number))
                }
            }
        } catch (e: Exception) {
            // no permission: ignore
        }
        return result
    }

    /** Name and "2.4 MB  PDF" style line for a file the person picked. */
    fun fileInfo(context: Context, uri: Uri): Pair<String, String> {
        var name = "File"
        var size = 0L
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { c ->
                if (c.moveToFirst()) {
                    val nameCol = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeCol = c.getColumnIndex(OpenableColumns.SIZE)
                    if (nameCol >= 0) name = c.getString(nameCol) ?: name
                    if (sizeCol >= 0) size = c.getLong(sizeCol)
                }
            }
        } catch (e: Exception) {
            // keep defaults
        }
        val ext = name.substringAfterLast('.', "").uppercase()
        val sizeText = if (size > 0) Formatter.formatShortFileSize(context, size) else ""
        return name to listOf(sizeText, ext).filter { it.isNotEmpty() }.joinToString("  ")
    }

    /** A file that was really saved: [uri] opens it, [label] is a short place name for the message. */
    data class SavedFile(val uri: String, val label: String)

    /**
     * Saves a copy of [source] (content:// address) as a download. Returns null unless the saved file really exists
     * and is not empty - so the caller can show a failure instead of a false "Saved".
     * Android 10+: public Downloads/Payra folder (MediaStore). Older phones: the app's own Downloads folder.
     */
    fun saveToDownloads(context: Context, source: Uri, fileName: String, mime: String? = null): SavedFile? {
        val name = fileName.ifBlank { "payra_${System.currentTimeMillis()}" }
        return try {
            if (Build.VERSION.SDK_INT >= 29) {
                val values = android.content.ContentValues().apply {
                    put(MediaStore.Downloads.DISPLAY_NAME, name)
                    if (!mime.isNullOrBlank()) put(MediaStore.Downloads.MIME_TYPE, mime)
                    put(MediaStore.Downloads.RELATIVE_PATH, android.os.Environment.DIRECTORY_DOWNLOADS + "/Payra")
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
                val resolver = context.contentResolver
                val target = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values) ?: return null
                var written = 0L
                try {
                    resolver.openInputStream(source)?.use { input ->
                        resolver.openOutputStream(target)?.use { output -> written = input.copyTo(output) }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("PayraSave", "copy failed", e)
                    written = 0L
                }
                if (written <= 0L) {
                    resolver.delete(target, null, null)
                    return null
                }
                val done = android.content.ContentValues().apply { put(MediaStore.Downloads.IS_PENDING, 0) }
                resolver.update(target, done, null, null)
                SavedFile(target.toString(), "Downloads/Payra")
            } else {
                val dir = context.getExternalFilesDir(android.os.Environment.DIRECTORY_DOWNLOADS) ?: context.filesDir
                dir.mkdirs()
                val target = File(dir, name)
                context.contentResolver.openInputStream(source)?.use { input ->
                    target.outputStream().use { output -> input.copyTo(output) }
                }
                if (target.exists() && target.length() > 0L) {
                    SavedFile(Uri.fromFile(target).toString(), "the app's Downloads folder")
                } else {
                    target.delete()
                    null
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("PayraSave", "save failed for $source", e)
            null
        }
    }

    /** Length of a video as "0:24" (empty if it cannot be read). */
    fun videoDurationLabel(context: Context, uri: Uri): String {
        return try {
            val retriever = android.media.MediaMetadataRetriever()
            retriever.setDataSource(context, uri)
            val ms = retriever.extractMetadata(android.media.MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            retriever.release()
            val total = (ms / 1000).toInt()
            "${total / 60}:${(total % 60).toString().padStart(2, '0')}"
        } catch (e: Exception) {
            ""
        }
    }

    fun cacheSizeBytes(context: Context): Long {
        fun size(f: File): Long = if (f.isFile) f.length() else (f.listFiles()?.sumOf { size(it) } ?: 0L)
        return size(context.cacheDir) + (context.externalCacheDir?.let { size(it) } ?: 0L)
    }

    fun clearCache(context: Context) {
        fun wipe(f: File) {
            if (f.isDirectory) f.listFiles()?.forEach { wipe(it) }
            if (f != context.cacheDir && f != context.externalCacheDir) f.delete()
        }
        wipe(context.cacheDir)
        context.externalCacheDir?.let { wipe(it) }
    }

    /** True when the app may read photos and videos. */
    fun hasGalleryPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= 33) {
            context.checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            context.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }

    fun galleryPermissions(): Array<String> =
        if (Build.VERSION.SDK_INT >= 33) {
            arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES, android.Manifest.permission.READ_MEDIA_VIDEO)
        } else {
            arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
}
