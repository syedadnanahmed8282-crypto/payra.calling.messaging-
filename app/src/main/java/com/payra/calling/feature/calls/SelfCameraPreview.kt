package com.payra.calling.feature.calls

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

/** Your own live camera picture (front or back camera) using the phone's camera. Needs the camera permission. */
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
        var provider: ProcessCameraProvider? = null
        if (view != null && owner != null) {
            val future = ProcessCameraProvider.getInstance(context)
            future.addListener(
                {
                    try {
                        val p = future.get()
                        provider = p
                        val preview = Preview.Builder().build().also { it.setSurfaceProvider(view.surfaceProvider) }
                        p.unbindAll()
                        p.bindToLifecycle(
                            owner,
                            if (front) CameraSelector.DEFAULT_FRONT_CAMERA else CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                        )
                    } catch (e: Exception) {
                        // no such camera or permission missing: leave the preview empty
                    }
                },
                ContextCompat.getMainExecutor(context),
            )
        }
        onDispose {
            try {
                provider?.unbindAll()
            } catch (e: Exception) {
                // ignore
            }
        }
    }
}
