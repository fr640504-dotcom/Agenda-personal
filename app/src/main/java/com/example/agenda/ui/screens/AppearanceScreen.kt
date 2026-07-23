package com.example.agenda.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import com.example.agenda.data.*
import com.example.agenda.theme.*
import com.example.agenda.ui.components.*

@Composable
fun AppearanceScreen(
  state: PlannerState,
  onUpdateState: ((PlannerState) -> PlannerState) -> Unit,
  modifier: Modifier = Modifier
) {
  // Local profile states
  var name by remember(state.profile.name) { mutableStateOf(state.profile.name) }
  var email by remember(state.profile.email) { mutableStateOf(state.profile.email) }
  var language by remember(state.profile.language) { mutableStateOf(state.profile.language) }
  var timezone by remember(state.profile.timezone) { mutableStateOf(state.profile.timezone) }
  var dateFormat by remember(state.profile.dateFormat) { mutableStateOf(state.profile.dateFormat) }

  val context = androidx.compose.ui.platform.LocalContext.current
  val customCoverLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
    contract = androidx.activity.result.contract.ActivityResultContracts.GetContent()
  ) { uri: android.net.Uri? ->
    if (uri != null) {
      val path = saveCoverImageToInternalStorageHelper(context, uri)
      if (path != null) {
        onUpdateState { it.copy(customCoverPath = path) }
      }
    }
  }

  ScreenFrame(
    cover = state.appearance.cover,
    title = "Apariencia & Personalización",
    subtitle = "Haz tuyo este espacio",
    bannerPath = state.customBannerPath,
    coverPath = state.customCoverPath,
    onUpdateBannerPath = { path ->
      onUpdateState { it.copy(customBannerPath = path) }
    },
    onUpdateCoverPath = { path ->
      onUpdateState { it.copy(customCoverPath = path) }
    }
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
      // 1. PALETA DE COLORES
      Text(
        text = "Paleta de Colores",
        style = Typography.labelSmall.copy(color = GrayText, fontWeight = FontWeight.Bold),
        modifier = Modifier.padding(bottom = 4.dp)
      )
      
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // Option 1: Navy Blue
        PaletteCard(
          name = "Azul Marino & Rosa",
          desc = "Elegante y sofisticado",
          primaryColor = NavyBluePrimary,
          bgColors = listOf(NavyBluePrimary, NavyBlueDark, NavyBlueBackground),
          isSelected = state.appearance.palette == ColorPalette.NAVY_BLUE,
          onClick = {
            onUpdateState { it.copy(appearance = it.appearance.copy(palette = ColorPalette.NAVY_BLUE)) }
          },
          modifier = Modifier.weight(1f)
        )

        // Option 2: Purple Lavender
        PaletteCard(
          name = "Lavanda & Amatista",
          desc = "Espiritual y relajante",
          primaryColor = LavenderPrimary,
          bgColors = listOf(LavenderPrimary, LavenderDark, LavenderBackground),
          isSelected = state.appearance.palette == ColorPalette.PURPLE_LAVENDER,
          onClick = {
            onUpdateState { it.copy(appearance = it.appearance.copy(palette = ColorPalette.PURPLE_LAVENDER)) }
          },
          modifier = Modifier.weight(1f)
        )

        // Option 3: Pink Rose
        PaletteCard(
          name = "Cerezo & Rosa",
          desc = "Cálido y floreciente",
          primaryColor = RosePrimary,
          bgColors = listOf(RosePrimary, RoseDark, RoseBackground),
          isSelected = state.appearance.palette == ColorPalette.PINK_ROSE,
          onClick = {
            onUpdateState { it.copy(appearance = it.appearance.copy(palette = ColorPalette.PINK_ROSE)) }
          },
          modifier = Modifier.weight(1f)
        )
      }

      // 2. IMAGEN DE PORTADA
      Text(
        text = "Imagen de Portada",
        style = Typography.labelSmall.copy(color = GrayText, fontWeight = FontWeight.Bold),
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
      )
      
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        // Row 1: Pink Desk, Navy Blue, Lavender
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          CoverImageSelectionCard(
            title = "Escritorio rosa",
            cover = CoverImage.PINK_DESK,
            isSelected = state.appearance.cover == CoverImage.PINK_DESK && state.customCoverPath == null,
            onClick = {
              onUpdateState {
                it.copy(
                  appearance = it.appearance.copy(cover = CoverImage.PINK_DESK),
                  customCoverPath = null
                )
              }
            },
            modifier = Modifier.weight(1f)
          )

          CoverImageSelectionCard(
            title = "Azul Marino",
            cover = CoverImage.NAVY_BLUE_COVER,
            isSelected = state.appearance.cover == CoverImage.NAVY_BLUE_COVER && state.customCoverPath == null,
            onClick = {
              onUpdateState {
                it.copy(
                  appearance = it.appearance.copy(cover = CoverImage.NAVY_BLUE_COVER),
                  customCoverPath = null
                )
              }
            },
            modifier = Modifier.weight(1f)
          )

          CoverImageSelectionCard(
            title = "Lavanda Amatista",
            cover = CoverImage.LAVENDER_COVER,
            isSelected = state.appearance.cover == CoverImage.LAVENDER_COVER && state.customCoverPath == null,
            onClick = {
              onUpdateState {
                it.copy(
                  appearance = it.appearance.copy(cover = CoverImage.LAVENDER_COVER),
                  customCoverPath = null
                )
              }
            },
            modifier = Modifier.weight(1f)
          )
        }

        // Row 2: Rose Cherry, Custom Photo, and Spacer placeholder
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          CoverImageSelectionCard(
            title = "Cerezo & Rosa",
            cover = CoverImage.ROSE_COVER,
            isSelected = state.appearance.cover == CoverImage.ROSE_COVER && state.customCoverPath == null,
            onClick = {
              onUpdateState {
                it.copy(
                  appearance = it.appearance.copy(cover = CoverImage.ROSE_COVER),
                  customCoverPath = null
                )
              }
            },
            modifier = Modifier.weight(1f)
          )

          // Option 5: Custom Photo upload card
          CustomPhotoSelectionCard(
            title = "Mi foto personalizada",
            customPath = state.customCoverPath,
            isSelected = state.customCoverPath != null,
            onClick = {
              customCoverLauncher.launch("image/*")
            },
            modifier = Modifier.weight(1f)
          )

          Spacer(modifier = Modifier.weight(1f))
        }
      }

      // 3. PERFIL
      Text(
        text = "Perfil",
        style = Typography.labelSmall.copy(color = GrayText, fontWeight = FontWeight.Bold),
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
      )
      
      PlannerCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(20.dp)) {
          // Top Row: Avatar Initials & text details
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 16.dp)
          ) {
            Box(
              modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .background(LocalCustomColors.current.primary.copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              val initials = if (name.isNotEmpty()) name.take(1).uppercase() else "S"
              Text(
                text = initials,
                style = Typography.titleLarge.copy(color = LocalCustomColors.current.darkAccent, fontSize = 28.sp)
              )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
              Text(
                text = state.profile.name,
                fontFamily = TitleFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = LocalCustomColors.current.text
              )
            }
          }

          // Form Grid (Static read-only fields)
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(text = "NOMBRE", style = Typography.labelSmall.copy(color = GrayText, fontWeight = FontWeight.Bold))
              Spacer(modifier = Modifier.height(8.dp))
              Text(text = name, style = Typography.bodyLarge, color = LocalCustomColors.current.text)
            }

            Column(modifier = Modifier.weight(1f)) {
              Text(text = "IDIOMA", style = Typography.labelSmall.copy(color = GrayText, fontWeight = FontWeight.Bold))
              Spacer(modifier = Modifier.height(8.dp))
              Text(text = language, style = Typography.bodyLarge, color = LocalCustomColors.current.text)
            }
          }

          Spacer(modifier = Modifier.height(16.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(text = "ZONA HORARIA", style = Typography.labelSmall.copy(color = GrayText, fontWeight = FontWeight.Bold))
              Spacer(modifier = Modifier.height(8.dp))
              Text(text = timezone, style = Typography.bodyLarge, color = LocalCustomColors.current.text)
            }

            Column(modifier = Modifier.weight(1f)) {
              Text(text = "FORMATO DE FECHA", style = Typography.labelSmall.copy(color = GrayText, fontWeight = FontWeight.Bold))
              Spacer(modifier = Modifier.height(8.dp))
              Text(text = dateFormat, style = Typography.bodyLarge, color = LocalCustomColors.current.text)
            }
          }
        }
      }
    }
  }
}

