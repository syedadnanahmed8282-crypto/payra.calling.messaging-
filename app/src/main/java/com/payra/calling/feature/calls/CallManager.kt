package com.payra.calling.feature.calls

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.payra.calling.data.FakeChatStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class CallPhase { Calling, Ringing, IncomingRinging, Connected, Busy, NoAnswer, Declined, Ended }

/**
 * Keeps ONE call alive for the whole app (so it keeps running when you minimize it).
 * Fake call for now: nothing is sent over the internet yet. Demo outcomes when you call:
 * Rahim Bhai = busy, Nusrat = no answer, everybody else picks up after a few rings.
 */
object CallManager {
    var active by mutableStateOf(false)
        private set
    var contactId by mutableStateOf("")
        private set
    var incoming by mutableStateOf(false)
        private set
    var video by mutableStateOf(false)
        private set
    var phase by mutableStateOf(CallPhase.Calling)
        private set
    var seconds by mutableIntStateOf(0)
        private set

    /** Demo only: pretends the line gets weak for a few seconds (the real signal comes from Agora later). */
    var poorConnection by mutableStateOf(false)
        private set

    var muted by mutableStateOf(false)
    var speaker by mutableStateOf(false)
    var camOff by mutableStateOf(false)
    var frontCamera by mutableStateOf(true)

    /** True while the app is shown in Android's picture-in-picture window (after pressing Home during a call). */
    var inPip by mutableStateOf(false)

    /** Set when the app is leaving the screen during a call, so the call screen opens before the small window appears. */
    var pipRequested by mutableStateOf(false)

    /** Video call: true = you are the big picture and the other person is small. */
    var swapped by mutableStateOf(false)
    var minimized by mutableStateOf(false)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var scriptJob: Job? = null
    private var tickJob: Job? = null
    private var closeJob: Job? = null

    val isConnected: Boolean get() = phase == CallPhase.Connected
    val isFinished: Boolean
        get() = phase == CallPhase.Busy || phase == CallPhase.NoAnswer || phase == CallPhase.Declined || phase == CallPhase.Ended

    /**
     * Starts (or restores) a call. Returns false when the call was refused: a blocked contact, or another
     * call with someone else is still running. A call that already finished (result screen) is closed first.
     */
    fun start(context: Context, contactId: String, incoming: Boolean, video: Boolean): Boolean {
        if (FakeChatStore.isBlocked(contactId)) return false
        if (active) {
            if (isFinished) {
                close()
            } else if (this.contactId == contactId) {
                minimized = false
                return true
            } else {
                return false
            }
        }
        closeJob?.cancel()
        scriptJob?.cancel()
        tickJob?.cancel()
        this.contactId = contactId
        this.incoming = incoming
        this.video = video
        seconds = 0
        muted = false
        speaker = false
        camOff = false
        frontCamera = true
        swapped = false
        minimized = false
        phase = if (incoming) CallPhase.IncomingRinging else CallPhase.Calling
        active = true

        if (incoming) CallSounds.playIncoming(context.applicationContext) else CallSounds.playOutgoing()

        scriptJob = scope.launch {
            if (incoming) {
                delay(30000)
                if (phase == CallPhase.IncomingRinging) finish(connected = false, label = CallPhase.NoAnswer)
            } else {
                delay(1300)
                if (phase == CallPhase.Calling) phase = CallPhase.Ringing
                when (contactId) {
                    "rahim" -> {
                        delay(3500)
                        CallSounds.playBusy()
                        phase = CallPhase.Busy
                        finish(connected = false, label = CallPhase.Busy)
                    }
                    "nusrat" -> {
                        delay(9000)
                        phase = CallPhase.NoAnswer
                        finish(connected = false, label = CallPhase.NoAnswer)
                    }
                    else -> {
                        delay(3800)
                        connect()
                    }
                }
            }
        }
        return true
    }

    fun answer() {
        if (phase == CallPhase.IncomingRinging) connect()
    }

    fun decline() {
        if (phase == CallPhase.IncomingRinging) finish(connected = false, label = CallPhase.Declined)
    }

    /** The red button: ends a connected call, or cancels one that is still ringing. */
    fun hangUp() {
        when (phase) {
            CallPhase.Connected -> finish(connected = true, label = CallPhase.Ended)
            CallPhase.Calling, CallPhase.Ringing -> finish(connected = false, label = CallPhase.Ended)
            CallPhase.IncomingRinging -> decline()
            else -> close()
        }
    }

    /** Close the finished-call screen right away. */
    fun close() {
        SelfCamera.release()
        closeJob?.cancel()
        active = false
        minimized = false
    }

    fun callBack(context: Context) {
        val id = contactId
        val v = video
        close()
        start(context, id, incoming = false, video = v)
    }

    private fun connect() {
        scriptJob?.cancel()
        CallSounds.stop()
        phase = CallPhase.Connected
        seconds = 0
        tickJob?.cancel()
        tickJob = scope.launch {
            while (true) {
                delay(1000)
                seconds++
                poorConnection = seconds in 15..20
            }
        }
    }

    private fun finish(connected: Boolean, label: CallPhase) {
        poorConnection = false
        SelfCamera.release()
        scriptJob?.cancel()
        tickJob?.cancel()
        if (connected) {
            CallSounds.stop()
            CallSounds.playEnd()
        } else if (label != CallPhase.Busy) {
            CallSounds.stop()
        }
        FakeChatStore.addCallLog(
            contactId = contactId,
            outgoing = !incoming,
            video = video,
            missed = !connected,
            seconds = if (connected) seconds else 0,
        )
        phase = label
        // Stay on the result screen for a moment so the person can read it, then close.
        closeJob?.cancel()
        closeJob = scope.launch {
            delay(if (connected) 3500 else 4500)
            active = false
            minimized = false
        }
    }
}
