package com.example.agenda.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val TitleFontFamily = FontFamily.Serif
val BodyFontFamily = FontFamily.SansSerif
val DataFontFamily = FontFamily.Monospace

val Typography = Typography(
  displayLarge = TextStyle(
    fontFamily = TitleFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 32.sp
  ),
  titleLarge = TextStyle(
    fontFamily = TitleFontFamily,
    fontWeight = FontWeight.Bold,
    fontSize = 24.sp
  ),
  titleMedium = TextStyle(
    fontFamily = TitleFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 18.sp
  ),
  bodyLarge = TextStyle(
    fontFamily = BodyFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 16.sp
  ),
  bodyMedium = TextStyle(
    fontFamily = BodyFontFamily,
    fontWeight = FontWeight.Normal,
    fontSize = 14.sp
  ),
  labelLarge = TextStyle(
    fontFamily = BodyFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 14.sp
  ),
  labelSmall = TextStyle(
    fontFamily = DataFontFamily,
    fontWeight = FontWeight.Medium,
    fontSize = 11.sp
  )
)
