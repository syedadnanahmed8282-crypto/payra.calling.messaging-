package com.payra.calling

import android.app.PictureInPictureParams
import android.content.Intent
import android.content.res.Configuration
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Rational
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.payra.calling.data.ShareInbox
import com.payra.calling.designsystem.PayraTheme
import com.payra.calling.feature.calls.CallManager
import com.payra.calling.navigation.PayraNavHost

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) ShareInbox.handle(this, intent)
        setContent {
            PayraTheme {
                PayraNavHost()
            }
        }
    }

    /** Another app shared something while Payra was already open. */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        ShareInbox.handle(this, intent)
    }

    /** Pressing Home during a call: keep the call on screen in a small floating window over other apps. */
    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (CallManager.active && !CallManager.isFinished && Build.VERSION.SDK_INT >= 26) {
            CallManager.pipRequested = true
            try {
                val params = PictureInPictureParams.Builder().setAspectRatio(Rational(9, 16)).build()
                enterPictureInPictureMode(params)
            } catch (e: Exception) {
                Log.w("PayraPip", "could not enter picture-in-picture", e)
            }
        }
    }

    @Suppress("OVERRIDE_DEPRECATION")
    override fun onPictureInPictureModeChanged(isInPictureInPictureMode: Boolean, newConfig: Configuration) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
        CallManager.inPip = isInPictureInPictureMode
        if (!isInPictureInPictureMode) CallManager.pipRequested = false
    }
}
