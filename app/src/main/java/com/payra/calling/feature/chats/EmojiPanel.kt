package com.payra.calling.feature.chats

import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payra.calling.designsystem.GlassCircleButton
import com.payra.calling.designsystem.PayraColors
import com.payra.calling.designsystem.bouncyClick

/**
 * Hundreds of emojis in categories. The emoji ranges are listed by code point and every emoji the phone
 * cannot draw is dropped (checked with the phone's own font), so older phones never show empty boxes.
 */
object EmojiData {
    class Category(val tab: String, val name: String, val emojis: List<String>)

    private fun cp(code: Int): String = String(Character.toChars(code))

    private fun cps(vararg ranges: IntRange): List<String> = ranges.flatMap { range -> range.map { cp(it) } }

    private fun flag(country: String): String =
        country.map { cp(0x1F1E6 + (it - 'A')) }.joinToString("")

    private val paint by lazy { android.graphics.Paint() }

    private fun supported(list: List<String>): List<String> = list.filter { paint.hasGlyph(it) }.distinct()

    val categories: List<Category> by lazy {
        listOf(
            Category(
                cp(0x1F600), "Smileys",
                supported(cps(0x1F600..0x1F64F, 0x1F910..0x1F92F, 0x1F970..0x1F97A, 0x1F9D0..0x1F9D0)),
            ),
            Category(
                cp(0x1F44D), "People",
                supported(cps(0x1F440..0x1F450, 0x1F466..0x1F483, 0x1F590..0x1F596, 0x1F918..0x1F91F, 0x1F9D1..0x1F9DD, 0x1F4AA..0x1F4AA)),
            ),
            Category(
                cp(0x1F436), "Animals",
                supported(cps(0x1F400..0x1F43F, 0x1F980..0x1F9AE, 0x1F331..0x1F344, 0x1F490..0x1F490)),
            ),
            Category(
                cp(0x1F354), "Food",
                supported(cps(0x1F345..0x1F37F, 0x1F950..0x1F96F, 0x1F9C0..0x1F9CB)),
            ),
            Category(
                cp(0x26BD), "Activities",
                supported(cps(0x26BD..0x26BE, 0x1F3A0..0x1F3CA, 0x1F3CF..0x1F3D3, 0x1F93A..0x1F94F)),
            ),
            Category(
                cp(0x1F697), "Travel",
                supported(cps(0x1F680..0x1F6C5, 0x1F30D..0x1F320, 0x1F3D4..0x1F3DF, 0x1F3E0..0x1F3F0, 0x1F6E0..0x1F6EC)),
            ),
            Category(
                cp(0x1F4A1), "Objects",
                supported(cps(0x231A..0x231B, 0x1F4A1..0x1F4FC, 0x1F50A..0x1F52E, 0x1F5A5..0x1F5A5)),
            ),
            Category(
                "\u2764\uFE0F", "Symbols",
                supported(
                    listOf("\u2764\uFE0F", "\u2763\uFE0F", "\u2705", "\u274C", "\u2B50", "\u2728", "\u2757", "\u2753") +
                        cps(0x1F493..0x1F49F, 0x1F4AF..0x1F4AF, 0x1F525..0x1F525, 0x1F7E0..0x1F7EB, 0x1F500..0x1F50F),
                ),
            ),
            Category(
                flag("BD"), "Flags",
                supported(
                    listOf(
                        "BD", "IN", "PK", "NP", "LK", "US", "GB", "CA", "AU", "DE", "FR", "IT", "ES", "RU", "TR",
                        "SA", "AE", "QA", "KW", "EG", "ZA", "NG", "CN", "JP", "KR", "ID", "MY", "SG", "BR", "AR",
                    ).map { flag(it) },
                ),
            ),
        ).filter { it.emojis.isNotEmpty() }
    }
}

/** Emoji keyboard: category tabs on top and a scrolling grid. [onBackspace] shows a delete button. */
@Composable
fun EmojiPanel(
    onPick: (String) -> Unit,
    modifier: Modifier = Modifier,
    onBackspace: (() -> Unit)? = null,
    height: Dp = 300.dp,
) {
    val categories = EmojiData.categories
    var selected by remember { mutableIntStateOf(0) }

    Column(modifier = modifier.fillMaxWidth().height(height)) {
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()).padding(horizontal = 12.dp, vertical = 8.dp),
        ) {
            categories.forEachIndexed { index, category ->
                val chipColor by animateColorAsState(
                    targetValue = if (index == selected) PayraColors.Primary.copy(alpha = 0.16f) else Color.Transparent,
                    animationSpec = tween(180),
                    label = "emojiTab",
                )
                Box(
                    modifier = Modifier
                        .padding(end = 6.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(chipColor)
                        .bouncyClick(onClick = { selected = index }, pressedScale = 0.88f)
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(category.tab, fontSize = 22.sp)
                }
            }
        }
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Crossfade(targetState = selected, animationSpec = tween(200), label = "emojiGrid") { index ->
                val category = categories[index.coerceIn(0, categories.lastIndex)]
                LazyVerticalGrid(
                    columns = GridCells.Adaptive(46.dp),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    items(category.emojis) { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 28.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Normal,
                            modifier = Modifier
                                .fillMaxWidth()
                                .bouncyClick(onClick = { onPick(emoji) }, pressedScale = 0.7f)
                                .padding(vertical = 6.dp),
                        )
                    }
                }
            }
            if (onBackspace != null) {
                GlassCircleButton(
                    icon = Icons.AutoMirrored.Filled.Backspace,
                    contentDescription = "Delete",
                    onClick = onBackspace,
                    size = 44.dp,
                    modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp),
                )
            }
        }
    }
}
