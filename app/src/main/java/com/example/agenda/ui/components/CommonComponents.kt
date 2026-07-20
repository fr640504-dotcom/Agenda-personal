package com.example.agenda.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File
import java.io.FileOutputStream
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.agenda.data.ColorPalette
import com.example.agenda.data.CoverImage
import com.example.agenda.data.UserProfile
import com.example.agenda.theme.*

@Composable
fun CustomAddIcon(
  modifier: Modifier = Modifier,
  color: Color = Color.White
) {
  Box(
    modifier = modifier.size(18.dp),
    contentAlignment = Alignment.Center
  ) {
    Box(modifier = Modifier.width(12.dp).height(2.dp).background(color))
    Box(modifier = Modifier.width(2.dp).height(12.dp).background(color))
  }
}

@Composable
fun CustomCheckIcon(
  modifier: Modifier = Modifier,
  color: Color = Color.White
) {
  androidx.compose.foundation.Canvas(modifier = modifier.size(12.dp)) {
    val w = size.width
    val h = size.height
    drawLine(
      color = color,
      start = Offset(x = w * 0.15f, y = h * 0.5f),
      end = Offset(x = w * 0.45f, y = h * 0.85f),
      strokeWidth = 2.dp.toPx(),
      cap = StrokeCap.Round
    )
    drawLine(
      color = color,
      start = Offset(x = w * 0.45f, y = h * 0.85f),
      end = Offset(x = w * 0.9f, y = h * 0.2f),
      strokeWidth = 2.dp.toPx(),
      cap = StrokeCap.Round
    )
  }
}

@Composable
fun CoverBanner(cover: CoverImage, modifier: Modifier = Modifier) {
  val context = LocalContext.current
  val resourceName = when (cover) {
    CoverImage.PINK_DESK -> "cover_pink_desk"
    else -> ""
  }
  
  val resourceId = remember(resourceName) {
    if (resourceName.isNotEmpty()) {
      context.resources.getIdentifier(resourceName, "drawable", context.packageName)
    } else 0
  }
  
  if (resourceId != 0) {
    Image(
      painter = painterResource(id = resourceId),
      contentDescription = "Personal cover",
      modifier = modifier.fillMaxWidth().height(150.dp),
      contentScale = ContentScale.Crop
    )
  } else {
    // Beautiful color palettes matching gradients/solids
    val brush = when (cover) {
      CoverImage.NAVY_BLUE_COVER -> Brush.horizontalGradient(listOf(Color(0xFF3B5998), Color(0xFF1F385C)))
      CoverImage.LAVENDER_COVER -> Brush.horizontalGradient(listOf(Color(0xFF8E7AB5), Color(0xFF6B5894)))
      CoverImage.ROSE_COVER -> Brush.horizontalGradient(listOf(Color(0xFFD67B93), Color(0xFFB55D74)))
      else -> Brush.horizontalGradient(listOf(Color(0xFFD67B93), Color(0xFFB55D74)))
    }
    Box(
      modifier = modifier
        .fillMaxWidth()
        .height(150.dp)
        .background(brush)
    )
  }
}

