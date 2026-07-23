package com.example.agenda.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
fun ShoppingScreen(
  state: PlannerState,
  onUpdateState: ((PlannerState) -> PlannerState) -> Unit,
  modifier: Modifier = Modifier
) {
  var newPantryText by remember { mutableStateOf("") }
  var newPantryQty by remember { mutableStateOf("1") }
  
  var newCareText by remember { mutableStateOf("") }
  var newCareQty by remember { mutableStateOf("1") }
  
  var newHomeText by remember { mutableStateOf("") }
  var newHomeQty by remember { mutableStateOf("1") }

  var newPetsText by remember { mutableStateOf("") }
  var newPetsQty by remember { mutableStateOf("1") }

  var newSubsText by remember { mutableStateOf("") }
  var newSubsQty by remember { mutableStateOf("1") }

  val totalItems = state.shoppingItems.size
  val completedItems = state.shoppingItems.count { it.isCompleted }

  ScreenFrame(
    cover = state.appearance.cover,
    title = "Lista de Compras",
    subtitle = "$completedItems de $totalItems artículos marcados",
    bannerPath = state.customBannerPath,
    coverPath = state.customCoverPath
  ) {
    // Clear completed button at the top right
    Row(
      modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
      horizontalArrangement = Arrangement.End
    ) {
      PlannerButton(
        text = "Limpiar marcados",
        onClick = {
          onUpdateState { currentState ->
            currentState.copy(
              shoppingItems = currentState.shoppingItems.filter { !it.isCompleted }
            )
          }
        },
        containerColor = LocalCustomColors.current.primary.copy(alpha = 0.2f),
        contentColor = LocalCustomColors.current.darkAccent
      )
    }

    Row(
      modifier = Modifier
        .fillMaxWidth()
        .weight(1f)
        .horizontalScroll(rememberScrollState()),
      horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // COLUMN 1: Despensa
      ShoppingColumnWidget(
        title = "Despensa",
        items = state.shoppingItems.filter { it.category == ShoppingCategory.DESPENSA },
        newTextValue = newPantryText,
        onTextChange = { newPantryText = it },
        newQtyValue = newPantryQty,
        onQtyChange = { newPantryQty = it },
        onAddItem = {
          if (newPantryText.isNotEmpty()) {
            onUpdateState { currentState ->
              currentState.copy(
                shoppingItems = currentState.shoppingItems + ShoppingItem(
                  id = UUID.randomUUID().toString(),
                  name = newPantryText,
                  quantity = newPantryQty,
                  category = ShoppingCategory.DESPENSA,
                  isCompleted = false
                )
              )
            }
            newPantryText = ""
            newPantryQty = "1"
          }
        },
        onToggleItem = { itemId, isChecked ->
          onUpdateState { currentState ->
            currentState.copy(
              shoppingItems = currentState.shoppingItems.map {
                if (it.id == itemId) it.copy(isCompleted = isChecked) else it
              }
            )
          }
        },
        modifier = Modifier.width(320.dp)
      )

      // COLUMN 2: Cuidado Personal
      ShoppingColumnWidget(
        title = "Cuidado Personal",
        items = state.shoppingItems.filter { it.category == ShoppingCategory.CUIDADO_PERSONAL },
        newTextValue = newCareText,
        onTextChange = { newCareText = it },
        newQtyValue = newCareQty,
        onQtyChange = { newCareQty = it },
        onAddItem = {
          if (newCareText.isNotEmpty()) {
            onUpdateState { currentState ->
              currentState.copy(
                shoppingItems = currentState.shoppingItems + ShoppingItem(
                  id = UUID.randomUUID().toString(),
                  name = newCareText,
                  quantity = newCareQty,
                  category = ShoppingCategory.CUIDADO_PERSONAL,
                  isCompleted = false
                )
              )
            }
            newCareText = ""
            newCareQty = "1"
          }
        },
        onToggleItem = { itemId, isChecked ->
          onUpdateState { currentState ->
            currentState.copy(
              shoppingItems = currentState.shoppingItems.map {
                if (it.id == itemId) it.copy(isCompleted = isChecked) else it
              }
            )
          }
        },
        modifier = Modifier.width(320.dp)
      )

      // COLUMN 3: Hogar
      ShoppingColumnWidget(
        title = "Hogar",
        items = state.shoppingItems.filter { it.category == ShoppingCategory.HOGAR },
        newTextValue = newHomeText,
        onTextChange = { newHomeText = it },
        newQtyValue = newHomeQty,
        onQtyChange = { newHomeQty = it },
        onAddItem = {
          if (newHomeText.isNotEmpty()) {
            onUpdateState { currentState ->
              currentState.copy(
                shoppingItems = currentState.shoppingItems + ShoppingItem(
                  id = UUID.randomUUID().toString(),
                  name = newHomeText,
                  quantity = newHomeQty,
                  category = ShoppingCategory.HOGAR,
                  isCompleted = false
                )
              )
            }
            newHomeText = ""
            newHomeQty = "1"
          }
        },
        onToggleItem = { itemId, isChecked ->
          onUpdateState { currentState ->
            currentState.copy(
              shoppingItems = currentState.shoppingItems.map {
                if (it.id == itemId) it.copy(isCompleted = isChecked) else it
              }
            )
          }
        },
        modifier = Modifier.width(320.dp)
      )

      // COLUMN 4: Mascotas
      ShoppingColumnWidget(
        title = "Mascotas",
        items = state.shoppingItems.filter { it.category == ShoppingCategory.MASCOTAS },
        newTextValue = newPetsText,
        onTextChange = { newPetsText = it },
        newQtyValue = newPetsQty,
        onQtyChange = { newPetsQty = it },
        onAddItem = {
          if (newPetsText.isNotEmpty()) {
            onUpdateState { currentState ->
              currentState.copy(
                shoppingItems = currentState.shoppingItems + ShoppingItem(
                  id = UUID.randomUUID().toString(),
                  name = newPetsText,
                  quantity = newPetsQty,
                  category = ShoppingCategory.MASCOTAS,
                  isCompleted = false
                )
              )
            }
            newPetsText = ""
            newPetsQty = "1"
          }
        },
        onToggleItem = { itemId, isChecked ->
          onUpdateState { currentState ->
            currentState.copy(
              shoppingItems = currentState.shoppingItems.map {
                if (it.id == itemId) it.copy(isCompleted = isChecked) else it
              }
            )
          }
        },
        modifier = Modifier.width(320.dp)
      )

      // COLUMN 5: Suscripciones
      ShoppingColumnWidget(
        title = "Suscripciones",
        items = state.shoppingItems.filter { it.category == ShoppingCategory.SUSCRIPCIONES },
        newTextValue = newSubsText,
        onTextChange = { newSubsText = it },
        newQtyValue = newSubsQty,
        onQtyChange = { newSubsQty = it },
        onAddItem = {
          if (newSubsText.isNotEmpty()) {
            onUpdateState { currentState ->
              currentState.copy(
                shoppingItems = currentState.shoppingItems + ShoppingItem(
                  id = UUID.randomUUID().toString(),
                  name = newSubsText,
                  quantity = newSubsQty,
                  category = ShoppingCategory.SUSCRIPCIONES,
                  isCompleted = false
                )
              )
            }
            newSubsText = ""
            newSubsQty = "1"
          }
        },
        onToggleItem = { itemId, isChecked ->
          onUpdateState { currentState ->
            currentState.copy(
              shoppingItems = currentState.shoppingItems.map {
                if (it.id == itemId) it.copy(isCompleted = isChecked) else it
              }
            )
          }
        },
        modifier = Modifier.width(320.dp)
      )
    }
  }
}

