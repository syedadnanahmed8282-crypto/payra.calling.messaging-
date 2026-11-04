package com.payra.calling.designsystem

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Inter will be bundled later; until then the system font is used.
private val PayraFont = FontFamily.Default

val PayraTypography = Typography(
    displayLarge = TextStyle(fontFamily = PayraFont, fontWeight = FontWeight.Bold, fontSize = 34.sp, lineHeight = 40.sp),
    headlineMedium = TextStyle(fontFamily = PayraFont, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 32.sp),
    titleMedium = TextStyle(fontFamily = PayraFont, fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontFamily = PayraFont, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 21.sp),
    bodyMedium = TextStyle(fontFamily = PayraFont, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    labelSmall = TextStyle(fontFamily = PayraFont, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
)
