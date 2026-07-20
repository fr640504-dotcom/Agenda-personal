package com.example.agenda.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.example.agenda.data.ColorPalette

data class CustomColors(
  val primary: Color,
  val darkAccent: Color,
  val background: Color,
  val surface: Color,
  val text: Color,
  val lightHighlight: Color,
  val activeBorder: Color
)

val LocalCustomColors = staticCompositionLocalOf {
  CustomColors(
    primary = SageGreenPrimary,
    darkAccent = SageGreenDark,
    background = SageGreenBackground,
    surface = SageGreenSurface,
    text = SageGreenText,
    lightHighlight = SageGreenLightHighlight,
    activeBorder = SageGreenActiveBorder
  )
}

@Composable
fun AgendaTheme(
  palette: ColorPalette = ColorPalette.NAVY_BLUE,
  content: @Composable () -> Unit
) {
  val customColors = when (palette) {
    ColorPalette.NAVY_BLUE -> CustomColors(
      primary = NavyBluePrimary,
      darkAccent = NavyBlueDark,
      background = NavyBlueBackground,
      surface = NavyBlueSurface,
      text = NavyBlueText,
      lightHighlight = NavyBlueLightHighlight,
      activeBorder = NavyBlueActiveBorder
    )
    ColorPalette.PURPLE_LAVENDER -> CustomColors(
      primary = LavenderPrimary,
      darkAccent = LavenderDark,
      background = LavenderBackground,
      surface = LavenderSurface,
      text = LavenderText,
      lightHighlight = LavenderLightHighlight,
      activeBorder = LavenderActiveBorder
    )
    ColorPalette.PINK_ROSE -> CustomColors(
      primary = RosePrimary,
      darkAccent = RoseDark,
      background = RoseBackground,
      surface = RoseSurface,
      text = RoseText,
      lightHighlight = RoseLightHighlight,
      activeBorder = RoseActiveBorder
    )
  }

  val colorScheme = lightColorScheme(
    primary = customColors.primary,
    onPrimary = Color.White,
    primaryContainer = customColors.lightHighlight,
    onPrimaryContainer = customColors.darkAccent,
    background = customColors.background,
    onBackground = customColors.text,
    surface = customColors.surface,
    onSurface = customColors.text,
    outline = customColors.activeBorder
  )

  CompositionLocalProvider(LocalCustomColors provides customColors) {
    MaterialTheme(
      colorScheme = colorScheme,
      typography = Typography,
      content = content
    )
  }
}
