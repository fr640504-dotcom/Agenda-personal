package com.example.agenda.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agenda.data.*
import com.example.agenda.theme.*
import com.example.agenda.ui.components.*
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinancesScreen(
  state: PlannerState,
  onUpdateState: ((PlannerState) -> PlannerState) -> Unit,
  modifier: Modifier = Modifier
) {
  var description by remember { mutableStateOf("") }
  var amountText by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf(ExpenseCategory.ALIMENTACION) }
  var isCategoryDropdownExpanded by remember { mutableStateOf(false) }

  // Resolve current month data record
  val currentRecord = state.monthlyRecords[state.currentMonth] ?: MonthlyRecord()
  val budget = currentRecord.budget
  val savingsGoal = currentRecord.savingsGoal
  val savingsAchieved = currentRecord.savingsAchieved

  var isEditingBudget by remember { mutableStateOf(false) }
  var editedBudgetStr by remember(state.currentMonth) { mutableStateOf(budget.toInt().toString()) }

  // Filter transactions to only those belonging to the currentMonth (e.g. starting with "2026-07")
  val currentExpenses = remember(state.expenses, state.currentMonth) {
    state.expenses.filter { it.date.startsWith(state.currentMonth) }
  }

  val totalSpent = remember(currentExpenses) {
    currentExpenses.sumOf { it.amount }
  }
  
  val budgetProgress = if (budget > 0) (totalSpent / budget).coerceIn(0.0, 1.0) else 0.0
  val availableAmount = budget - totalSpent
  val savingsProgress = if (savingsGoal > 0) (savingsAchieved / savingsGoal).coerceIn(0.0, 1.0) else 0.0

  // Calculations for categories
  val categorySpent = remember(currentExpenses) {
    ExpenseCategory.values().associateWith { cat ->
      currentExpenses.filter { it.category == cat }.sumOf { it.amount }
    }
  }

  // Parse Month Name for subtitle
  val parts = state.currentMonth.split("-")
  val currentYear = parts.getOrNull(0)?.toIntOrNull() ?: 2026
  val currentMonthIndex = (parts.getOrNull(1)?.toIntOrNull() ?: 7) - 1
  val monthName = when (currentMonthIndex) {
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

  ScreenFrame(
    cover = state.appearance.cover,
    title = "Rastreador de Finanzas",
    subtitle = "Control de presupuesto y gastos de $monthName de $currentYear",
    bannerPath = state.customBannerPath,
    coverPath = state.customCoverPath
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f),
      horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // LEFT COLUMN: Metrics, Budget Progress & Category Breakdown
      Column(
        modifier = Modifier
          .weight(1.2f)
          .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // TOP METRICS ROW
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          // Presupuesto
          PlannerCard(modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.padding(16.dp)) {
              Text(text = "PRESUPUESTO MENSUAL", style = Typography.labelSmall.copy(color = GrayText))
              
              if (isEditingBudget) {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp),
                  modifier = Modifier.padding(vertical = 4.dp)
                ) {
                  OutlinedTextField(
                    value = editedBudgetStr,
                    onValueChange = { editedBudgetStr = it },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(110.dp).height(46.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                      focusedBorderColor = LocalCustomColors.current.primary,
                      unfocusedBorderColor = LocalCustomColors.current.activeBorder,
                      focusedContainerColor = Color.White,
                      unfocusedContainerColor = Color.White
                    ),
                    textStyle = Typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                  )
                  
                  Box(
                    modifier = Modifier
                      .size(36.dp)
                      .clip(RoundedCornerShape(8.dp))
                      .background(LocalCustomColors.current.primary)
                      .clickable {
                        val newBudget = editedBudgetStr.toDoubleOrNull()
                        if (newBudget != null && newBudget > 0) {
                          onUpdateState { currentState ->
                            val record = currentState.monthlyRecords[currentState.currentMonth] ?: MonthlyRecord()
                            val updatedRecord = record.copy(budget = newBudget)
                            currentState.copy(
                              monthlyRecords = currentState.monthlyRecords + (currentState.currentMonth to updatedRecord)
                            )
                          }
                          isEditingBudget = false
                        }
                      },
                    contentAlignment = Alignment.Center
                  ) {
                    CustomCheckIcon(color = Color.White, modifier = Modifier.size(16.dp))
                  }
                }
              } else {
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  Text(
                    text = "$${budget.toInt()}",
                    fontFamily = DataFontFamily,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = LocalCustomColors.current.text
                  )
                  Box(
                    modifier = Modifier
                      .size(24.dp)
                      .clip(CircleShape)
                      .background(LocalCustomColors.current.primary.copy(alpha = 0.15f))
                      .clickable {
                        editedBudgetStr = budget.toInt().toString()
                        isEditingBudget = true
                      },
                    contentAlignment = Alignment.Center
                  ) {
                    Text("✏️", fontSize = 11.sp)
                  }
                }
              }
              
              Text(text = "$monthName $currentYear", style = Typography.bodyMedium.copy(color = GrayText))
            }
          }

          // Gastado
          PlannerCard(modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.padding(16.dp)) {
              Text(text = "GASTADO HASTA HOY", style = Typography.labelSmall.copy(color = GrayText))
              Text(
                text = "$${String.format("%.2f", totalSpent)}",
                fontFamily = DataFontFamily,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = LocalCustomColors.current.text
              )
              Text(
                text = "${(budgetProgress * 100).toInt()}% del presupuesto",
                style = Typography.bodyMedium.copy(color = if (budgetProgress > 0.9) UrgenciaAlta else LocalCustomColors.current.primary)
              )
            }
          }

          // Disponible
          PlannerCard(modifier = Modifier.weight(1f)) {
            Column(modifier = Modifier.padding(16.dp)) {
              Text(text = "DISPONIBLE", style = Typography.labelSmall.copy(color = GrayText))
              Text(
                text = "$${String.format("%.2f", availableAmount)}",
                fontFamily = DataFontFamily,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = if (availableAmount < 100) UrgenciaAlta else LocalCustomColors.current.darkAccent
              )
              Text(text = "Mes en curso", style = Typography.bodyMedium.copy(color = GrayText))
            }
          }
        }

        // BUDGET PROGRESS CARD
        PlannerCard(modifier = Modifier.fillMaxWidth()) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = "PROGRESO DEL PRESUPUESTO",
              style = Typography.labelSmall.copy(color = GrayText, fontWeight = FontWeight.Bold),
              modifier = Modifier.padding(bottom = 6.dp)
            )
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
              Text(text = "$0", fontFamily = DataFontFamily, fontSize = 11.sp, color = GrayText)
              Text(
                text = "$${totalSpent.toInt()} gastados",
                fontFamily = DataFontFamily,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = LocalCustomColors.current.primary
              )
              Text(text = "$${budget.toInt()}", fontFamily = DataFontFamily, fontSize = 11.sp, color = GrayText)
            }
            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
              progress = budgetProgress.toFloat(),
              color = LocalCustomColors.current.primary,
              trackColor = LocalCustomColors.current.activeBorder.copy(alpha = 0.2f),
              modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
            )
          }
        }

        // CATEGORY BREAKDOWN CARD
        PlannerCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(16.dp)
          ) {
            Text(
              text = "GASTOS POR CATEGORÍA",
              style = Typography.labelSmall.copy(color = GrayText, fontWeight = FontWeight.Bold),
              modifier = Modifier.padding(bottom = 12.dp)
            )
            
            Column(
              verticalArrangement = Arrangement.SpaceBetween,
              modifier = Modifier.weight(1f).fillMaxWidth()
            ) {
              ExpenseCategory.values().forEach { category ->
                val amt = categorySpent[category] ?: 0.0
                val percent = if (totalSpent > 0) (amt / totalSpent).toFloat() else 0f
                
                Column(modifier = Modifier.fillMaxWidth()) {
                  Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                  ) {
                    val displayName = when(category) {
                      ExpenseCategory.ALIMENTACION -> "Alimentación"
                      ExpenseCategory.CUIDADO_PERSONAL -> "Cuidado personal"
                      ExpenseCategory.EDUCACION -> "Educación"
                      ExpenseCategory.MASCOTAS -> "Mascotas"
                      ExpenseCategory.SALIDAS -> "Salidas"
                    }
                    Text(
                      text = "● $displayName",
                      style = Typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                      color = when (category) {
                        ExpenseCategory.ALIMENTACION -> SageGreenPrimary
                        ExpenseCategory.CUIDADO_PERSONAL -> PastelBlushPrimary
                        ExpenseCategory.EDUCACION -> NavyBluePrimary
                        ExpenseCategory.MASCOTAS -> LocalCustomColors.current.darkAccent
                        ExpenseCategory.SALIDAS -> UrgenciaAlta
                      }
                    )
                    Text(
                      text = "$${String.format("%.2f", amt)}",
                      fontFamily = DataFontFamily,
                      fontSize = 13.sp,
                      fontWeight = FontWeight.Bold,
                      color = LocalCustomColors.current.text
                    )
                  }
                  Spacer(modifier = Modifier.height(4.dp))
                  LinearProgressIndicator(
                    progress = percent,
                    color = when (category) {
                      ExpenseCategory.ALIMENTACION -> SageGreenPrimary
                      ExpenseCategory.CUIDADO_PERSONAL -> PastelBlushPrimary
                      ExpenseCategory.EDUCACION -> NavyBluePrimary
                      ExpenseCategory.MASCOTAS -> LocalCustomColors.current.darkAccent
                      ExpenseCategory.SALIDAS -> UrgenciaAlta
                    },
                    trackColor = LocalCustomColors.current.activeBorder.copy(alpha = 0.2f),
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(6.dp)
                      .clip(CircleShape)
                  )
                }
              }
            }
          }
        }
      }

      // RIGHT COLUMN: Add Expense & Transaction History
      Column(
        modifier = Modifier
          .weight(0.8f)
          .fillMaxHeight(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        // ADD EXPENSE CARD
        PlannerCard(modifier = Modifier.fillMaxWidth()) {
          Column(modifier = Modifier.padding(14.dp)) {
            Text(
              text = "AÑADIR GASTO",
              fontFamily = TitleFontFamily,
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = LocalCustomColors.current.text,
              modifier = Modifier.padding(bottom = 10.dp)
            )

            // Description input
            PlannerTextField(
              value = description,
              onValueChange = { description = it },
              placeholder = "Descripción..."
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            // Amount input
            Row(verticalAlignment = Alignment.CenterVertically) {
              PlannerTextField(
                value = amountText,
                onValueChange = { amountText = it },
                placeholder = "Importe ($)...",
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
              )
              
              Spacer(modifier = Modifier.width(8.dp))

              // Category selector dropdown
              Box {
                Box(
                  modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, LocalCustomColors.current.activeBorder, RoundedCornerShape(12.dp))
                    .clickable { isCategoryDropdownExpanded = true }
                    .padding(horizontal = 12.dp, vertical = 12.dp)
                ) {
                  Text(
                    text = when(selectedCategory) {
                      ExpenseCategory.ALIMENTACION -> "Alimentación"
                      ExpenseCategory.CUIDADO_PERSONAL -> "Cuidado personal"
                      ExpenseCategory.EDUCACION -> "Educación"
                      ExpenseCategory.MASCOTAS -> "Mascotas"
                      ExpenseCategory.SALIDAS -> "Salidas"
                    },
                    style = Typography.bodyMedium.copy(color = LocalCustomColors.current.text)
                  )
                }

                DropdownMenu(
                  expanded = isCategoryDropdownExpanded,
                  onDismissRequest = { isCategoryDropdownExpanded = false }
                ) {
                  ExpenseCategory.values().forEach { cat ->
                    val label = when(cat) {
                      ExpenseCategory.ALIMENTACION -> "Alimentación"
                      ExpenseCategory.CUIDADO_PERSONAL -> "Cuidado personal"
                      ExpenseCategory.EDUCACION -> "Educación"
                      ExpenseCategory.MASCOTAS -> "Mascotas"
                      ExpenseCategory.SALIDAS -> "Salidas"
                    }
                    DropdownMenuItem(
                      text = { Text(label) },
                      onClick = {
                        selectedCategory = cat
                        isCategoryDropdownExpanded = false
                      }
                    )
                  }
                }
              }
            }

            Spacer(modifier = Modifier.height(10.dp))

            PlannerButton(
              text = "Registrar gasto",
              onClick = {
                val amt = amountText.toDoubleOrNull()
                if (description.isNotEmpty() && amt != null) {
                  onUpdateState { currentState ->
                    currentState.copy(
                      expenses = listOf(
                        Expense(
                          id = UUID.randomUUID().toString(),
                          description = description,
                          amount = amt,
                          category = selectedCategory,
                          date = "${currentState.currentMonth}-15"
                        )
                      ) + currentState.expenses
                    )
                  }
                  description = ""
                  amountText = ""
                }
              },
              modifier = Modifier.fillMaxWidth()
            )
          }
        }

        // TRANSACTION HISTORY CARD
        PlannerCard(modifier = Modifier.fillMaxWidth().weight(1f)) {
          Column(modifier = Modifier.padding(16.dp)) {
            Text(
              text = "HISTORIAL DE TRANSACCIONES",
              style = Typography.labelSmall.copy(color = GrayText, fontWeight = FontWeight.Bold),
              modifier = Modifier.padding(bottom = 12.dp)
            )
            
            LazyColumn(
              verticalArrangement = Arrangement.spacedBy(10.dp),
              modifier = Modifier.weight(1f)
            ) {
              items(currentExpenses) { expense ->
                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFAF9F6))
                    .padding(10.dp)
                ) {
                  // Category Dot Indicators
                  Box(
                    modifier = Modifier
                      .size(8.dp)
                      .clip(CircleShape)
                      .background(
                        when (expense.category) {
                          ExpenseCategory.ALIMENTACION -> SageGreenPrimary
                          ExpenseCategory.CUIDADO_PERSONAL -> PastelBlushPrimary
                          ExpenseCategory.EDUCACION -> NavyBluePrimary
                          ExpenseCategory.MASCOTAS -> LocalCustomColors.current.darkAccent
                          ExpenseCategory.SALIDAS -> UrgenciaAlta
                        }
                      )
                  )
                  
                  Spacer(modifier = Modifier.width(10.dp))
                  
                  Column(modifier = Modifier.weight(1f)) {
                    Text(
                      text = expense.description,
                      style = Typography.bodyMedium.copy(fontWeight = FontWeight.Medium, color = LocalCustomColors.current.text)
                    )
                    Text(
                      text = expense.date,
                      style = Typography.labelSmall.copy(color = GrayText)
                    )
                  }

                  Text(
                    text = "-$${String.format("%.2f", expense.amount)}",
                    fontFamily = DataFontFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = UrgenciaAlta
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
