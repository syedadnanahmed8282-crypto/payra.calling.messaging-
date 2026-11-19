package com.payra.calling.feature.calls

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner

/**
 * The phone camera for a video call. There is only ONE camera session for the whole call: the call screen and the
 * floating window just take turns showing it. (Before, each screen started and stopped its own session and the
 * second one switched the first one off, which left the floating window black.)
 */
object SelfCamera {
    private const val TAG = "PayraCamera"
    private val handler = Handler(Looper.getMainLooper())
    private var provider: ProcessCameraProvider? = null
    private var preview: Preview? = null
    private var boundFront: Boolean? = null
    private var currentView: PreviewView? = null

    fun attach(context: Context, owner: LifecycleOwner, front: Boolean, view: PreviewView) {
        currentView = view
        val existing = preview
        if (existing != null && boundFront == front) {
            existing.setSurfaceProvider(view.surfaceProvider)
            return
        }
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener(
            {
                try {
                    val p = future.get()
                    provider = p
                    p.unbindAll()
                    val newPreview = Preview.Builder().build()
                    newPreview.setSurfaceProvider(currentView?.surfaceProvider)
                    p.bindToLifecycle(
                        owner,
                        if (front) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA,
                        newPreview,
                    )
                    preview = newPreview
                    boundFront = front
                } catch (e: Exception) {
                    Log.e(TAG, "could not start the camera", e)
                }
            },
            ContextCompat.getMainExecutor(context),
        )
    }

    /** A screen stopped showing the camera. If nobody picks it up within a moment, the camera is switched off. */
    fun detach(view: PreviewView) {
        if (currentView === view) {
            currentView = null
            preview?.setSurfaceProvider(null)
            handler.postDelayed({ if (currentView == null) release() }, 700)
        }
    }

    fun release() {
        try {
            provider?.unbindAll()
        } catch (e: Exception) {
            Log.w(TAG, "release failed", e)
        }
        preview = null
        boundFront = null
        currentView = null
    }
}

/** Your own live camera picture (front or back camera). Needs the camera permission. */
@Composable
fun SelfCameraPreview(front: Boolean, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var previewView by remember { mutableStateOf<PreviewView?>(null) }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                scaleType = PreviewView.ScaleType.FILL_CENTER
                previewView = this
            }
        },
        modifier = modifier,
    )

    DisposableEffect(front, previewView) {
        val view = previewView
        val owner = context as? LifecycleOwner
        if (view != null && owner != null) SelfCamera.attach(context, owner, front, view)
        onDispose {
            if (view != null) SelfCamera.detach(view)
        }
    }
}
