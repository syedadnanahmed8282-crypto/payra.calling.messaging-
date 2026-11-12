package com.payra.calling.designsystem

import android.content.Context
import android.graphics.drawable.BitmapDrawable
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** One shared image loader that can also show a still frame of a video. */
object PayraImageLoader {
    @Volatile
    private var instance: ImageLoader? = null

    fun get(context: Context): ImageLoader {
        val existing = instance
        if (existing != null) return existing
        return synchronized(this) {
            instance ?: ImageLoader.Builder(context.applicationContext)
                .components { add(VideoFrameDecoder.Factory()) }
                .crossfade(true)
                .build()
                .also { instance = it }
        }
    }
}

/** Shows a real photo (or the first frame of a video) from a content URI / file / web address. */
@Composable
fun PayraImage(
    model: Any?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop,
    contentDescription: String? = null,
) {
    val context = LocalContext.current
    val request = remember(model) { ImageRequest.Builder(context).data(model).crossfade(true).build() }
    AsyncImage(
        model = request,
        imageLoader = PayraImageLoader.get(context),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
    )
}

/**
 * The main color of a picture (used to tint a profile screen with the cover photo's color).
 * Returns [fallback] while loading or when there is no picture.
 */
@Composable
fun rememberDominantColor(uri: String, fallback: Color): Color {
    val context = LocalContext.current
    var color by remember(uri, fallback) { mutableStateOf(fallback) }
    LaunchedEffect(uri) {
        if (uri.isEmpty()) {
            color = fallback
            return@LaunchedEffect
        }
        try {
            val request = ImageRequest.Builder(context).data(uri).size(48).allowHardware(false).build()
            val result = PayraImageLoader.get(context).execute(request)
            val bitmap = (result.drawable as? BitmapDrawable)?.bitmap ?: return@LaunchedEffect
            val picked = withContext(Dispatchers.Default) {
                var r = 0L
                var g = 0L
                var b = 0L
                var n = 0L
                val w = bitmap.width
                val h = bitmap.height
                for (y in 0 until h step 2) {
                    for (x in 0 until w step 2) {
                        val px = bitmap.getPixel(x, y)
                        val pr = (px shr 16) and 0xFF
                        val pg = (px shr 8) and 0xFF
                        val pb = px and 0xFF
                        val max = maxOf(pr, pg, pb)
                        val min = minOf(pr, pg, pb)
                        // skip near white / near black / grey pixels so the color stays lively
                        if (max > 235 && min > 215) continue
                        if (max < 30) continue
                        if (max - min < 18) continue
                        r += pr
                        g += pg
                        b += pb
                        n++
                    }
                }
                if (n == 0L) {
                    null
                } else {
                    val hsv = FloatArray(3)
                    android.graphics.Color.RGBToHSV((r / n).toInt(), (g / n).toInt(), (b / n).toInt(), hsv)
                    hsv[1] = hsv[1].coerceAtLeast(0.45f)
                    hsv[2] = hsv[2].coerceIn(0.50f, 0.85f)
                    Color(android.graphics.Color.HSVToColor(hsv))
                }
            }
            if (picked != null) color = picked
        } catch (e: Exception) {
            color = fallback
        }
    }
    return color
}
