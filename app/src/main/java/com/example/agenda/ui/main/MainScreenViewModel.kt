package com.example.agenda.ui.main

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.agenda.data.PlannerRepository
import com.example.agenda.data.PlannerState
import kotlinx.coroutines.flow.StateFlow

class MainScreenViewModel(private val repository: PlannerRepository) : ViewModel() {
  val state: StateFlow<PlannerState> = repository.state

  var currentTab by mutableStateOf("Dashboard")
    private set

  fun selectTab(tab: String) {
    currentTab = tab
  }

  fun updateState(updater: (PlannerState) -> PlannerState) {
    repository.updateState(updater)
  }
}
