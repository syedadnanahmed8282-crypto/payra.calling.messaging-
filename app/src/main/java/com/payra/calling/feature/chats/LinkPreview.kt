package com.payra.calling.feature.chats

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payra.calling.designsystem.PayraColors
import com.payra.calling.designsystem.PayraImage
import com.payra.calling.designsystem.bouncyClick
import com.payra.calling.designsystem.openLink
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

data class LinkPreviewData(val url: String, val site: String, val title: String, val description: String, val imageUrl: String)

/** Reads the page title, description and picture of a web link (the tags websites provide for sharing). */
object LinkPreviewRepo {
    private val cache = HashMap<String, LinkPreviewData>()

    private fun decodeHtml(text: String): String = text
        .replace("&amp;", "&").replace("&quot;", "\"").replace("&#39;", "'").replace("&#x27;", "'")
        .replace("&lt;", "<").replace("&gt;", ">").replace("&nbsp;", " ").trim()

    private fun meta(html: String, name: String): String? {
        val a = Regex("<meta[^>]+(?:property|name)=[\"']$name[\"'][^>]*content=[\"']([^\"']*)[\"']", RegexOption.IGNORE_CASE).find(html)
        if (a != null) return decodeHtml(a.groupValues[1])
        val b = Regex("<meta[^>]+content=[\"']([^\"']*)[\"'][^>]*(?:property|name)=[\"']$name[\"']", RegexOption.IGNORE_CASE).find(html)
        return b?.let { decodeHtml(it.groupValues[1]) }
    }

    suspend fun load(url: String): LinkPreviewData = withContext(Dispatchers.IO) {
        val target = if (url.startsWith("http", ignoreCase = true)) url else "https://$url"
        cache[target]?.let { return@withContext it }
        val host = try { Uri.parse(target).host.orEmpty().removePrefix("www.") } catch (e: Exception) { "" }
        var result = LinkPreviewData(target, host, host, "", "")
        try {
            val connection = (URL(target).openConnection() as HttpURLConnection).apply {
                connectTimeout = 4000
                readTimeout = 4000
                instanceFollowRedirects = true
                setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 12) Payra/1.0")
                setRequestProperty("Accept-Language", "en")
            }
            val html = connection.inputStream.use { stream ->
                val buffer = ByteArray(120_000)
                var total = 0
                while (total < buffer.size) {
                    val n = stream.read(buffer, total, buffer.size - total)
                    if (n < 0) break
                    total += n
                }
                String(buffer, 0, total, Charsets.UTF_8)
            }
            val title = meta(html, "og:title")
                ?: Regex("<title[^>]*>([^<]*)</title>", RegexOption.IGNORE_CASE).find(html)?.groupValues?.get(1)?.let { decodeHtml(it) }
                ?: host
            val description = meta(html, "og:description") ?: meta(html, "description") ?: ""
            var image = meta(html, "og:image") ?: ""
            if (image.startsWith("//")) image = "https:$image"
            if (image.startsWith("/")) image = "https://${Uri.parse(target).host}$image"
            val site = meta(html, "og:site_name") ?: host
            result = LinkPreviewData(target, site, title.ifBlank { host }, description, if (image.startsWith("http")) image else "")
        } catch (e: Exception) {
            Log.w("PayraPreview", "no preview for $target", e)
        }
        cache[target] = result
        result
    }
}

/** The card under a message that contains a link: picture, site, title and a short description. */
@Composable
fun LinkPreviewCard(url: String, sent: Boolean) {
    val context = LocalContext.current
    val data by produceState<LinkPreviewData?>(initialValue = null, url) { value = LinkPreviewRepo.load(url) }
    val fg = if (sent) Color.White else PayraColors.Ink
    val sub = if (sent) Color.White.copy(alpha = 0.8f) else PayraColors.Muted
    val card = if (sent) Color.White.copy(alpha = 0.16f) else PayraColors.Primary.copy(alpha = 0.08f)
    val d = data
    val hostGuess = remember(url) { url.removePrefix("https://").removePrefix("http://").removePrefix("www.").substringBefore('/') }

    Column(
        modifier = Modifier
            .padding(top = 8.dp)
            .widthIn(max = 260.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(card)
            .bouncyClick(onClick = { openLink(context, url) }, pressedScale = 0.97f),
    ) {
        if (d != null && d.imageUrl.isNotEmpty()) {
            PayraImage(model = d.imageUrl, modifier = Modifier.fillMaxWidth().height(130.dp))
        }
        Column(modifier = Modifier.padding(10.dp)) {
            Text(d?.site ?: hostGuess, color = sub, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                text = d?.title ?: "Loading preview...",
                color = fg,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (d != null && d.description.isNotEmpty()) {
                Text(d.description, color = sub, fontSize = 12.sp, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
