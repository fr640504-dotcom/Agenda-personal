package com.example.agenda.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

interface PlannerRepository {
  val state: StateFlow<PlannerState>
  fun updateState(updater: (PlannerState) -> PlannerState)
}

class DefaultPlannerRepository(private val context: Context) : PlannerRepository {
  private val file = File(context.filesDir, "planner_state_config.txt")
  private val _state = MutableStateFlow(loadState())

  override val state: StateFlow<PlannerState> = _state.asStateFlow()

  private val ioScope = CoroutineScope(Dispatchers.IO)

  private fun loadState(): PlannerState {
    var state = PlannerState()
    try {
      if (file.exists()) {
        val lines = file.readLines()
        val data = mutableMapOf<String, String>()
        for (line in lines) {
          if (!line.contains("=")) continue
          val parts = line.split("=", limit = 2)
          data[parts[0].trim()] = parts[1].trim()
        }

        val name = data["profile.name"] ?: state.profile.name
        val email = data["profile.email"] ?: state.profile.email
        val language = data["profile.language"] ?: state.profile.language
        val timezone = data["profile.timezone"] ?: state.profile.timezone
        val dateFormat = data["profile.dateFormat"] ?: state.profile.dateFormat
        
        var palette = state.appearance.palette
        data["appearance.palette"]?.let {
          try { palette = ColorPalette.valueOf(it) } catch (e: Exception) {}
        }
        
        var cover = state.appearance.cover
        data["appearance.cover"]?.let {
          try { cover = CoverImage.valueOf(it) } catch (e: Exception) {}
        }

        val water = data["water"]?.toIntOrNull() ?: state.waterGlassesDrunk
        val notes = (data["notes"] ?: state.dailyNotes).replace("\\n", "\n")
        val customBannerPath = data["customBannerPath"]?.let { if (it == "null") null else it }
        val customCoverPath = data["customCoverPath"]?.let { if (it == "null") null else it }

        var goalTitle = state.monthlyGoals.goalTitle
        data["monthlyGoals.goalTitle"]?.let { goalTitle = it.replace("\\n", "\n") }
        val goalProgress = data["monthlyGoals.goalProgress"]?.toFloatOrNull() ?: state.monthlyGoals.goalProgress
        val exerciseProgress = data["monthlyGoals.exerciseProgress"]?.toFloatOrNull() ?: state.monthlyGoals.exerciseProgress
        val savingsProgress = data["monthlyGoals.savingsProgress"]?.toFloatOrNull() ?: state.monthlyGoals.savingsProgress

        val monthlyBudget = data["monthlyBudget"]?.toDoubleOrNull() ?: state.monthlyBudget
        val savingsGoal = data["savingsGoal"]?.toDoubleOrNull() ?: state.savingsGoal
        val savingsAchieved = data["savingsAchieved"]?.toDoubleOrNull() ?: state.savingsAchieved
        val currentMonth = data["currentMonth"] ?: state.currentMonth

        // Parse tasks
        val tasks = mutableListOf<PlannerTask>()
        val tasksSize = data["tasks.size"]?.toIntOrNull() ?: 0
        for (i in 0 until tasksSize) {
          val id = data["tasks.$i.id"] ?: continue
          val title = (data["tasks.$i.title"] ?: "").replace("\\n", "\n")
          val catStr = data["tasks.$i.category"] ?: TaskCategory.PERSONAL.name
          val priStr = data["tasks.$i.priority"] ?: TaskPriority.BAJA.name
          val isComp = data["tasks.$i.isCompleted"]?.toBoolean() ?: false
          val tDate = data["tasks.$i.date"] ?: ""
          
          val category = try { TaskCategory.valueOf(catStr) } catch(e: Exception) { TaskCategory.PERSONAL }
          val priority = try { TaskPriority.valueOf(priStr) } catch(e: Exception) { TaskPriority.BAJA }
          tasks.add(PlannerTask(id, title, category, priority, isComp, tDate))
        }

        // Parse shoppingItems
        val shoppingItems = mutableListOf<ShoppingItem>()
        val shoppingSize = data["shopping.size"]?.toIntOrNull() ?: 0
        for (i in 0 until shoppingSize) {
          val id = data["shopping.$i.id"] ?: continue
          val sName = (data["shopping.$i.name"] ?: "").replace("\\n", "\n")
          val qty = (data["shopping.$i.quantity"] ?: "").replace("\\n", "\n")
          val catStr = data["shopping.$i.category"] ?: ShoppingCategory.DESPENSA.name
          val isComp = data["shopping.$i.isCompleted"]?.toBoolean() ?: false
          
          val category = try { ShoppingCategory.valueOf(catStr) } catch(e: Exception) { ShoppingCategory.DESPENSA }
          shoppingItems.add(ShoppingItem(id, sName, qty, category, isComp))
        }

        // Parse expenses
        val expenses = mutableListOf<Expense>()
        val expensesSize = data["expenses.size"]?.toIntOrNull() ?: 0
        for (i in 0 until expensesSize) {
          val id = data["expenses.$i.id"] ?: continue
          val desc = (data["expenses.$i.description"] ?: "").replace("\\n", "\n")
          val amt = data["expenses.$i.amount"]?.toDoubleOrNull() ?: 0.0
          val catStr = data["expenses.$i.category"] ?: ExpenseCategory.SALIDAS.name
          val eDate = data["expenses.$i.date"] ?: ""
          
          val category = try { ExpenseCategory.valueOf(catStr) } catch(e: Exception) { ExpenseCategory.SALIDAS }
          expenses.add(Expense(id, desc, amt, category, eDate))
        }

        // Parse habits
        val habits = mutableListOf<HabitTracker>()
        val habitsSize = data["habits.size"]?.toIntOrNull() ?: 0
        for (i in 0 until habitsSize) {
          val id = data["habits.$i.id"] ?: continue
          val hName = (data["habits.$i.name"] ?: "").replace("\\n", "\n")
          val completionsStr = data["habits.$i.completions"] ?: ""
          val completions = mutableMapOf<String, Boolean>()
          if (completionsStr.isNotEmpty()) {
            val parts = completionsStr.split(",")
            for (p in parts) {
              val kv = p.split("=")
              if (kv.size == 2) {
                completions[kv[0]] = kv[1].toBoolean()
              }
            }
          }
          habits.add(HabitTracker(id, hName, completions))
        }

        // Parse dailyEvents
        val dailyEvents = mutableListOf<DailyEvent>()
        val dailyEventsSize = data["dailyEvents.size"]?.toIntOrNull() ?: 0
        for (i in 0 until dailyEventsSize) {
          val id = data["dailyEvents.$i.id"] ?: continue
          val time = data["dailyEvents.$i.time"] ?: ""
          val eTitle = (data["dailyEvents.$i.title"] ?: "").replace("\\n", "\n")
          val dur = data["dailyEvents.$i.duration"] ?: ""
          val isComp = data["dailyEvents.$i.isCompleted"]?.toBoolean() ?: false
          dailyEvents.add(DailyEvent(id, time, eTitle, dur, isComp))
        }

        // Parse moodCheckIns
        val moodCheckIns = mutableListOf<MoodCheckIn>()
        val moodCheckInsSize = data["moodCheckIns.size"]?.toIntOrNull() ?: 0
        for (i in 0 until moodCheckInsSize) {
          val mDate = data["moodCheckIns.$i.date"] ?: continue
          val moodStr = data["moodCheckIns.$i.mood"] ?: MoodType.NORMAL.name
          val nrg = data["moodCheckIns.$i.energy"] ?: "Medio"
          
          val mood = try { MoodType.valueOf(moodStr) } catch(e: Exception) { MoodType.NORMAL }
          moodCheckIns.add(MoodCheckIn(mDate, mood, nrg))
        }

        // Parse monthlyRecords Map
        val monthlyRecords = mutableMapOf<String, MonthlyRecord>()
        val keysStr = data["monthlyRecords.keys"] ?: ""
        if (keysStr.isNotEmpty()) {
          val keys = keysStr.split(",")
          for (k in keys) {
            val budget = data["monthlyRecord.$k.budget"]?.toDoubleOrNull() ?: 0.0
            val sGoal = data["monthlyRecord.$k.savingsGoal"]?.toDoubleOrNull() ?: 0.0
            val sAch = data["monthlyRecord.$k.savingsAchieved"]?.toDoubleOrNull() ?: 0.0
            
            val calendarEvents = mutableMapOf<String, List<String>>()
            val calKeysStr = data["monthlyRecord.$k.calendarEvents.keys"] ?: ""
            if (calKeysStr.isNotEmpty()) {
              val calKeys = calKeysStr.split(",")
              for (day in calKeys) {
                val daySize = data["monthlyRecord.$k.calendarEvents.day.$day.size"]?.toIntOrNull() ?: 0
                val dayEventsList = mutableListOf<String>()
                for (j in 0 until daySize) {
                  val eventText = data["monthlyRecord.$k.calendarEvents.day.$day.$j"] ?: ""
                  dayEventsList.add(eventText.replace("\\n", "\n"))
                }
                calendarEvents[day] = dayEventsList
              }
            }
            monthlyRecords[k] = MonthlyRecord(budget, sGoal, sAch, calendarEvents)
          }
        }

        state = state.copy(
          profile = UserProfile(name, email, null, language, timezone, dateFormat),
          appearance = AppearanceConfig(palette, cover),
          waterGlassesDrunk = water,
          dailyNotes = notes,
          customBannerPath = customBannerPath,
          customCoverPath = customCoverPath,
          monthlyGoals = MonthlyGoals(goalTitle, goalProgress, exerciseProgress, savingsProgress),
          monthlyBudget = monthlyBudget,
          savingsGoal = savingsGoal,
          savingsAchieved = savingsAchieved,
          currentMonth = currentMonth,
          tasks = if (data.containsKey("tasks.size")) tasks else state.tasks,
          shoppingItems = if (data.containsKey("shopping.size")) shoppingItems else state.shoppingItems,
          expenses = if (data.containsKey("expenses.size")) expenses else state.expenses,
          habits = if (data.containsKey("habits.size")) habits else state.habits,
          dailyEvents = if (data.containsKey("dailyEvents.size")) dailyEvents else state.dailyEvents,
          moodCheckIns = if (data.containsKey("moodCheckIns.size")) moodCheckIns else state.moodCheckIns,
          monthlyRecords = if (data.containsKey("monthlyRecords.keys")) monthlyRecords else state.monthlyRecords
        )
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
    return state
  }

  override fun updateState(updater: (PlannerState) -> PlannerState) {
    val currentState = _state.value
    val newState = updater(currentState)
    _state.value = newState
    saveState(newState)
  }

  private fun saveState(state: PlannerState) {
    ioScope.launch {
      try {
        val builder = StringBuilder()
        builder.append("profile.name=").append(state.profile.name).append("\n")
        builder.append("profile.email=").append(state.profile.email).append("\n")
        builder.append("profile.language=").append(state.profile.language).append("\n")
        builder.append("profile.timezone=").append(state.profile.timezone).append("\n")
        builder.append("profile.dateFormat=").append(state.profile.dateFormat).append("\n")
        builder.append("appearance.palette=").append(state.appearance.palette.name).append("\n")
        builder.append("appearance.cover=").append(state.appearance.cover.name).append("\n")
        builder.append("water=").append(state.waterGlassesDrunk).append("\n")
        builder.append("notes=").append(state.dailyNotes.replace("\n", "\\n")).append("\n")
        builder.append("customBannerPath=").append(state.customBannerPath ?: "null").append("\n")
        builder.append("customCoverPath=").append(state.customCoverPath ?: "null").append("\n")
        
        builder.append("monthlyGoals.goalTitle=").append(state.monthlyGoals.goalTitle.replace("\n", "\\n")).append("\n")
        builder.append("monthlyGoals.goalProgress=").append(state.monthlyGoals.goalProgress).append("\n")
        builder.append("monthlyGoals.exerciseProgress=").append(state.monthlyGoals.exerciseProgress).append("\n")
        builder.append("monthlyGoals.savingsProgress=").append(state.monthlyGoals.savingsProgress).append("\n")
        
        builder.append("monthlyBudget=").append(state.monthlyBudget).append("\n")
        builder.append("savingsGoal=").append(state.savingsGoal).append("\n")
        builder.append("savingsAchieved=").append(state.savingsAchieved).append("\n")
        builder.append("currentMonth=").append(state.currentMonth).append("\n")

        // Serialize tasks
        builder.append("tasks.size=").append(state.tasks.size).append("\n")
        state.tasks.forEachIndexed { i, t ->
          builder.append("tasks.$i.id=").append(t.id).append("\n")
          builder.append("tasks.$i.title=").append(t.title.replace("\n", "\\n")).append("\n")
          builder.append("tasks.$i.category=").append(t.category.name).append("\n")
          builder.append("tasks.$i.priority=").append(t.priority.name).append("\n")
          builder.append("tasks.$i.isCompleted=").append(t.isCompleted).append("\n")
          builder.append("tasks.$i.date=").append(t.date).append("\n")
        }

        // Serialize shoppingItems
        builder.append("shopping.size=").append(state.shoppingItems.size).append("\n")
        state.shoppingItems.forEachIndexed { i, s ->
          builder.append("shopping.$i.id=").append(s.id).append("\n")
          builder.append("shopping.$i.name=").append(s.name.replace("\n", "\\n")).append("\n")
          builder.append("shopping.$i.quantity=").append(s.quantity.replace("\n", "\\n")).append("\n")
          builder.append("shopping.$i.category=").append(s.category.name).append("\n")
          builder.append("shopping.$i.isCompleted=").append(s.isCompleted).append("\n")
        }

        // Serialize expenses
        builder.append("expenses.size=").append(state.expenses.size).append("\n")
        state.expenses.forEachIndexed { i, e ->
          builder.append("expenses.$i.id=").append(e.id).append("\n")
          builder.append("expenses.$i.description=").append(e.description.replace("\n", "\\n")).append("\n")
          builder.append("expenses.$i.amount=").append(e.amount).append("\n")
          builder.append("expenses.$i.category=").append(e.category.name).append("\n")
          builder.append("expenses.$i.date=").append(e.date).append("\n")
        }

        // Serialize habits
        builder.append("habits.size=").append(state.habits.size).append("\n")
        state.habits.forEachIndexed { i, h ->
          builder.append("habits.$i.id=").append(h.id).append("\n")
          builder.append("habits.$i.name=").append(h.name.replace("\n", "\\n")).append("\n")
          val completionsJoin = h.completions.entries.joinToString(",") { "${it.key}=${it.value}" }
          builder.append("habits.$i.completions=").append(completionsJoin).append("\n")
        }

        // Serialize dailyEvents
        builder.append("dailyEvents.size=").append(state.dailyEvents.size).append("\n")
        state.dailyEvents.forEachIndexed { i, d ->
          builder.append("dailyEvents.$i.id=").append(d.id).append("\n")
          builder.append("dailyEvents.$i.time=").append(d.time).append("\n")
          builder.append("dailyEvents.$i.title=").append(d.title.replace("\n", "\\n")).append("\n")
          builder.append("dailyEvents.$i.duration=").append(d.duration).append("\n")
          builder.append("dailyEvents.$i.isCompleted=").append(d.isCompleted).append("\n")
        }

        // Serialize moodCheckIns
        builder.append("moodCheckIns.size=").append(state.moodCheckIns.size).append("\n")
        state.moodCheckIns.forEachIndexed { i, m ->
          builder.append("moodCheckIns.$i.date=").append(m.date).append("\n")
          builder.append("moodCheckIns.$i.mood=").append(m.mood.name).append("\n")
          builder.append("moodCheckIns.$i.energy=").append(m.energy).append("\n")
        }

        // Serialize monthlyRecords
        val keysJoin = state.monthlyRecords.keys.joinToString(",")
        builder.append("monthlyRecords.keys=").append(keysJoin).append("\n")
        state.monthlyRecords.forEach { (k, r) ->
          builder.append("monthlyRecord.$k.budget=").append(r.budget).append("\n")
          builder.append("monthlyRecord.$k.savingsGoal=").append(r.savingsGoal).append("\n")
          builder.append("monthlyRecord.$k.savingsAchieved=").append(r.savingsAchieved).append("\n")
          
          val calKeysJoin = r.calendarEvents.keys.joinToString(",")
          builder.append("monthlyRecord.$k.calendarEvents.keys=").append(calKeysJoin).append("\n")
          r.calendarEvents.forEach { (day, list) ->
            builder.append("monthlyRecord.$k.calendarEvents.day.$day.size=").append(list.size).append("\n")
            list.forEachIndexed { idx, txt ->
              builder.append("monthlyRecord.$k.calendarEvents.day.$day.$idx=").append(txt.replace("\n", "\\n")).append("\n")
            }
          }
        }

        file.writeText(builder.toString())
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }
  }
}
