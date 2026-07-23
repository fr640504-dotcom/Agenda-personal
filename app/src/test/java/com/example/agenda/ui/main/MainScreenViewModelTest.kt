package com.example.agenda.ui.main

import com.example.agenda.data.PlannerRepository
import com.example.agenda.data.PlannerState
import junit.framework.TestCase.assertEquals
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test

class MainScreenViewModelTest {
  @Test
  fun uiState_initiallyDashboard() = runTest {
    val repository = FakePlannerRepository()
    val viewModel = MainScreenViewModel(repository)
    assertEquals("Dashboard", viewModel.currentTab)
  }

  @Test
  fun uiState_onSelectTab_changesTab() = runTest {
    val repository = FakePlannerRepository()
    val viewModel = MainScreenViewModel(repository)
    viewModel.selectTab("Monthly")
    assertEquals("Monthly", viewModel.currentTab)
  }
}

private class FakePlannerRepository : PlannerRepository {
  private val _state = MutableStateFlow(PlannerState())
  override val state: StateFlow<PlannerState> = _state.asStateFlow()

  override fun updateState(updater: (PlannerState) -> PlannerState) {
    _state.value = updater(_state.value)
  }
}
