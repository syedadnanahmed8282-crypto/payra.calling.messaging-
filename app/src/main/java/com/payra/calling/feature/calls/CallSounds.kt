package com.payra.calling.feature.calls

import android.content.Context
import android.media.AudioManager
import android.media.Ringtone
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.payra.calling.data.UiPrefs

/** The real sounds of a call: ringing tone for incoming calls, ring-back tone when calling, busy tone, end beep. */
object CallSounds {
    private var ringtone: Ringtone? = null
    private var tone: ToneGenerator? = null
    private var vibrator: Vibrator? = null

    @Suppress("DEPRECATION")
    private fun vibratorOf(context: Context): Vibrator? =
        if (Build.VERSION.SDK_INT >= 31) {
            (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager)?.defaultVibrator
        } else {
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

    /** "Ring ring" while you wait for the other person to pick up. */
    fun playOutgoing() {
        stop()
        try {
            tone = ToneGenerator(AudioManager.STREAM_VOICE_CALL, 70).also { it.startTone(ToneGenerator.TONE_SUP_RINGTONE) }
        } catch (e: Exception) {
            tone = null
        }
    }

    /** The phone's ringtone + vibration for an incoming call. */
    @Suppress("DEPRECATION")
    fun playIncoming(context: Context) {
        stop()
        if (UiPrefs.ringtone) {
            try {
                val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
                val r = RingtoneManager.getRingtone(context, uri)
                if (Build.VERSION.SDK_INT >= 28) r.isLooping = true
                r.play()
                ringtone = r
            } catch (e: Exception) {
                ringtone = null
            }
        }
        if (UiPrefs.vibrate) {
            try {
                val v = vibratorOf(context)
                vibrator = v
                val pattern = longArrayOf(0, 700, 700)
                if (Build.VERSION.SDK_INT >= 26) {
                    v?.vibrate(VibrationEffect.createWaveform(pattern, 0))
                } else {
                    v?.vibrate(pattern, 0)
                }
            } catch (e: Exception) {
                vibrator = null
            }
        }
    }

    fun playBusy() {
        stop()
        try {
            tone = ToneGenerator(AudioManager.STREAM_VOICE_CALL, 70).also { it.startTone(ToneGenerator.TONE_SUP_BUSY, 2500) }
        } catch (e: Exception) {
            tone = null
        }
    }

    fun playEnd() {
        try {
            val t = ToneGenerator(AudioManager.STREAM_VOICE_CALL, 60)
            t.startTone(ToneGenerator.TONE_PROP_PROMPT, 250)
        } catch (e: Exception) {
            // ignore
        }
    }

    fun stop() {
        try { ringtone?.stop() } catch (e: Exception) { }
        ringtone = null
        try { tone?.stopTone() } catch (e: Exception) { }
        try { tone?.release() } catch (e: Exception) { }
        tone = null
        try { vibrator?.cancel() } catch (e: Exception) { }
        vibrator = null
    }
}
