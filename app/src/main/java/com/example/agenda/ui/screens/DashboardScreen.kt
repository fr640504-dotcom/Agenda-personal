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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import java.io.File
import java.io.FileOutputStream
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.LazyRow
import com.example.agenda.data.*
import com.example.agenda.theme.*
import com.example.agenda.ui.components.*

@Composable
fun DashboardScreen(
  state: PlannerState,
  onUpdateState: ((PlannerState) -> PlannerState) -> Unit,
  modifier: Modifier = Modifier
) {
  val subscriptionsSum = remember(state.shoppingItems) {
    val subs = state.shoppingItems.filter { it.category == ShoppingCategory.SUSCRIPCIONES }
    var total = 0.0
    for (sub in subs) {
      val cleanQty = sub.quantity
        .replace("$", "")
        .replace("/mes", "")
        .replace("€", "")
        .trim()
      val price = cleanQty.toDoubleOrNull() ?: 0.0
      total += price
    }
    total
  }

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

  val latestMoodText = remember(state.moodCheckIns) {
    val last = state.moodCheckIns.lastOrNull()
    if (last == null) "Sin registrar" else {
      val emoji = when (last.mood) {
        MoodType.INCREIBLE -> "🤩"
        MoodType.GENIAL -> "😊"
        MoodType.BIEN -> "🙂"
        MoodType.NORMAL -> "😐"
        MoodType.BAJO -> "🙁"
        MoodType.AGOTADO -> "😴"
      }
      "${last.mood.name.lowercase().replaceFirstChar { it.uppercase() }} $emoji"
    }
  }

  ScreenFrame(
    cover = state.appearance.cover,
    title = "Buenos días, ${state.profile.name} ✦",
    subtitle = "Aquí tienes tu resumen de hoy — todo en un vistazo.",
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
        .fillMaxSize()
        .weight(1f),
      horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // LEFT COLUMN: Monthly Calendar & Priorities
      Column(
        modifier = Modifier
          .weight(1.1f)
          .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // Compact Monthly Calendar Card
        PlannerCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Planificador Mensual",
              fontFamily = TitleFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = LocalCustomColors.current.text
            )
            Text(
              text = "Julio de 2026",
              style = Typography.bodyMedium.copy(color = GrayText),
              modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // Days of the Week headers
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
              listOf("LU", "MA", "MI", "JU", "VI", "SÁ", "DO").forEach { day ->
                Text(
                  text = day,
                  fontFamily = DataFontFamily,
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = GrayText,
                  modifier = Modifier.width(28.dp),
                  textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
              }
            }
            Spacer(modifier = Modifier.height(6.dp))
            
            // Calendar Grid: July 2026 starting on Wednesday (1st)
            val days = listOf(
              "", "", "1", "2", "3", "4", "5",
              "6", "7", "8", "9", "10", "11", "12",
              "13", "14", "15", "16", "17", "18", "19",
              "20", "21", "22", "23", "24", "25", "26",
              "27", "28", "29", "30", "31", "", ""
            )
            
            val todayDayOfMonth = remember {
              try {
                java.time.LocalDate.now().dayOfMonth.toString()
              } catch (e: Exception) {
                "15"
              }
            }
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
              for (week in 0 until 5) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                  for (dayIndex in 0 until 7) {
                    val dayStr = days.getOrNull(week * 7 + dayIndex) ?: ""
                    val isToday = dayStr == todayDayOfMonth
                    
                    Box(
                      modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (isToday) LocalCustomColors.current.primary else Color.Transparent),
                      contentAlignment = Alignment.Center
                    ) {
                      if (dayStr.isNotEmpty()) {
                        Text(
                          text = dayStr,
                          fontFamily = DataFontFamily,
                          fontSize = 12.sp,
                          fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                          color = if (isToday) Color.White else LocalCustomColors.current.text
                        )
                      }
                    }
                  }
                }
              }
            }
          }
        }
        
        // Today's Priorities Card
        PlannerCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Prioridades de Hoy",
              fontFamily = TitleFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = LocalCustomColors.current.text,
              modifier = Modifier.padding(bottom = 12.dp)
            )
            
            val priorities = remember(state.tasks) {
              state.tasks.filter { it.priority == TaskPriority.ALTA }.take(4)
            }
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              items(priorities) { task ->
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier.fillMaxWidth()
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
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  
                  // Category Badge
                  val badgeColor = when (task.category) {
                    TaskCategory.ACADEMIC -> UrgenciaBaja.copy(alpha = 0.8f)
                    TaskCategory.HOME -> UrgenciaMedia.copy(alpha = 0.8f)
                    TaskCategory.PERSONAL -> UrgenciaAlta.copy(alpha = 0.8f)
                  }
                  Box(
                    modifier = Modifier
                      .clip(RoundedCornerShape(6.dp))
                      .background(badgeColor)
                      .padding(horizontal = 6.dp, vertical = 2.dp)
                  ) {
                    Text(
                      text = task.category.name.lowercase().capitalize(),
                      fontFamily = BodyFontFamily,
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color.White
                    )
                  }
                }
              }
            }
          }
        }
      }
      
      // RIGHT COLUMN: Timeline & Finances
      Column(
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // Today's Timeline Card ("Hoy")
        PlannerCard(modifier = Modifier.fillMaxWidth().weight(1.1f)) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Hoy",
              fontFamily = TitleFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = LocalCustomColors.current.text,
              modifier = Modifier.padding(bottom = 12.dp)
            )
            
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
              items(state.dailyEvents.take(4)) { event ->
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                      if (event.isCompleted) LocalCustomColors.current.lightHighlight.copy(alpha = 0.4f)
                      else LocalCustomColors.current.background.copy(alpha = 0.5f)
                    )
                    .padding(8.dp)
                ) {
                  Text(
                    text = event.time,
                    fontFamily = DataFontFamily,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = LocalCustomColors.current.primary,
                    modifier = Modifier.width(48.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = event.title,
                    style = Typography.bodyMedium.copy(
                      color = if (event.isCompleted) GrayText else LocalCustomColors.current.text,
                      textDecoration = if (event.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
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
                }
              }
            }
          }
        }
        
        // Month Finances Card
        PlannerCard(modifier = Modifier.fillMaxWidth().weight(0.9f)) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "Finanzas del Mes",
              fontFamily = TitleFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = LocalCustomColors.current.text,
              modifier = Modifier.padding(bottom = 12.dp)
            )
            
            val currentRecord = state.monthlyRecords[state.currentMonth] ?: MonthlyRecord()
            val budget = currentRecord.budget
            val savingsGoal = currentRecord.savingsGoal
            val savingsAchieved = currentRecord.savingsAchieved

            val currentExpenses = remember(state.expenses, state.currentMonth) {
              state.expenses.filter { it.date.startsWith(state.currentMonth) }
            }

            val totalSpent = remember(currentExpenses) {
              currentExpenses.sumOf { it.amount }
            }
            val spentPercentage = if (budget > 0) (totalSpent / budget).coerceIn(0.0, 1.0) else 0.0
            
            // Progress Bar
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "Gastado",
                style = Typography.bodyMedium,
                color = LocalCustomColors.current.text,
                modifier = Modifier.weight(1f)
              )
              Text(
                text = "$${String.format("%.2f", totalSpent)} / $${budget.toInt()}",
                fontFamily = DataFontFamily,
                fontSize = 13.sp,
                color = LocalCustomColors.current.text
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
              progress = spentPercentage.toFloat(),
              color = LocalCustomColors.current.primary,
              trackColor = LocalCustomColors.current.activeBorder.copy(alpha = 0.3f),
              modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Statistics values
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
              Column {
                Text(text = "Ahorro Meta", style = Typography.bodyMedium.copy(color = GrayText))
                Text(
                  text = "$${savingsAchieved.toInt()} / $${savingsGoal.toInt()}",
                  fontFamily = DataFontFamily,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = LocalCustomColors.current.text
                )
              }
              
              Column(horizontalAlignment = Alignment.End) {
                Text(text = "Suscripciones", style = Typography.bodyMedium.copy(color = GrayText))
                Text(
                  text = String.format(java.util.Locale.US, "$%.2f/mes", subscriptionsSum),
                  fontFamily = DataFontFamily,
                  fontSize = 14.sp,
                  fontWeight = FontWeight.Bold,
                  color = LocalCustomColors.current.text
                )
              }
            }
          }
        }
      }
    }
    
    Spacer(modifier = Modifier.height(16.dp))
    
    // BOTTOM SECTION: Habit Tracker Summary
    PlannerCard(modifier = Modifier.fillMaxWidth()) {
      Column(modifier = Modifier.padding(16.dp)) {
        Text(
          text = "Hábitos & Bienestar",
          fontFamily = TitleFontFamily,
          fontWeight = FontWeight.Bold,
          fontSize = 16.sp,
          color = LocalCustomColors.current.text,
          modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Habits mini grid
          Column(modifier = Modifier.weight(1.5f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            state.habits.take(3).forEach { habit ->
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
              ) {
                Text(
                  text = habit.name,
                  style = Typography.bodyMedium,
                  modifier = Modifier.width(100.dp),
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.width(8.dp))
                // Last 7 days check box squares
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                  val dates = listOf("2026-07-09", "2026-07-10", "2026-07-11", "2026-07-12", "2026-07-13", "2026-07-14", "2026-07-15")
                  dates.forEach { date ->
                    val isChecked = habit.completions[date] == true
                    Box(
                      modifier = Modifier
                        .size(16.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(
                          if (isChecked) LocalCustomColors.current.primary
                          else LocalCustomColors.current.activeBorder.copy(alpha = 0.3f)
                        )
                    )
                  }
                }
              }
            }
          }
          
          Spacer(modifier = Modifier.width(24.dp))
          
          // Mood/Energy summary stats
          Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text(text = "Racha Activa", style = Typography.bodyMedium.copy(color = GrayText))
              Text(
                text = "$activeStreak días 🔥",
                fontFamily = DataFontFamily,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = LocalCustomColors.current.text
              )
            }
            
            Column(horizontalAlignment = Alignment.End) {
              Text(text = "Estado de ánimo", style = Typography.bodyMedium.copy(color = GrayText))
              Text(
                text = latestMoodText,
                fontFamily = BodyFontFamily,
                fontSize = 16.sp,
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