@Composable
fun PaletteCard(
  name: String,
  desc: String,
  primaryColor: Color,
  bgColors: List<Color>,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = LocalCustomColors.current
  
  PlannerCard(
    modifier = modifier
      .clickable { onClick() }
      .border(
        1.5.dp,
        if (isSelected) colors.primary else colors.activeBorder.copy(alpha = 0.4f),
        RoundedCornerShape(16.dp)
      )
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      // Color dot bubbles
      Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = Modifier.padding(bottom = 12.dp)
      ) {
        bgColors.forEach { color ->
          Box(
            modifier = Modifier
              .size(24.dp)
              .clip(CircleShape)
              .background(color)
          )
        }
      }

      Row(
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = name,
            fontFamily = TitleFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = colors.text
          )
          Text(
            text = desc,
            style = Typography.bodyMedium.copy(color = GrayText)
          )
        }

        if (isSelected) {
          Box(
            modifier = Modifier
              .size(20.dp)
              .clip(CircleShape)
              .background(colors.primary),
            contentAlignment = Alignment.Center
          ) {
            CustomCheckIcon(
              color = Color.White,
              modifier = Modifier.size(12.dp)
            )
          }
        }
      }
    }
  }
}

@Composable
fun CoverImageSelectionCard(
  title: String,
  cover: CoverImage,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = LocalCustomColors.current
  
  PlannerCard(
    modifier = modifier
      .clickable { onClick() }
      .border(
        1.5.dp,
        if (isSelected) colors.primary else colors.activeBorder.copy(alpha = 0.4f),
        RoundedCornerShape(16.dp)
      )
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      // Cover preview box
      Box(modifier = Modifier.fillMaxWidth()) {
        CoverBanner(cover = cover, modifier = Modifier.height(70.dp).clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)))
        
        if (isSelected) {
          Box(
            modifier = Modifier
              .align(Alignment.Center)
              .size(24.dp)
              .clip(CircleShape)
              .background(Color.White.copy(alpha = 0.9f)),
            contentAlignment = Alignment.Center
          ) {
            CustomCheckIcon(
              color = colors.primary,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }
      
      Text(
        text = title,
        style = Typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
        modifier = Modifier.padding(10.dp),
        maxLines = 1,
        color = colors.text
      )
    }
  }
}

@Composable
fun CustomPhotoSelectionCard(
  title: String,
  customPath: String?,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = LocalCustomColors.current
  val bitmap = rememberFilePathBitmapHelper(customPath ?: "")
  
  PlannerCard(
    modifier = modifier
      .clickable { onClick() }
      .border(
        1.5.dp,
        if (isSelected) colors.primary else colors.activeBorder.copy(alpha = 0.4f),
        RoundedCornerShape(16.dp)
      )
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(70.dp)
          .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
          .background(colors.activeBorder.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
      ) {
        if (bitmap != null) {
          Image(
            bitmap = bitmap,
            contentDescription = "Custom cover preview",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
          )
        } else {
          Text("📷", fontSize = 24.sp)
        }
        
        if (isSelected) {
          Box(
            modifier = Modifier
              .align(Alignment.Center)
              .size(24.dp)
              .clip(CircleShape)
              .background(Color.White.copy(alpha = 0.9f)),
            contentAlignment = Alignment.Center
          ) {
            CustomCheckIcon(
              color = colors.primary,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }
      
      Text(
        text = title,
        style = Typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
        modifier = Modifier.padding(10.dp),
        maxLines = 1,
        color = colors.text
      )
    }
  }
}
