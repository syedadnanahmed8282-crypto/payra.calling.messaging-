package com.payra.calling.designsystem

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/** Colors of one chat wallpaper: a base color and three soft glows. */
data class WallpaperPalette(
    val name: String,
    val base: Color,
    val glowA: Color,
    val glowB: Color,
    val glowC: Color,
)

object Wallpapers {
    val all = listOf(
        WallpaperPalette("Default", PayraColors.Background, PayraColors.Primary, PayraColors.AccentLight, PayraColors.Glow),
        WallpaperPalette("Ocean", Color(0xFFE3F3FA), Color(0xFF2B8FAE), Color(0xFF1B6F8A), Color(0xFF7CD3E6)),
        WallpaperPalette("Mint", Color(0xFFEAF8F0), Color(0xFF3FA796), Color(0xFFA8E6C4), Color(0xFF7CD3E6)),
        WallpaperPalette("Lavender", Color(0xFFF1EDFC), Color(0xFF7B6FD6), Color(0xFFC3B5F5), Color(0xFFA9C4F5)),
        WallpaperPalette("Peach", Color(0xFFFFF1E8), Color(0xFFF28B66), Color(0xFFFFB27C), Color(0xFFFFD6A8)),
        WallpaperPalette("Sunset", Color(0xFFFFEDE6), Color(0xFFD6457B), Color(0xFFF7D98A), Color(0xFFFFB27C)),
    )

    /** Index of the wallpaper used inside chats. */
    var selected by mutableIntStateOf(0)

    val current: WallpaperPalette get() = all[selected]
}
