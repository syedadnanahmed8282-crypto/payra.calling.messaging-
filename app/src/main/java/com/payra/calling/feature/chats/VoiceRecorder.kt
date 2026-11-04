package com.payra.calling.feature.chats

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.SystemClock
import java.io.File

/**
 * Records a voice message with the phone microphone into a small .m4a file (AAC).
 * start() -> stop() returns (file path, seconds); cancel() throws the recording away.
 */
class VoiceRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var file: File? = null
    private var startedAt = 0L

    @Suppress("DEPRECATION")
    private fun newRecorder(): MediaRecorder =
        if (Build.VERSION.SDK_INT >= 31) MediaRecorder(context) else MediaRecorder()

    fun start(): Boolean {
        cancel()
        return try {
            val target = File(context.cacheDir, "voice_${System.currentTimeMillis()}.m4a")
            val r = newRecorder()
            r.setAudioSource(MediaRecorder.AudioSource.MIC)
            r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            r.setAudioEncodingBitRate(64000)
            r.setAudioSamplingRate(44100)
            r.setOutputFile(target.absolutePath)
            r.prepare()
            r.start()
            recorder = r
            file = target
            startedAt = SystemClock.elapsedRealtime()
            true
        } catch (e: Exception) {
            recorder?.release()
            recorder = null
            file = null
            false
        }
    }

    /** Stops and returns the saved file path and its length in seconds, or null if it failed / was too short. */
    fun stop(): Pair<String, Int>? {
        val r = recorder ?: return null
        val target = file
        recorder = null
        file = null
        return try {
            r.stop()
            r.release()
            val seconds = ((SystemClock.elapsedRealtime() - startedAt) / 1000L).toInt().coerceAtLeast(1)
            if (target != null) target.absolutePath to seconds else null
        } catch (e: Exception) {
            try { r.release() } catch (_: Exception) { }
            target?.delete()
            null
        }
    }

    fun cancel() {
        val r = recorder
        val target = file
        recorder = null
        file = null
        if (r != null) {
            try { r.stop() } catch (_: Exception) { }
            try { r.release() } catch (_: Exception) { }
        }
        target?.delete()
    }
}