@Composable
fun ScreenFrame(
  cover: CoverImage,
  title: String,
  subtitle: String? = null,
  bannerPath: String? = null,
  coverPath: String? = null,
  onUpdateBannerPath: ((String) -> Unit)? = null,
  onUpdateCoverPath: ((String) -> Unit)? = null,
  content: @Composable ColumnScope.() -> Unit
) {
  val context = LocalContext.current

  val currentDateString = remember {
    try {
      val now = java.time.LocalDate.now()
      val formatter = java.time.format.DateTimeFormatter.ofPattern("EEEE, d 'DE' MMMM 'DE' yyyy", java.util.Locale("es", "ES"))
      now.format(formatter).uppercase()
    } catch (e: Exception) {
      "MIÉRCOLES, 15 DE JULIO DE 2026"
    }
  }

  val bannerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    if (uri != null && onUpdateBannerPath != null) {
      val path = saveImageToInternalStorageHelper(context, uri)
      if (path != null) {
        onUpdateBannerPath(path)
      }
    }
  }

  val coverLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    if (uri != null && onUpdateCoverPath != null) {
      val path = saveCoverImageToInternalStorageHelper(context, uri)
      if (path != null) {
        onUpdateCoverPath(path)
      }
    }
  }

  val bannerModifier = if (onUpdateBannerPath != null) {
    Modifier
      .fillMaxWidth()
      .clickable { bannerLauncher.launch("image/*") }
  } else {
    Modifier.fillMaxWidth()
  }

  val coverModifier = if (onUpdateCoverPath != null) {
    Modifier
      .fillMaxWidth()
      .height(150.dp)
      .clickable { coverLauncher.launch("image/*") }
  } else {
    Modifier.fillMaxWidth().height(150.dp)
  }

  Column(modifier = Modifier.fillMaxSize().background(LocalCustomColors.current.background)) {
    // Upper Cover Banner Section (Stretched and clickable to personalize)
    val coverBitmap = rememberFilePathBitmapHelper(coverPath ?: "")
    if (coverBitmap != null) {
      Box(modifier = coverModifier) {
        Image(
          bitmap = coverBitmap,
          contentDescription = "Custom Cover Banner",
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop
        )
      }
    } else {
      CoverBanner(cover = cover, modifier = coverModifier)
    }
    
    // Header date banner
    Box(
      modifier = bannerModifier
    ) {
      val bitmap = rememberFilePathBitmapHelper(bannerPath ?: "")
      if (bitmap != null) {
        Image(
          bitmap = bitmap,
          contentDescription = "Banner background",
          modifier = Modifier.matchParentSize(),
          contentScale = ContentScale.Crop
        )
        // Dark overlay to ensure text readability
        Box(
          modifier = Modifier
            .matchParentSize()
            .background(Color.Black.copy(alpha = 0.35f))
        )
      } else {
        Box(
          modifier = Modifier
            .matchParentSize()
            .background(
              Brush.verticalGradient(
                colors = listOf(
                  LocalCustomColors.current.primary,
                  LocalCustomColors.current.darkAccent
                )
              )
            )
        )
      }

      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 24.dp, vertical = 12.dp)
      ) {
        Text(
          text = currentDateString,
          style = Typography.labelSmall.copy(color = Color(0xFFE1DFDC), letterSpacing = 1.5.sp)
        )
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Text(
            text = "Mi Espacio Personal",
            fontFamily = TitleFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
            color = Color.White
          )
          if (onUpdateBannerPath != null) {
            Text(
              text = "✏️",
              fontSize = 14.sp
            )
          }
        }
      }
    }
    
    // Main Body
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(24.dp)
    ) {
      Text(
        text = title,
        fontFamily = TitleFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
        color = LocalCustomColors.current.text
      )
      if (subtitle != null) {
        Text(
          text = subtitle,
          style = Typography.bodyMedium.copy(color = GrayText),
          modifier = Modifier.padding(bottom = 16.dp)
        )
      } else {
        Spacer(modifier = Modifier.height(16.dp))
      }
      
      content()
    }
  }
}

fun saveCoverImageToInternalStorageHelper(context: Context, uri: Uri): String? {
  return try {
    val inputStream = context.contentResolver.openInputStream(uri) ?: return null
    val file = File(context.filesDir, "custom_cover.jpg")
    val outputStream = FileOutputStream(file)
    inputStream.use { input ->
      outputStream.use { output ->
        input.copyTo(output)
      }
    }
    file.absolutePath
  } catch (e: Exception) {
    e.printStackTrace()
    null
  }
}

