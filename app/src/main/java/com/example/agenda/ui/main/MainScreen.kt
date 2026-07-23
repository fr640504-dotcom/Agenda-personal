package com.example.agenda.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation3.runtime.NavKey
import com.example.agenda.data.PlannerRepository
import com.example.agenda.theme.AgendaTheme
import com.example.agenda.ui.components.SidebarNavigation
import com.example.agenda.ui.screens.*

@Composable
fun MainScreen(
  repository: PlannerRepository,
  onItemClick: (NavKey) -> Unit,
  modifier: Modifier = Modifier,
  viewModel: MainScreenViewModel = viewModel { MainScreenViewModel(repository) },
) {
  val plannerState by viewModel.state.collectAsStateWithLifecycle()
  
  AgendaTheme(palette = plannerState.appearance.palette) {
    Row(modifier = Modifier.fillMaxSize()) {
      // PERSISTENT SIDEBAR NAVIGATION
      SidebarNavigation(
        currentTab = viewModel.currentTab,
        onTabSelected = { viewModel.selectTab(it) },
        profile = plannerState.profile
      )

      // INDEPENDENTLY SCROLLING SCREEN WRAPPER
      Box(
        modifier = Modifier
          .weight(1f)
          .fillMaxHeight()
      ) {
        when (viewModel.currentTab) {
          "Dashboard" -> DashboardScreen(
            state = plannerState,
            onUpdateState = { viewModel.updateState(it) },
            onNavigateToTab = { viewModel.selectTab(it) }
          )
          "Monthly" -> MonthlyPlannerScreen(
            state = plannerState,
            onUpdateState = { viewModel.updateState(it) },
            onNavigateToTab = { viewModel.selectTab(it) }
          )
          "Daily" -> DailyPlannerScreen(
            state = plannerState,
            onUpdateState = { viewModel.updateState(it) },
            onNavigateToTab = { viewModel.selectTab(it) }
          )
          "Tasks" -> TasksScreen(
            state = plannerState,
            onUpdateState = { viewModel.updateState(it) }
          )
          "Shopping" -> ShoppingScreen(
            state = plannerState,
            onUpdateState = { viewModel.updateState(it) }
          )
          "Finances" -> FinancesScreen(
            state = plannerState,
            onUpdateState = { viewModel.updateState(it) }
          )
          "Habits" -> HabitsScreen(
            state = plannerState,
            onUpdateState = { viewModel.updateState(it) }
          )
          "Appearance" -> AppearanceScreen(
            state = plannerState,
            onUpdateState = { viewModel.updateState(it) }
          )
        }
      }
    }
  }
}
