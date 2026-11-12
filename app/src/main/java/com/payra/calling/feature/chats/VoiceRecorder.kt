package com.payra.calling.feature.chats

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.os.SystemClock
import java.io.File

/**
 * Records a voice message with the phone microphone into a small .m4a file (AAC).
 * start() -> (pause() / resume()) -> stop() returns (file path, seconds); cancel() throws the recording away.
 * Pause needs Android 7 (API 24) or newer.
 */
class VoiceRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var file: File? = null
    private var accumulatedMs = 0L
    private var segmentStart = 0L
    var isPaused: Boolean = false
        private set

    val canPause: Boolean get() = Build.VERSION.SDK_INT >= 24

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
            accumulatedMs = 0L
            segmentStart = SystemClock.elapsedRealtime()
            isPaused = false
            true
        } catch (e: Exception) {
            recorder?.release()
            recorder = null
            file = null
            false
        }
    }

    fun elapsedSeconds(): Int {
        val running = if (isPaused) 0L else SystemClock.elapsedRealtime() - segmentStart
        return ((accumulatedMs + running) / 1000L).toInt()
    }

    fun pause(): Boolean {
        val r = recorder ?: return false
        if (Build.VERSION.SDK_INT < 24 || isPaused) return false
        return try {
            r.pause()
            accumulatedMs += SystemClock.elapsedRealtime() - segmentStart
            isPaused = true
            true
        } catch (e: Exception) {
            false
        }
    }

    fun resume(): Boolean {
        val r = recorder ?: return false
        if (Build.VERSION.SDK_INT < 24 || !isPaused) return false
        return try {
            r.resume()
            segmentStart = SystemClock.elapsedRealtime()
            isPaused = false
            true
        } catch (e: Exception) {
            false
        }
    }

    /** Stops and returns the saved file path and its length in seconds, or null if it failed. */
    fun stop(): Pair<String, Int>? {
        val r = recorder ?: return null
        val target = file
        val seconds = elapsedSeconds().coerceAtLeast(1)
        recorder = null
        file = null
        isPaused = false
        return try {
            r.stop()
            r.release()
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
        isPaused = false
        if (r != null) {
            try { r.stop() } catch (_: Exception) { }
            try { r.release() } catch (_: Exception) { }
        }
        target?.delete()
    }
}
