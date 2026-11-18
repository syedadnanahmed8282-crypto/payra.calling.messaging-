package com.payra.calling.feature.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.graphics.Paint
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.RotateRight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payra.calling.designsystem.BouncingDots
import com.payra.calling.designsystem.GlassCircleButton
import com.payra.calling.designsystem.TextLink
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.max
import kotlin.math.min

private const val TAG = "PayraCrop"

private fun exifRotation(context: Context, uri: Uri): Int {
    if (Build.VERSION.SDK_INT < 24) return 0
    return try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            when (ExifInterface(input).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        } ?: 0
    } catch (e: Exception) {
        Log.w(TAG, "no exif for $uri", e)
        0
    }
}

/** Reads the picture, shrinks very large ones (max 2048 px) and applies the camera's rotation (EXIF). */
private fun loadBitmap(context: Context, uri: Uri, maxSide: Int = 2048): Bitmap? {
    return try {
        val resolver = context.contentResolver
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return null
        var sample = 1
        while (bounds.outWidth / sample > maxSide || bounds.outHeight / sample > maxSide) sample *= 2
        val options = BitmapFactory.Options().apply { inSampleSize = sample }
        val raw = resolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, options) } ?: return null
        val rotation = exifRotation(context, uri)
        if (rotation == 0) {
            raw
        } else {
            Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, Matrix().apply { postRotate(rotation.toFloat()) }, true)
        }
    } catch (e: Exception) {
        Log.e(TAG, "could not load $uri", e)
        null
    } catch (e: OutOfMemoryError) {
        Log.e(TAG, "picture too big: $uri", e)
        null
    }
}

/** The matrix that places the picture inside a frame of [fw] x [fh] pixels. */
private fun placement(bw: Int, bh: Int, turns: Int, scale: Float, offX: Float, offY: Float, fw: Float, fh: Float): Matrix =
    Matrix().apply {
        postTranslate(-bw / 2f, -bh / 2f)
        postRotate(turns * 90f)
        postScale(scale, scale)
        postTranslate(fw / 2f + offX, fh / 2f + offY)
    }

private fun fitScale(bw: Int, bh: Int, turns: Int, fw: Float, fh: Float): Float {
    val w = if (turns % 2 == 0) bw.toFloat() else bh.toFloat()
    val h = if (turns % 2 == 0) bh.toFloat() else bw.toFloat()
    return max(fw / w, fh / h)
}

/**
 * Crop a profile photo (square) or a cover photo (16:9): pinch to zoom, drag to move, double-tap to reset,
 * rotate by 90 degrees. The result is saved in the app's own files (not in the cache) and returned by [onDone].
 */
