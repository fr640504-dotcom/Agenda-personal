package com.example.agenda.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agenda.data.*
import com.example.agenda.theme.*
import com.example.agenda.ui.components.*
import java.util.UUID

@Composable
fun HabitsScreen(
  state: PlannerState,
  onUpdateState: ((PlannerState) -> PlannerState) -> Unit,
  modifier: Modifier = Modifier
) {
  var newHabitName by remember { mutableStateOf("") }
  
  // Local state for today's check-in form
  var selectedMood by remember { mutableStateOf<MoodType?>(null) }
  var energyLevel by remember { mutableStateOf("Medio") }

  // Statistics calculations
  val completedToday = remember(state.habits, state.selectedDate) {
    state.habits.count { it.completions[state.selectedDate] == true }
  }
  val totalHabits = state.habits.size

  val avgMood = remember(state.moodCheckIns) {
    if (state.moodCheckIns.isEmpty()) 0.0 else {
      state.moodCheckIns.sumOf {
        when (it.mood) {
          MoodType.AGOTADO -> 1.0
          MoodType.BAJO -> 2.0
          MoodType.NORMAL -> 3.0
          MoodType.BIEN -> 4.0
          MoodType.GENIAL -> 5.0
          MoodType.INCREIBLE -> 6.0
        }
      } / state.moodCheckIns.size
    }
  }

  // Calculate dynamic active streak across all habits
  val activeStreak = remember(state.habits) {
    val completionDates = state.habits.flatMap { it.completions.filter { c -> c.value }.keys }.toSet()
    if (completionDates.isEmpty()) 0 else {
      var streak = 0
      var checkDate = java.time.LocalDate.now()
      val formatter = java.time.format.DateTimeFormatter.ISO_LOCAL_DATE
      while (true) {
        val dateString = checkDate.format(formatter)
        if (completionDates.contains(dateString)) {
          streak++
          checkDate = checkDate.minusDays(1)
        } else {
          if (streak == 0) {
            checkDate = checkDate.minusDays(1)
            val yesterdayString = checkDate.format(formatter)
            if (completionDates.contains(yesterdayString)) {
              streak++
              checkDate = checkDate.minusDays(1)
              continue
            }
          }
          break
        }
      }
      streak
    }
  }

  // Generate all dates of the active month dynamically
  val monthDates = remember(state.currentMonth) {
    val list = mutableListOf<String>()
    try {
      val parts = state.currentMonth.split("-")
      val year = parts[0].toInt()
      val month = parts[1].toInt()
      val cal = java.util.GregorianCalendar(year, month - 1, 1)
      val maxDays = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
      for (day in 1..maxDays) {
        list.add(String.format(java.util.Locale.US, "%d-%02d-%02d", year, month, day))
      }
    } catch (e: Exception) {
      for (i in 1..31) {
        list.add("2026-07-${String.format("%02d", i)}")
      }
    }
    list
  }

  ScreenFrame(
    cover = state.appearance.cover,
    title = "Hábitos & Bienestar",
    subtitle = "Seguimiento de salud física y mental",
    bannerPath = state.customBannerPath,
    coverPath = state.customCoverPath
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // LEFT COLUMN: Habits Grid & Mood Chart
      Column(
        modifier = Modifier
          .weight(1.3f)
          .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // Habit Tracker Monthly history Card
        PlannerCard(modifier = Modifier.weight(1.2f)) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Rastreador de Hábitos — Todo el Mes",
              fontFamily = TitleFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = LocalCustomColors.current.text,
              modifier = Modifier.padding(bottom = 12.dp)
            )

            // Header for the grid: Days of the active month
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = "Hábito",
                style = Typography.labelSmall.copy(color = GrayText),
                modifier = Modifier.width(110.dp)
              )
              Spacer(modifier = Modifier.width(10.dp))
              
              Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                monthDates.forEach { dateStr ->
                  val dayNum = try {
                    dateStr.split("-").getOrNull(2)?.toIntOrNull()?.toString() ?: ""
                  } catch (e: Exception) {
                    ""
                  }
                  Text(
                    text = dayNum,
                    fontFamily = DataFontFamily,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = GrayText,
                    modifier = Modifier.width(14.dp),
                    textAlign = TextAlign.Center
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Habit Rows
            LazyColumn(
              verticalArrangement = Arrangement.spacedBy(8.dp),
              modifier = Modifier.weight(1f)
            ) {
              items(state.habits) { habit ->
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = habit.name,
                    style = Typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                    modifier = Modifier.width(110.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Spacer(modifier = Modifier.width(10.dp))
                  
                  // Monthly check squares
                  Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.SpaceBetween
                  ) {
                    monthDates.forEach { dateStr ->
                      val isCompleted = habit.completions[dateStr] == true
                      
                      Box(
                        modifier = Modifier
                          .size(14.dp)
                          .clip(RoundedCornerShape(3.dp))
                          .background(
                            if (isCompleted) LocalCustomColors.current.primary
                            else LocalCustomColors.current.activeBorder.copy(alpha = 0.3f)
                          )
                          .clickable {
                            onUpdateState { currentState ->
                              currentState.copy(
                                habits = currentState.habits.map { h ->
                                  if (h.id == habit.id) {
                                    val newCompletions = h.completions.toMutableMap()
                                    newCompletions[dateStr] = !isCompleted
                                    h.copy(completions = newCompletions)
                                  } else h
                                }
                              )
                            }
                          }
                      )
                    }
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Add Habit Field Input
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth()
            ) {
              PlannerTextField(
                value = newHabitName,
                onValueChange = { newHabitName = it },
                placeholder = "Nuevo hábito...",
                modifier = Modifier.weight(1f)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .background(LocalCustomColors.current.primary)
                  .clickable {
                    if (newHabitName.isNotEmpty()) {
                      onUpdateState { currentState ->
                        currentState.copy(
                          habits = currentState.habits + HabitTracker(
                            id = UUID.randomUUID().toString(),
                            name = newHabitName,
                            completions = emptyMap()
                          )
                        )
                      }
                      newHabitName = ""
                    }
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

        // Mood History 15-day Bar Chart Card
        PlannerCard(modifier = Modifier.weight(0.8f)) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Estado de Ánimo — Últimos 15 Días",
              style = Typography.labelSmall.copy(color = GrayText, fontWeight = FontWeight.Bold),
              modifier = Modifier.padding(bottom = 12.dp)
            )

            // Bar Chart Canvas (represented by rows/boxes)
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.Bottom
            ) {
              state.moodCheckIns.takeLast(15).forEachIndexed { index, checkIn ->
                val rating = when (checkIn.mood) {
                  MoodType.AGOTADO -> 1
                  MoodType.BAJO -> 2
                  MoodType.NORMAL -> 3
                  MoodType.BIEN -> 4
                  MoodType.GENIAL -> 5
                  MoodType.INCREIBLE -> 6
                }
                
                Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  verticalArrangement = Arrangement.Bottom,
                  modifier = Modifier.weight(1f)
                ) {
                  // The bar
                  Box(
                    modifier = Modifier
                      .fillMaxHeight(fraction = rating / 6f)
                      .width(10.dp)
                      .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                      .background(
                        when (checkIn.mood) {
                          MoodType.INCREIBLE -> UrgenciaBaja
                          MoodType.GENIAL -> LocalCustomColors.current.primary
                          MoodType.BIEN -> LocalCustomColors.current.primary.copy(alpha = 0.7f)
                          MoodType.NORMAL -> UrgenciaMedia
                          else -> UrgenciaAlta
                        }
                      )
                  )
                  Spacer(modifier = Modifier.height(4.dp))
                  // Date dot indicator
                  Box(
                    modifier = Modifier
                      .size(6.dp)
                      .clip(CircleShape)
                      .background(
                        if (checkIn.date == state.selectedDate) LocalCustomColors.current.primary
                        else Color.LightGray
                      )
                  )
                }
              }
            }
          }
        }
      }

      // RIGHT COLUMN: Check-In Form & Statistics
      Column(
        modifier = Modifier
          .weight(0.8f)
          .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // Today Check-In Card
        PlannerCard {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Check-in de Hoy",
              fontFamily = TitleFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = LocalCustomColors.current.text
            )
            Text(
              text = "¿Cómo te sientes?",
              style = Typography.bodyMedium.copy(color = GrayText),
              modifier = Modifier.padding(bottom = 12.dp)
            )

            // Mood Emojis Grid
            val moods = listOf(
              MoodType.AGOTADO to "😫\nAgotado",
              MoodType.BAJO to "🙁\nBajo",
              MoodType.NORMAL to "😐\nNormal",
              MoodType.BIEN to "🙂\nBien",
              MoodType.GENIAL to "😀\nGenial",
              MoodType.INCREIBLE to "🌟\nIncreíble"
            )
            
            // Render 2 rows of 3 emojis
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
              for (row in 0 until 2) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  for (col in 0 until 3) {
                    val index = row * 3 + col
                    val moodItem = moods[index]
                    val isSelected = selectedMood == moodItem.first
                    
                    Box(
                      modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                          if (isSelected) LocalCustomColors.current.primary.copy(alpha = 0.2f)
                          else Color(0xFFFAF9F6)
                        )
                        .border(
                          1.dp,
                          if (isSelected) LocalCustomColors.current.primary
                          else LocalCustomColors.current.activeBorder.copy(alpha = 0.3f),
                          RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedMood = moodItem.first }
                        .padding(8.dp),
                      contentAlignment = Alignment.Center
                    ) {
                      Text(
                        text = moodItem.second,
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isSelected) LocalCustomColors.current.darkAccent else LocalCustomColors.current.text
                      )
                    }
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Energy Slider Selection
            Text(
              text = "Nivel de energía",
              style = Typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
              modifier = Modifier.padding(bottom = 6.dp)
            )
            
            val levels = listOf("Bajo", "Medio", "Alto")
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              levels.forEach { lvl ->
                val isSelected = energyLevel == lvl
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                      if (isSelected) LocalCustomColors.current.primary
                      else Color(0xFFFAF9F6)
                    )
                    .border(
                      1.dp,
                      if (isSelected) LocalCustomColors.current.primary
                      else LocalCustomColors.current.activeBorder.copy(alpha = 0.3f),
                      RoundedCornerShape(8.dp)
                    )
                    .clickable { energyLevel = lvl }
                    .padding(vertical = 8.dp),
                  contentAlignment = Alignment.Center
                ) {
                  Text(
                    text = lvl,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) Color.White else LocalCustomColors.current.text
                  )
                }
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            PlannerButton(
              text = "Guardar check-in +",
              onClick = {
                selectedMood?.let { mood ->
                  onUpdateState { currentState ->
                    // Remove existing entry for selectedDate if any, and append new check-in
                    val cleanCheckIns = currentState.moodCheckIns.filter { it.date != currentState.selectedDate }
                    currentState.copy(
                      moodCheckIns = cleanCheckIns + MoodCheckIn(currentState.selectedDate, mood, energyLevel)
                    )
                  }
                }
              },
              modifier = Modifier.fillMaxWidth()
            )
          }
        }

        // Statistics Summary Card
        PlannerCard {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Estadísticas",
              fontFamily = TitleFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = LocalCustomColors.current.text,
              modifier = Modifier.padding(bottom = 12.dp)
            )

            // Stat 1: Best Streak
            Row(
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(text = "Racha activa global", style = Typography.bodyMedium.copy(color = GrayText))
              Text(
                text = "$activeStreak días 🔥",
                fontFamily = DataFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = LocalCustomColors.current.text
              )
            }
            
            Divider(color = LocalCustomColors.current.activeBorder.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))

            // Stat 2: Habits Completed Today
            Row(
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(text = "Hábitos completados hoy", style = Typography.bodyMedium.copy(color = GrayText))
              Text(
                text = "$completedToday de $totalHabits",
                fontFamily = DataFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = LocalCustomColors.current.text
              )
            }

            Divider(color = LocalCustomColors.current.activeBorder.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))

            // Stat 3: Average Mood
            Row(
              horizontalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(text = "Estado de ánimo promedio", style = Typography.bodyMedium.copy(color = GrayText))
              Text(
                text = "${String.format("%.1f", avgMood)} / 6.0",
                fontFamily = DataFontFamily,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = LocalCustomColors.current.primary
              )
            }
          }
        }
      }
    }
  }
}
