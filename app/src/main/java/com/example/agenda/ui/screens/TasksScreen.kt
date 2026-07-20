package com.example.agenda.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agenda.data.*
import com.example.agenda.theme.*
import com.example.agenda.ui.components.*
import java.util.UUID

@Composable
fun TasksScreen(
  state: PlannerState,
  onUpdateState: ((PlannerState) -> PlannerState) -> Unit,
  modifier: Modifier = Modifier
) {
  var newAcademicText by remember { mutableStateOf("") }
  var newHomeText by remember { mutableStateOf("") }
  var newPersonalText by remember { mutableStateOf("") }

  ScreenFrame(
    cover = state.appearance.cover,
    title = "Mis Listas de Tareas",
    subtitle = "Organizadas por área de vida",
    bannerPath = state.customBannerPath,
    coverPath = state.customCoverPath,
    onUpdateBannerPath = { path ->
      onUpdateState { it.copy(customBannerPath = path) }
    },
    onUpdateCoverPath = { path ->
      onUpdateState { it.copy(customCoverPath = path) }
    }
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // COLUMN 1: Academic / Professional Tasks
      TaskColumnWidget(
        title = "Tareas Académicas / Profesionales",
        tasks = state.tasks.filter { it.category == TaskCategory.ACADEMIC },
        newTextValue = newAcademicText,
        onTextChange = { newAcademicText = it },
        onAddTask = { priority ->
          if (newAcademicText.isNotEmpty()) {
            onUpdateState { currentState ->
              currentState.copy(
                tasks = currentState.tasks + PlannerTask(
                  id = UUID.randomUUID().toString(),
                  title = newAcademicText,
                  category = TaskCategory.ACADEMIC,
                  priority = priority,
                  isCompleted = false,
                  date = "2026-07-15"
                )
              )
            }
            newAcademicText = ""
          }
        },
        onToggleTask = { taskId, isChecked ->
          onUpdateState { currentState ->
            currentState.copy(
              tasks = currentState.tasks.map {
                if (it.id == taskId) it.copy(isCompleted = isChecked) else it
              }
            )
          }
        },
        modifier = Modifier.weight(1f)
      )

      // COLUMN 2: Home Tasks
      TaskColumnWidget(
        title = "Tareas del Hogar",
        tasks = state.tasks.filter { it.category == TaskCategory.HOME },
        newTextValue = newHomeText,
        onTextChange = { newHomeText = it },
        onAddTask = { priority ->
          if (newHomeText.isNotEmpty()) {
            onUpdateState { currentState ->
              currentState.copy(
                tasks = currentState.tasks + PlannerTask(
                  id = UUID.randomUUID().toString(),
                  title = newHomeText,
                  category = TaskCategory.HOME,
                  priority = priority,
                  isCompleted = false,
                  date = "2026-07-15"
                )
              )
            }
            newHomeText = ""
          }
        },
        onToggleTask = { taskId, isChecked ->
          onUpdateState { currentState ->
            currentState.copy(
              tasks = currentState.tasks.map {
                if (it.id == taskId) it.copy(isCompleted = isChecked) else it
              }
            )
          }
        },
        modifier = Modifier.weight(1f)
      )

      // COLUMN 3: Personal Tasks
      TaskColumnWidget(
        title = "Tareas Personales",
        tasks = state.tasks.filter { it.category == TaskCategory.PERSONAL },
        newTextValue = newPersonalText,
        onTextChange = { newPersonalText = it },
        onAddTask = { priority ->
          if (newPersonalText.isNotEmpty()) {
            onUpdateState { currentState ->
              currentState.copy(
                tasks = currentState.tasks + PlannerTask(
                  id = UUID.randomUUID().toString(),
                  title = newPersonalText,
                  category = TaskCategory.PERSONAL,
                  priority = priority,
                  isCompleted = false,
                  date = "2026-07-15"
                )
              )
            }
            newPersonalText = ""
          }
        },
        onToggleTask = { taskId, isChecked ->
          onUpdateState { currentState ->
            currentState.copy(
              tasks = currentState.tasks.map {
                if (it.id == taskId) it.copy(isCompleted = isChecked) else it
              }
            )
          }
        },
        modifier = Modifier.weight(1f)
      )
    }
  }
}

