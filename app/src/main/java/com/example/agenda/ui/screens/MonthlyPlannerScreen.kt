package com.example.agenda.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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

@Composable
fun MonthlyPlannerScreen(
  state: PlannerState,
  onUpdateState: ((PlannerState) -> PlannerState) -> Unit,
  onNavigateToTab: (String) -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = LocalCustomColors.current
  
  // Parse currentMonth string "2026-07"
  val parts = state.currentMonth.split("-")
  val currentYear = parts.getOrNull(0)?.toIntOrNull() ?: 2026
  val currentMonthIndex = (parts.getOrNull(1)?.toIntOrNull() ?: 7) - 1 // 0-indexed for Calendar

  val monthName = remember(currentMonthIndex) {
    when (currentMonthIndex) {
      0 -> "Enero"
      1 -> "Febrero"
      2 -> "Marzo"
      3 -> "Abril"
      4 -> "Mayo"
      5 -> "Junio"
      6 -> "Julio"
      7 -> "Agosto"
      8 -> "Septiembre"
      9 -> "Octubre"
      10 -> "Noviembre"
      11 -> "Diciembre"
      else -> "Julio"
    }
  }

  // Current record of monthly values
  val currentRecord = state.monthlyRecords[state.currentMonth] ?: MonthlyRecord()
  val dayEvents = currentRecord.calendarEvents

  val selectedDay = remember(state.selectedDate, state.currentMonth) {
    if (state.selectedDate.startsWith(state.currentMonth)) {
      try {
        val dateParts = state.selectedDate.split("-")
        dateParts.getOrNull(2)?.toIntOrNull()?.toString() ?: ""
      } catch (e: Exception) {
        ""
      }
    } else {
      ""
    }
  }
  var newEventText by remember { mutableStateOf("") }

  // Dynamically calculate days of the month and starting offset day of week (Monday=0)
  val days = remember(currentYear, currentMonthIndex) {
    val cal = java.util.GregorianCalendar(currentYear, currentMonthIndex, 1)
    val dayOfWeek = cal.get(java.util.Calendar.DAY_OF_WEEK)
    val startOffset = when (dayOfWeek) {
      java.util.Calendar.MONDAY -> 0
      java.util.Calendar.TUESDAY -> 1
      java.util.Calendar.WEDNESDAY -> 2
      java.util.Calendar.THURSDAY -> 3
      java.util.Calendar.FRIDAY -> 4
      java.util.Calendar.SATURDAY -> 5
      java.util.Calendar.SUNDAY -> 6
      else -> 0
    }
    
    val maxDays = cal.getActualMaximum(java.util.Calendar.DAY_OF_MONTH)
    val list = mutableListOf<String>()
    for (i in 0 until startOffset) {
      list.add("")
    }
    for (i in 1..maxDays) {
      list.add(i.toString())
    }
    while (list.size % 7 != 0) {
      list.add("")
    }
    list
  }

  ScreenFrame(
    cover = state.appearance.cover,
    title = "Planificador Mensual",
    subtitle = "Calendario de actividades y metas",
    bannerPath = state.customBannerPath,
    coverPath = state.customCoverPath
  ) {
    // Month navigation header
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 14.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Prev month button
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(colors.primary.copy(alpha = 0.15f))
          .clickable {
            var newMonthIndex = currentMonthIndex - 1
            var newYear = currentYear
            if (newMonthIndex < 0) {
              newMonthIndex = 11
              newYear--
            }
            val newMonthStr = String.format("%d-%02d", newYear, newMonthIndex + 1)
            onUpdateState { currentState ->
              // Initialize monthly record if empty
              val records = currentState.monthlyRecords
              val updatedRecords = if (!records.containsKey(newMonthStr)) {
                records + (newMonthStr to MonthlyRecord())
              } else records
              currentState.copy(currentMonth = newMonthStr, monthlyRecords = updatedRecords)
            }
          },
        contentAlignment = Alignment.Center
      ) {
        Text("◀", color = colors.darkAccent, fontSize = 14.sp)
      }

      // Selected Month Title text
      Text(
        text = "$monthName de $currentYear",
        fontFamily = TitleFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        color = colors.text
      )

      // Next month button
      Box(
        modifier = Modifier
          .size(40.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(colors.primary.copy(alpha = 0.15f))
          .clickable {
            var newMonthIndex = currentMonthIndex + 1
            var newYear = currentYear
            if (newMonthIndex > 11) {
              newMonthIndex = 0
              newYear++
            }
            val newMonthStr = String.format("%d-%02d", newYear, newMonthIndex + 1)
            onUpdateState { currentState ->
              val records = currentState.monthlyRecords
              val updatedRecords = if (!records.containsKey(newMonthStr)) {
                records + (newMonthStr to MonthlyRecord())
              } else records
              currentState.copy(currentMonth = newMonthStr, monthlyRecords = updatedRecords)
            }
          },
        contentAlignment = Alignment.Center
      ) {
        Text("▶", color = colors.darkAccent, fontSize = 14.sp)
      }
    }

    // TOP PANELS: Calendar & Daily Details side-by-side
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Left side: Calendar Grid Panel
      PlannerCard(modifier = Modifier.weight(1.3f)) {
        Column(modifier = Modifier.padding(16.dp)) {
          // Calendar header days names
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            val weekdays = listOf("L", "M", "M", "J", "V", "S", "D")
            weekdays.forEach { dayName ->
              Text(
                text = dayName,
                fontFamily = TitleFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = GrayText,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
              )
            }
          }
          
          Spacer(modifier = Modifier.height(8.dp))
          
          // Calendar Grid items layout (Weighted to fill container height/width)
          Column(
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.weight(1f).fillMaxWidth()
          ) {
            val totalWeeks = days.size / 7
            for (week in 0 until totalWeeks) {
              Row(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                for (dayIndex in 0 until 7) {
                  val dayStr = days.getOrNull(week * 7 + dayIndex) ?: ""
                  val isSelected = dayStr == selectedDay && dayStr.isNotEmpty()
                  val hasEvents = dayStr.isNotEmpty() && dayEvents[dayStr]?.isNotEmpty() == true
                  
                  Box(
                    modifier = Modifier
                      .weight(1f)
                      .fillMaxHeight()
                      .padding(vertical = 4.dp)
                      .clip(RoundedCornerShape(12.dp))
                      .background(
                        if (isSelected) colors.primary
                        else if (dayStr.isEmpty()) Color.Transparent
                        else Color(0xFFFAF9F6)
                      )
                      .clickable(enabled = dayStr.isNotEmpty()) {
                        try {
                          val newDateStr = String.format(java.util.Locale.US, "%d-%02d-%02d", currentYear, currentMonthIndex + 1, dayStr.toInt())
                          onUpdateState { it.copy(selectedDate = newDateStr) }
                        } catch (e: Exception) {}
                      },
                    contentAlignment = Alignment.Center
                  ) {
                    if (dayStr.isNotEmpty()) {
                      Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                      ) {
                        Text(
                          text = dayStr,
                          fontFamily = DataFontFamily,
                          fontSize = 16.sp,
                          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                          color = if (isSelected) Color.White else colors.text
                        )
                        
                        if (hasEvents) {
                          Spacer(modifier = Modifier.height(4.dp))
                          Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                            val count = dayEvents[dayStr]?.size ?: 0
                            for (i in 0 until minOf(count, 3)) {
                              Box(
                                modifier = Modifier
                                  .size(5.dp)
                                  .clip(CircleShape)
                                  .background(
                                    if (isSelected) Color.White
                                    else if (i == 0) UrgenciaAlta
                                    else if (i == 1) UrgenciaMedia
                                    else UrgenciaBaja
                                  )
                              )
                            }
                          }
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }

      // Right side: Selected Day Detail list
      PlannerCard(modifier = Modifier.weight(1f)) {
        Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "EVENTOS DEL DÍA",
              style = Typography.labelSmall.copy(color = GrayText, fontWeight = FontWeight.Bold)
            )
            if (selectedDay.isNotEmpty()) {
              Text(
                text = "Ver Detalle ➔",
                style = Typography.labelSmall.copy(color = colors.primary, fontWeight = FontWeight.Bold),
                modifier = Modifier.clickable { onNavigateToTab("Daily") }
              )
            }
          }
          Text(
            text = if (selectedDay.isNotEmpty()) "$selectedDay de $monthName, $currentYear" else "Selecciona un día",
            style = Typography.bodyMedium.copy(color = GrayText),
            modifier = Modifier.padding(bottom = 12.dp)
          )
          
          val events = remember(selectedDay, dayEvents) {
            if (selectedDay.isNotEmpty()) dayEvents[selectedDay] ?: emptyList() else emptyList()
          }
          
          if (events.isNotEmpty()) {
            LazyColumn(
              verticalArrangement = Arrangement.spacedBy(10.dp),
              modifier = Modifier.weight(1f)
            ) {
              items(events) { event ->
                Box(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(colors.background.copy(alpha = 0.5f))
                    .padding(12.dp)
                ) {
                  Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    Text(
                      text = event,
                      style = Typography.bodyMedium.copy(color = colors.text),
                      modifier = Modifier.weight(1f)
                    )
                    Text(
                      text = "❌",
                      fontSize = 11.sp,
                      modifier = Modifier.clickable {
                        onUpdateState { currentState ->
                          val record = currentState.monthlyRecords[currentState.currentMonth] ?: MonthlyRecord()
                          val dayEvs = record.calendarEvents[selectedDay] ?: emptyList()
                          val updatedEvs = dayEvs - event
                          val updatedRecord = record.copy(
                            calendarEvents = record.calendarEvents + (selectedDay to updatedEvs)
                          )
                          currentState.copy(
                            monthlyRecords = currentState.monthlyRecords + (currentState.currentMonth to updatedRecord)
                          )
                        }
                      }
                    )
                  }
                }
              }
            }
          } else {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = if (selectedDay.isNotEmpty()) "No hay eventos programados para este día." else "Haz clic en un día del calendario para ver o programar eventos.",
                style = Typography.bodyMedium.copy(color = GrayText),
                textAlign = TextAlign.Center
              )
            }
          }

          // Add Event bar input inside card
          if (selectedDay.isNotEmpty()) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
            ) {
              PlannerTextField(
                value = newEventText,
                onValueChange = { newEventText = it },
                placeholder = "Añadir evento...",
                modifier = Modifier.weight(1f)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .background(colors.primary)
                  .clickable {
                    if (newEventText.isNotEmpty()) {
                      onUpdateState { currentState ->
                        val record = currentState.monthlyRecords[currentState.currentMonth] ?: MonthlyRecord()
                        val dayEvs = record.calendarEvents[selectedDay] ?: emptyList()
                        val updatedEvs = dayEvs + newEventText
                        val updatedRecord = record.copy(
                          calendarEvents = record.calendarEvents + (selectedDay to updatedEvs)
                        )
                        currentState.copy(
                          monthlyRecords = currentState.monthlyRecords + (currentState.currentMonth to updatedRecord)
                        )
                      }
                      newEventText = ""
                    }
                  },
                contentAlignment = Alignment.Center
              ) {
                CustomAddIcon(color = Color.White)
              }
            }
          }
        }
      }
    }
  }
}
