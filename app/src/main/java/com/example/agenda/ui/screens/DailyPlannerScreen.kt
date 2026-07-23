package com.example.agenda.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agenda.data.*
import com.example.agenda.theme.*
import com.example.agenda.ui.components.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyPlannerScreen(
  state: PlannerState,
  onUpdateState: ((PlannerState) -> PlannerState) -> Unit,
  onNavigateToTab: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  var newEventTitle by remember { mutableStateOf("") }
  var newEventTime by remember { mutableStateOf("") }
  var selectedAmPm by remember { mutableStateOf("AM") }
  
  var newEventDuration by remember { mutableStateOf("") }
  var durationUnit by remember { mutableStateOf("min") }

  // Daily schedule is global (persistent across all days)
  val currentEvents = state.dailyEvents

  val currentNotes = remember(state.selectedDate, state.dailyNotesMap) {
    state.dailyNotesMap[state.selectedDate] ?: ""
  }

  val currentWater = remember(state.selectedDate, state.waterGlassesMap) {
    state.waterGlassesMap[state.selectedDate] ?: 0
  }

  ScreenFrame(
    cover = state.appearance.cover,
    title = "Planificador del Día",
    subtitle = "Horario y metas del día",
    bannerPath = state.customBannerPath,
    coverPath = state.customCoverPath
  ) {
    // DATE NAVIGATION BAR
    PlannerCard(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 16.dp)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          // Backward button
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(LocalCustomColors.current.primary.copy(alpha = 0.12f))
              .clickable {
                try {
                  val localDate = java.time.LocalDate.parse(state.selectedDate)
                  val newDate = localDate.minusDays(1).toString()
                  onUpdateState { it.copy(selectedDate = newDate) }
                } catch (e: Exception) {}
              },
            contentAlignment = Alignment.Center
          ) {
            Text("◀", color = LocalCustomColors.current.darkAccent, fontSize = 12.sp)
          }

          // Forward button
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(RoundedCornerShape(8.dp))
              .background(LocalCustomColors.current.primary.copy(alpha = 0.12f))
              .clickable {
                try {
                  val localDate = java.time.LocalDate.parse(state.selectedDate)
                  val newDate = localDate.plusDays(1).toString()
                  onUpdateState { it.copy(selectedDate = newDate) }
                } catch (e: Exception) {}
              },
            contentAlignment = Alignment.Center
          ) {
            Text("▶", color = LocalCustomColors.current.darkAccent, fontSize = 12.sp)
          }
          
          Spacer(modifier = Modifier.width(8.dp))

          // Date text
          val readableDate = remember(state.selectedDate) {
            try {
              val localDate = java.time.LocalDate.parse(state.selectedDate)
              val formatter = java.time.format.DateTimeFormatter.ofPattern("EEEE, d 'de' MMMM, yyyy", java.util.Locale("es", "ES"))
              val formatted = localDate.format(formatter)
              formatted.replaceFirstChar { it.uppercase() }
            } catch (e: Exception) {
              state.selectedDate
            }
          }

          Text(
            text = readableDate,
            fontFamily = TitleFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = LocalCustomColors.current.text
          )
        }

        // Today button if not today
        val todayStr = remember { getTodayDateString() }
        if (state.selectedDate != todayStr) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(LocalCustomColors.current.primary)
              .clickable {
                onUpdateState { it.copy(selectedDate = todayStr) }
              }
              .padding(horizontal = 12.dp, vertical = 6.dp)
          ) {
            Text(
              text = "Hoy",
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = 12.sp
            )
          }
        }
      }
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // LEFT PANEL: Hourly Timeline List
      PlannerCard(
        modifier = Modifier
          .weight(1.2f)
          .fillMaxHeight()
      ) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
          Text(
            text = "Horario del Día",
            fontFamily = TitleFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = LocalCustomColors.current.text,
            modifier = Modifier.padding(bottom = 12.dp)
          )
          
          LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.weight(1f)
          ) {
            items(currentEvents) { event ->
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(12.dp))
                  .background(
                    if (event.isCompleted) LocalCustomColors.current.lightHighlight.copy(alpha = 0.3f)
                    else Color(0xFFFAF9F6)
                  )
                  .border(
                    1.dp,
                    if (event.isCompleted) LocalCustomColors.current.primary.copy(alpha = 0.2f)
                    else Color.Transparent,
                    RoundedCornerShape(12.dp)
                  )
                  .padding(horizontal = 16.dp, vertical = 12.dp)
              ) {
                // Time tag
                Column(modifier = Modifier.width(74.dp)) {
                  Text(
                    text = event.time,
                    fontFamily = DataFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = LocalCustomColors.current.primary
                  )
                  Text(
                    text = event.duration,
                    style = Typography.labelSmall.copy(color = GrayText)
                  )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Event details
                Text(
                  text = event.title,
                  style = Typography.bodyLarge.copy(
                    fontWeight = FontWeight.Medium,
                    color = if (event.isCompleted) GrayText else LocalCustomColors.current.text,
                    textDecoration = if (event.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                  ),
                  modifier = Modifier.weight(1f)
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // Status circle check
                PlannerCheckbox(
                  checked = event.isCompleted,
                  onCheckedChange = { isChecked ->
                    onUpdateState { currentState ->
                      currentState.copy(
                        dailyEvents = currentState.dailyEvents.map {
                          if (it.id == event.id) it.copy(isCompleted = isChecked) else it
                        }
                      )
                    }
                  }
                )
                
                Spacer(modifier = Modifier.width(10.dp))
                
                // Delete button
                Text(
                  text = "❌",
                  fontSize = 11.sp,
                  modifier = Modifier.clickable {
                    onUpdateState { currentState ->
                      currentState.copy(
                        dailyEvents = currentState.dailyEvents.filter { it.id != event.id }
                      )
                    }
                  }
                )
              }
            }
          }

          // Form to add a new daily event
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier
              .fillMaxWidth()
              .padding(top = 10.dp)
          ) {
            // Activity Input
            PlannerTextField(
              value = newEventTitle,
              onValueChange = { newEventTitle = it },
              placeholder = "Actividad...",
              modifier = Modifier.weight(1.2f)
            )
            
            // Hour Input (digits)
            OutlinedTextField(
              value = newEventTime,
              onValueChange = { newEventTime = it },
              placeholder = { Text("Hora", style = Typography.bodyMedium.copy(color = GrayText)) },
              singleLine = true,
              shape = RoundedCornerShape(12.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LocalCustomColors.current.primary,
                unfocusedBorderColor = LocalCustomColors.current.activeBorder,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = LocalCustomColors.current.background.copy(alpha = 0.5f)
              ),
              textStyle = Typography.bodyMedium,
              modifier = Modifier.width(65.dp)
            )

            // AM/PM Toggle Pill
            Box(
              modifier = Modifier
                .width(42.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(LocalCustomColors.current.primary.copy(alpha = 0.15f))
                .clickable { selectedAmPm = if (selectedAmPm == "AM") "PM" else "AM" },
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = selectedAmPm,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = LocalCustomColors.current.darkAccent
              )
            }

            // Duration Input (digits)
            OutlinedTextField(
              value = newEventDuration,
              onValueChange = { newEventDuration = it },
              placeholder = { Text("Cant.", style = Typography.bodyMedium.copy(color = GrayText)) },
              singleLine = true,
              shape = RoundedCornerShape(12.dp),
              colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = LocalCustomColors.current.primary,
                unfocusedBorderColor = LocalCustomColors.current.activeBorder,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = LocalCustomColors.current.background.copy(alpha = 0.5f)
              ),
              textStyle = Typography.bodyMedium,
              modifier = Modifier.width(65.dp)
            )

            // Min/Hour Toggle Pill
            Box(
              modifier = Modifier
                .width(52.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(LocalCustomColors.current.primary.copy(alpha = 0.15f))
                .clickable { durationUnit = if (durationUnit == "min") "h" else "min" },
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = durationUnit,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = LocalCustomColors.current.darkAccent
              )
            }

            // Plus Button
            Box(
              modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(LocalCustomColors.current.primary)
                .clickable {
                  if (newEventTitle.isNotEmpty() && newEventTime.isNotEmpty()) {
                    onUpdateState { currentState ->
                      val formattedTime = if (!newEventTime.contains(":")) {
                        "$newEventTime:00"
                      } else {
                        val parts = newEventTime.split(":")
                        if (parts.size == 2 && parts[1].isEmpty()) {
                          "${newEventTime}00"
                        } else if (parts.size == 2 && parts[1].length == 1) {
                          "${newEventTime}0"
                        } else {
                          newEventTime
                        }
                      }
                      val finalTime = "$formattedTime $selectedAmPm"
                      val finalDuration = "${if (newEventDuration.isNotEmpty()) newEventDuration else "30"} $durationUnit"
                      currentState.copy(
                        dailyEvents = currentState.dailyEvents + DailyEvent(
                          id = UUID.randomUUID().toString(),
                          time = finalTime,
                          title = newEventTitle,
                          duration = finalDuration,
                          isCompleted = false
                        )
                      )
                    }
                    newEventTitle = ""
                    newEventTime = ""
                    newEventDuration = ""
                  }
                },
              contentAlignment = Alignment.Center
            ) {
              CustomAddIcon(color = Color.White)
            }
          }
        }
      }
      
      // RIGHT PANEL: Priorities, Notes, Water & Quote
      Column(
        modifier = Modifier
          .weight(0.8f)
          .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // Top Priorities Checklist
        PlannerCard(modifier = Modifier.fillMaxWidth()) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Prioridades Principales",
              fontFamily = TitleFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = LocalCustomColors.current.text,
              modifier = Modifier.padding(bottom = 12.dp)
            )
            
            val dailyPriorities = remember(state.tasks, state.selectedDate) {
              state.tasks.filter { it.priority == TaskPriority.ALTA && it.date == state.selectedDate }.take(3)
            }
            
            if (dailyPriorities.isNotEmpty()) {
              dailyPriorities.forEach { task ->
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                ) {
                  PlannerCheckbox(
                    checked = task.isCompleted,
                    onCheckedChange = { isChecked ->
                      onUpdateState { currentState ->
                        currentState.copy(
                          tasks = currentState.tasks.map {
                            if (it.id == task.id) it.copy(isCompleted = isChecked) else it
                          }
                        )
                      }
                    }
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  Text(
                    text = task.title,
                    style = Typography.bodyMedium.copy(
                      textDecoration = if (task.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                      color = if (task.isCompleted) GrayText else LocalCustomColors.current.text
                    ),
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                  )
                }
              }
            } else {
              Text(
                text = "No hay prioridades altas para hoy.",
                style = Typography.bodyMedium.copy(color = GrayText),
                modifier = Modifier.padding(vertical = 4.dp)
              )
            }
          }
        }
        
        // Notes Card
        PlannerCard(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f)
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Notas del Día",
              fontFamily = TitleFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = LocalCustomColors.current.text,
              modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Editable text area
            BasicTextField(
              value = currentNotes,
              onValueChange = { newText ->
                onUpdateState { currentState ->
                  val updatedMap = currentState.dailyNotesMap.toMutableMap()
                  updatedMap[currentState.selectedDate] = newText
                  currentState.copy(dailyNotesMap = updatedMap)
                }
              },
              textStyle = Typography.bodyMedium.copy(color = LocalCustomColors.current.text, lineHeight = 20.sp),
              modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
            )
          }
        }
        
        // Water Tracker Card
        PlannerCard(modifier = Modifier.fillMaxWidth()) {
          Column(modifier = Modifier.padding(16.dp)) {
            Row(
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth()
            ) {
              Text(
                text = "Agua",
                fontFamily = TitleFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = LocalCustomColors.current.text
              )
              Text(
                text = "$currentWater de 8 vasos",
                fontFamily = DataFontFamily,
                fontSize = 13.sp,
                color = LocalCustomColors.current.primary,
                fontWeight = FontWeight.Bold
              )
            }
            Spacer(modifier = Modifier.height(10.dp))
            
            // Grid of 8 glasses
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              for (glassIndex in 1..8) {
                val isDrunk = glassIndex <= currentWater
                val colors = LocalCustomColors.current
                Box(
                  modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                      if (isDrunk) colors.primary.copy(alpha = 0.8f)
                      else colors.background
                    )
                    .border(
                      1.dp,
                      if (isDrunk) colors.primary else colors.activeBorder,
                      RoundedCornerShape(8.dp)
                    )
                    .clickable {
                      onUpdateState { currentState ->
                        val updatedMap = currentState.waterGlassesMap.toMutableMap()
                        val currentVal = updatedMap[currentState.selectedDate] ?: 0
                        updatedMap[currentState.selectedDate] = if (currentVal == glassIndex) glassIndex - 1 else glassIndex
                        currentState.copy(waterGlassesMap = updatedMap)
                      }
                    },
                  contentAlignment = Alignment.Center
                ) {
                  if (isDrunk) {
                    CustomCheckIcon(
                      color = Color.White,
                      modifier = Modifier.size(16.dp)
                    )
                  }
                }
              }
            }
          }
        }
        
        // Custom Quote Card
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = LocalCustomColors.current.primary.copy(alpha = 0.15f)
          ),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "“La disciplina es elegir entre lo que quieres ahora y lo que más quieres.”",
              fontFamily = TitleFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
              color = LocalCustomColors.current.darkAccent,
              lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "— Abraham Lincoln",
              fontFamily = BodyFontFamily,
              fontWeight = FontWeight.Medium,
              fontSize = 12.sp,
              color = LocalCustomColors.current.text.copy(alpha = 0.8f),
              modifier = Modifier.align(Alignment.End)
            )
          }
        }
      }
    }
  }
}