@Composable
fun TaskColumnWidget(
  title: String,
  tasks: List<PlannerTask>,
  newTextValue: String,
  onTextChange: (String) -> Unit,
  onAddTask: (TaskPriority) -> Unit,
  onToggleTask: (String, Boolean) -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = LocalCustomColors.current
  val pendingCount = remember(tasks) { tasks.count { !it.isCompleted } }
  var selectedPriority by remember { mutableStateOf(TaskPriority.MEDIA) }

  PlannerCard(modifier = modifier.fillMaxHeight()) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Column Header Banner
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(colors.primary)
          .padding(horizontal = 14.dp, vertical = 10.dp)
      ) {
        Row(
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = title,
            fontFamily = TitleFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
          )
          
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(colors.darkAccent)
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = "$pendingCount pendientes",
              fontFamily = BodyFontFamily,
              fontSize = 10.sp,
              color = Color.White,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // List of tasks
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(12.dp),
        modifier = Modifier.weight(1f)
      ) {
        items(tasks) { task ->
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
          ) {
            PlannerCheckbox(
              checked = task.isCompleted,
              onCheckedChange = { onToggleTask(task.id, it) }
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
              text = task.title,
              style = Typography.bodyMedium.copy(
                textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                color = if (task.isCompleted) GrayText else colors.text
              ),
              modifier = Modifier.weight(1f),
              maxLines = 2,
              overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.width(6.dp))

            // Urgencia Badge
            val (badgeText, badgeBg) = when {
              task.isCompleted -> "hecho" to UrgenciaCompletada
              task.priority == TaskPriority.ALTA -> "alta" to UrgenciaAlta
              task.priority == TaskPriority.MEDIA -> "media" to UrgenciaMedia
              else -> "baja" to UrgenciaBaja
            }

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(badgeBg)
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text(
                text = badgeText,
                fontFamily = BodyFontFamily,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = if (task.isCompleted) GrayText else Color.White
              )
            }
          }
        }
      }

      // Priority Pills Selector
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Prioridad:",
          style = Typography.bodyMedium.copy(color = GrayText, fontSize = 11.sp)
        )
        
        // Alta Pill
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selectedPriority == TaskPriority.ALTA) UrgenciaAlta else Color.LightGray.copy(alpha = 0.25f))
            .clickable { selectedPriority = TaskPriority.ALTA }
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = "Alta",
            color = if (selectedPriority == TaskPriority.ALTA) Color.White else colors.text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
          )
        }
        
        // Media Pill
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selectedPriority == TaskPriority.MEDIA) UrgenciaMedia else Color.LightGray.copy(alpha = 0.25f))
            .clickable { selectedPriority = TaskPriority.MEDIA }
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = "Media",
            color = if (selectedPriority == TaskPriority.MEDIA) Color.White else colors.text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
          )
        }
        
        // Baja Pill
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (selectedPriority == TaskPriority.BAJA) UrgenciaBaja else Color.LightGray.copy(alpha = 0.25f))
            .clickable { selectedPriority = TaskPriority.BAJA }
            .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
          Text(
            text = "Baja",
            color = if (selectedPriority == TaskPriority.BAJA) Color.White else colors.text,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }

      // Add task field input
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .fillMaxWidth()
          .padding(start = 12.dp, end = 12.dp, bottom = 12.dp)
      ) {
        PlannerTextField(
          value = newTextValue,
          onValueChange = onTextChange,
          placeholder = "Añadir tarea...",
          modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.primary)
            .clickable {
              onAddTask(selectedPriority)
              // Reset local priority state to MEDIA after adding task
              selectedPriority = TaskPriority.MEDIA
            },
          contentAlignment = Alignment.Center
        ) {
          CustomAddIcon(
            color = Color.White
          )
        }
      }
    }
  }
}