// Helpers for ScreenFrame Banner custom background
fun saveImageToInternalStorageHelper(context: Context, uri: Uri): String? {
  return try {
    val inputStream = context.contentResolver.openInputStream(uri) ?: return null
    val file = File(context.filesDir, "custom_banner.jpg")
    val outputStream = FileOutputStream(file)
    inputStream.use { input ->
      outputStream.use { output ->
        input.copyTo(output)
      }
    }
    file.absolutePath
  } catch (e: Exception) {
    e.printStackTrace()
    null
  }
}

@Composable
fun rememberFilePathBitmapHelper(path: String): ImageBitmap? {
  return remember(path) {
    try {
      val file = File(path)
      if (file.exists()) {
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        bitmap?.asImageBitmap()
      } else {
        null
      }
    } catch (e: Exception) {
      null
    }
  }
}

@Composable
fun PlannerCard(
  modifier: Modifier = Modifier,
  border: BorderStroke? = BorderStroke(1.dp, LocalCustomColors.current.activeBorder.copy(alpha = 0.6f)),
  content: @Composable ColumnScope.() -> Unit
) {
  Card(
    modifier = modifier,
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = Color.White),
    border = border,
    content = content
  )
}

@Composable
fun PlannerCheckbox(
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = LocalCustomColors.current
  Box(
    modifier = modifier
      .size(22.dp)
      .clip(CircleShape)
      .background(if (checked) colors.primary else Color.Transparent)
      .border(1.5.dp, if (checked) colors.primary else Color.LightGray, CircleShape)
      .clickable { onCheckedChange(!checked) },
    contentAlignment = Alignment.Center
  ) {
    if (checked) {
      CustomCheckIcon(
        color = Color.White,
        modifier = Modifier.size(14.dp)
      )
    }
  }
}

@Composable
fun PlannerTextField(
  value: String,
  onValueChange: (String) -> Unit,
  placeholder: String,
  modifier: Modifier = Modifier,
  singleLine: Boolean = true,
  keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
  val colors = LocalCustomColors.current
  OutlinedTextField(
    value = value,
    onValueChange = onValueChange,
    placeholder = { Text(placeholder, style = Typography.bodyMedium.copy(color = GrayText)) },
    singleLine = singleLine,
    keyboardOptions = keyboardOptions,
    shape = RoundedCornerShape(12.dp),
    colors = OutlinedTextFieldDefaults.colors(
      focusedBorderColor = colors.primary,
      unfocusedBorderColor = colors.activeBorder,
      focusedContainerColor = Color.White,
      unfocusedContainerColor = colors.background.copy(alpha = 0.5f)
    ),
    textStyle = Typography.bodyMedium,
    modifier = modifier.fillMaxWidth()
  )
}

@Composable
fun PlannerButton(
  text: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  containerColor: Color = LocalCustomColors.current.primary,
  contentColor: Color = Color.White
) {
  Button(
    onClick = onClick,
    shape = RoundedCornerShape(12.dp),
    colors = ButtonDefaults.buttonColors(
      containerColor = containerColor,
      contentColor = contentColor
    ),
    modifier = modifier
  ) {
    Text(text = text, style = Typography.labelLarge.copy(fontWeight = FontWeight.Bold))
  }
}

@Composable
fun SidebarSectionHeader(title: String, modifier: Modifier = Modifier) {
  Text(
    text = title.uppercase(),
    style = Typography.labelSmall.copy(
      color = Color.LightGray,
      fontWeight = FontWeight.Bold,
      letterSpacing = 1.2.sp
    ),
    modifier = modifier.padding(vertical = 6.dp)
  )
}

@Composable
fun SidebarMenuItem(
  title: String,
  isSelected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = LocalCustomColors.current
  Box(
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 3.dp)
      .clip(RoundedCornerShape(8.dp))
      .background(if (isSelected) colors.primary else Color.Transparent)
      .clickable { onClick() }
      .padding(horizontal = 12.dp, vertical = 8.dp)
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      Text(
        text = title,
        style = Typography.bodyLarge.copy(
          color = if (isSelected) Color.White else colors.text.copy(alpha = 0.8f),
          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
      )
    }
  }
}