@Composable
fun ShoppingColumnWidget(
  title: String,
  items: List<ShoppingItem>,
  newTextValue: String,
  onTextChange: (String) -> Unit,
  newQtyValue: String,
  onQtyChange: (String) -> Unit,
  onAddItem: () -> Unit,
  onToggleItem: (String, Boolean) -> Unit,
  modifier: Modifier = Modifier
) {
  val colors = LocalCustomColors.current
  val completedCount = remember(items) { items.count { it.isCompleted } }
  val totalCount = items.size

  PlannerCard(modifier = modifier.fillMaxHeight()) {
    Column(modifier = Modifier.fillMaxSize()) {
      // Header Banner
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
            color = Color.White
          )
          
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(colors.darkAccent)
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = "$completedCount/$totalCount",
              fontFamily = DataFontFamily,
              fontSize = 10.sp,
              color = Color.White,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // List of items
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(12.dp),
        modifier = Modifier.weight(1f)
      ) {
        items(items) { item ->
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
          ) {
            PlannerCheckbox(
              checked = item.isCompleted,
              onCheckedChange = { onToggleItem(item.id, it) }
            )
            
            Spacer(modifier = Modifier.width(10.dp))
            
            Text(
              text = item.name,
              style = Typography.bodyMedium.copy(
                textDecoration = if (item.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                color = if (item.isCompleted) GrayText else colors.text
              ),
              modifier = Modifier.weight(1f),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )

            if (item.quantity.isNotEmpty()) {
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = item.quantity,
                fontFamily = DataFontFamily,
                fontSize = 11.sp,
                color = GrayText,
                fontWeight = FontWeight.Medium
              )
            }
          }
        }
      }

      // Add item fields input
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
          .fillMaxWidth()
          .padding(12.dp)
      ) {
        PlannerTextField(
          value = newTextValue,
          onValueChange = onTextChange,
          placeholder = "Añadir artículo...",
          modifier = Modifier.weight(1.5f)
        )
        
        Spacer(modifier = Modifier.width(4.dp))
        
        // Compact Qty field
        OutlinedTextField(
          value = newQtyValue,
          onValueChange = onQtyChange,
          placeholder = { Text("Cant.", style = Typography.bodyMedium.copy(color = GrayText)) },
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = colors.primary,
            unfocusedBorderColor = colors.activeBorder,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = colors.background.copy(alpha = 0.5f)
          ),
          textStyle = Typography.bodyMedium,
          modifier = Modifier.width(70.dp)
        )

        Spacer(modifier = Modifier.width(6.dp))

        Box(
          modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(colors.primary)
            .clickable { onAddItem() },
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