@Composable
fun CropScreen(kind: String, sourceUri: String, onCancel: () -> Unit, onDone: (String) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current
    val isCover = kind == "cover"
    val aspect = if (isCover) 16f / 9f else 1f

    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var loading by remember { mutableStateOf(true) }
    var saving by remember { mutableStateOf(false) }
    var userScale by remember { mutableFloatStateOf(1f) }
    var offX by remember { mutableFloatStateOf(0f) }
    var offY by remember { mutableFloatStateOf(0f) }
    var turns by remember { mutableIntStateOf(0) }
    var frameWpx by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(sourceUri) {
        loading = true
        bitmap = withContext(Dispatchers.IO) { loadBitmap(context, Uri.parse(sourceUri)) }
        loading = false
        if (bitmap == null) {
            Toast.makeText(context, "Could not open this picture", Toast.LENGTH_SHORT).show()
            onCancel()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A1418)).systemBarsPadding()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextLink("Cancel", onClick = onCancel, color = Color.White)
                Spacer(Modifier.weight(1f))
                Text(if (isCover) "Adjust cover photo" else "Adjust profile photo", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.weight(1f))
                TextLink(
                    text = if (saving) "Saving..." else "Done",
                    color = Color(0xFF7CD3E6),
                    onClick = {
                        val bmp = bitmap
                        if (bmp != null && !saving) {
                            saving = true
                            // the frame size in px is measured below; rebuilt here from the same rule
                            scope.launch {
                                val saved = withContext(Dispatchers.IO) {
                                    try {
                                        val outW = if (isCover) 1280 else 1024
                                        val outH = (outW / aspect).toInt()
                                        val out = Bitmap.createBitmap(outW, outH, Bitmap.Config.ARGB_8888)
                                        val canvas = android.graphics.Canvas(out)
                                        val eff = fitScale(bmp.width, bmp.height, turns, outW.toFloat(), outH.toFloat()) * userScale
                                        // offsets were measured in frame pixels: scale them to the output size
                                        val k = frameWpx.coerceAtLeast(1f)
                                        val m = placement(bmp.width, bmp.height, turns, eff, offX * outW / k, offY * outW / k, outW.toFloat(), outH.toFloat())
                                        canvas.drawBitmap(bmp, m, Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG))
                                        val dir = File(context.filesDir, "profile").apply { mkdirs() }
                                        val file = File(dir, "${kind}_${System.currentTimeMillis()}.jpg")
                                        file.outputStream().use { out.compress(Bitmap.CompressFormat.JPEG, 92, it) }
                                        if (file.exists() && file.length() > 0L) Uri.fromFile(file).toString() else null
                                    } catch (e: Exception) {
                                        Log.e(TAG, "save failed", e)
                                        null
                                    } catch (e: OutOfMemoryError) {
                                        Log.e(TAG, "save ran out of memory", e)
                                        null
                                    }
                                }
                                saving = false
                                if (saved != null) {
                                    onDone(saved)
                                } else {
                                    Toast.makeText(context, "Could not save the picture", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    },
                )
            }

            BoxWithConstraints(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                val frameW = if (isCover) maxWidth - 32.dp else min(maxWidth.value - 64f, 340f).dp
                val frameH = frameW / aspect
                val fwPx = with(density) { frameW.toPx() }
                val fhPx = with(density) { frameH.toPx() }
                LaunchedEffect(fwPx) { frameWpx = fwPx }

                val bmp = bitmap
                if (loading || bmp == null) {
                    BouncingDots(color = Color.White, dotSize = 8.dp)
                } else {
                    fun clampOffsets() {
                        val eff = fitScale(bmp.width, bmp.height, turns, fwPx, fhPx) * userScale
                        val w = (if (turns % 2 == 0) bmp.width else bmp.height) * eff
                        val h = (if (turns % 2 == 0) bmp.height else bmp.width) * eff
                        val maxX = max(0f, (w - fwPx) / 2f)
                        val maxY = max(0f, (h - fhPx) / 2f)
                        offX = offX.coerceIn(-maxX, maxX)
                        offY = offY.coerceIn(-maxY, maxY)
                    }

                    Canvas(
                        modifier = Modifier
                            .size(frameW, frameH)
                            .clip(RoundedCornerShape(if (isCover) 16.dp else 28.dp))
                            .border(2.dp, Color.White.copy(alpha = 0.9f), RoundedCornerShape(if (isCover) 16.dp else 28.dp))
                            .pointerInput(bmp) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    userScale = (userScale * zoom).coerceIn(1f, 6f)
                                    offX += pan.x
                                    offY += pan.y
                                    clampOffsets()
                                }
                            }
                            .pointerInput(bmp) {
                                detectTapGestures(onDoubleTap = {
                                    userScale = 1f
                                    offX = 0f
                                    offY = 0f
                                })
                            },
                    ) {
                        val eff = fitScale(bmp.width, bmp.height, turns, size.width, size.height) * userScale
                        val m = placement(bmp.width, bmp.height, turns, eff, offX, offY, size.width, size.height)
                        drawIntoCanvas { it.nativeCanvas.drawBitmap(bmp, m, Paint(Paint.FILTER_BITMAP_FLAG or Paint.ANTI_ALIAS_FLAG)) }
                    }
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(bottom = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                GlassCircleButton(
                    icon = Icons.Outlined.RotateRight,
                    contentDescription = "Rotate",
                    onClick = {
                        turns = (turns + 1) % 4
                        userScale = 1f
                        offX = 0f
                        offY = 0f
                    },
                )
                Spacer(Modifier.height(10.dp))
                Text("Pinch to zoom, drag to move, double-tap to reset", color = Color.White.copy(alpha = 0.7f), fontSize = 13.sp)
            }
        }
    }
}