@Composable
fun SidebarNavigation(
  currentTab: String,
  onTabSelected: (String) -> Unit,
  profile: UserProfile,
  modifier: Modifier = Modifier
) {
  val colors = LocalCustomColors.current
  Column(
    modifier = modifier
      .fillMaxHeight()
      .width(280.dp)
      .background(colors.surface)
      .padding(20.dp)
  ) {
    // Header Logo
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.padding(bottom = 24.dp)
    ) {
      val context = LocalContext.current
      val iconResId = remember {
        context.resources.getIdentifier("ic_launcher", "drawable", context.packageName)
      }
      
      if (iconResId != 0) {
        Image(
          painter = painterResource(id = iconResId),
          contentDescription = "Logo Icon",
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape),
          contentScale = ContentScale.Crop
        )
      } else {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(colors.primary),
          contentAlignment = Alignment.Center
        ) {
          CustomAddIcon(
            color = Color.White,
            modifier = Modifier.size(20.dp)
          )
        }
      }
      Spacer(modifier = Modifier.width(10.dp))
      Column {
        Text(
          text = "Lumina",
          fontFamily = TitleFontFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 20.sp,
          color = colors.text
        )
        Text(
          text = "PLANNER",
          style = Typography.labelSmall.copy(color = GrayText, letterSpacing = 1.sp)
        )
      }
    }
    
    // Menu Sections
    Column(modifier = Modifier.weight(1f)) {
      SidebarSectionHeader(title = "Principal")
      SidebarMenuItem(title = "Tablero", isSelected = currentTab == "Dashboard", onClick = { onTabSelected("Dashboard") })
      
      Spacer(modifier = Modifier.height(8.dp))
      
      SidebarSectionHeader(title = "Planificación")
      SidebarMenuItem(title = "Planificador Mensual", isSelected = currentTab == "Monthly", onClick = { onTabSelected("Monthly") })
      SidebarMenuItem(title = "Planificador Diario", isSelected = currentTab == "Daily", onClick = { onTabSelected("Daily") })
      
      Spacer(modifier = Modifier.height(8.dp))
      
      SidebarSectionHeader(title = "Organización")
      SidebarMenuItem(title = "Mis Tareas", isSelected = currentTab == "Tasks", onClick = { onTabSelected("Tasks") })
      SidebarMenuItem(title = "Lista de Compras", isSelected = currentTab == "Shopping", onClick = { onTabSelected("Shopping") })
      SidebarMenuItem(title = "Finanzas", isSelected = currentTab == "Finances", onClick = { onTabSelected("Finances") })
      
      Spacer(modifier = Modifier.height(8.dp))
      
      SidebarSectionHeader(title = "Bienestar")
      SidebarMenuItem(title = "Hábitos & Estado", isSelected = currentTab == "Habits", onClick = { onTabSelected("Habits") })
      
      Spacer(modifier = Modifier.height(8.dp))
      
      SidebarSectionHeader(title = "Ajustes")
      SidebarMenuItem(title = "Apariencia", isSelected = currentTab == "Appearance", onClick = { onTabSelected("Appearance") })
    }
    
    Divider(color = colors.activeBorder.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 12.dp))
    
    // User Profile Footer Card
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(colors.background.copy(alpha = 0.6f))
        .padding(10.dp)
    ) {
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(CircleShape)
          .background(colors.primary.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
      ) {
        val initials = if (profile.name.isNotEmpty()) profile.name.take(1).uppercase() else "S"
        Text(
          text = initials,
          style = Typography.titleMedium.copy(color = colors.darkAccent, fontWeight = FontWeight.Bold)
        )
      }
      Spacer(modifier = Modifier.width(10.dp))
      Column {
        Text(
          text = profile.name,
          style = Typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = colors.text)
        )
        Text(
          text = "2026", // Or profile info/year
          style = Typography.labelSmall.copy(color = GrayText)
        )
      }
    }
  }
}
