package com.example.agenda.data

enum class ColorPalette {
  NAVY_BLUE,
  PURPLE_LAVENDER,
  PINK_ROSE
}

enum class CoverImage {
  PINK_DESK,
  NAVY_BLUE_COVER,
  LAVENDER_COVER,
  ROSE_COVER
}

enum class TaskCategory {
  ACADEMIC,
  HOME,
  PERSONAL
}

enum class TaskPriority {
  ALTA,
  MEDIA,
  BAJA
}

enum class ShoppingCategory {
  DESPENSA,
  CUIDADO_PERSONAL,
  HOGAR,
  MASCOTAS,
  SUSCRIPCIONES
}

enum class ExpenseCategory {
  ALIMENTACION,
  CUIDADO_PERSONAL,
  EDUCACION,
  MASCOTAS,
  SALIDAS
}

enum class MoodType {
  AGOTADO,
  BAJO,
  NORMAL,
  BIEN,
  GENIAL,
  INCREIBLE
}

fun getDefaultDeviceTimezone(): String {
  return try {
    val tz = java.util.TimeZone.getDefault()
    val id = tz.id
    val offset = tz.rawOffset
    val hours = java.util.concurrent.TimeUnit.MILLISECONDS.toHours(offset.toLong())
    val minutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(offset.toLong()) % 60
    val sign = if (offset >= 0) "+" else "-"
    val absHours = Math.abs(hours)
    val absMinutes = Math.abs(minutes)
    val offsetStr = String.format("GMT%s%02d:%02d", sign, absHours, absMinutes)
    "$id ($offsetStr)"
  } catch (e: java.lang.Exception) {
    "Europe/Madrid (UTC+2)"
  }
}

fun getTodayDateString(): String {
  return try {
    java.time.LocalDate.now().toString()
  } catch (e: Exception) {
    "2026-07-22"
  }
}

data class UserProfile(
  val name: String = "Fatima Rentería",
  val email: String = "sofia.garcia@email.com",
  val avatarUrl: String? = null,
  val language: String = "Español",
  val timezone: String = "America/Mexico_City (GMT-06:00)",
  val dateFormat: String = "DD/MM/AAAA"
)

data class AppearanceConfig(
  val palette: ColorPalette = ColorPalette.NAVY_BLUE,
  val cover: CoverImage = CoverImage.PINK_DESK
)

data class PlannerTask(
  val id: String,
  val title: String,
  val category: TaskCategory,
  val priority: TaskPriority,
  val isCompleted: Boolean = false,
  val date: String // e.g. "2026-07-15"
)

data class ShoppingItem(
  val id: String,
  val name: String,
  val quantity: String,
  val category: ShoppingCategory,
  val isCompleted: Boolean = false
)

data class Expense(
  val id: String,
  val description: String,
  val amount: Double,
  val category: ExpenseCategory,
  val date: String // e.g. "2026-07-15"
)

data class HabitTracker(
  val id: String,
  val name: String,
  val completions: Map<String, Boolean> = emptyMap() // Date ("YYYY-MM-DD") -> completed
)

data class DailyEvent(
  val id: String,
  val time: String, // "07:00"
  val title: String,
  val duration: String, // "30 min"
  val isCompleted: Boolean = false
)

data class MoodCheckIn(
  val date: String, // "2026-07-15"
  val mood: MoodType,
  val energy: String // "Bajo", "Medio", "Alto"
)

data class MonthlyGoals(
  val goalTitle: String = "",
  val goalProgress: Float = 0f,
  val exerciseProgress: Float = 0f,
  val savingsProgress: Float = 0f
)

data class MonthlyRecord(
  val budget: Double = 0.0,
  val savingsGoal: Double = 0.0,
  val savingsAchieved: Double = 0.0,
  val calendarEvents: Map<String, List<String>> = emptyMap()
)

fun defaultMonthlyRecords() = emptyMap<String, MonthlyRecord>()

data class PlannerState(
  val profile: UserProfile = UserProfile(),
  val appearance: AppearanceConfig = AppearanceConfig(),
  val tasks: List<PlannerTask> = initialTasks(),
  val shoppingItems: List<ShoppingItem> = initialShoppingItems(),
  val expenses: List<Expense> = initialExpenses(),
  val habits: List<HabitTracker> = initialHabits(),
  val dailyEvents: List<DailyEvent> = initialDailyEvents(),
  val dailyNotes: String = "",
  val waterGlassesDrunk: Int = 0,
  val moodCheckIns: List<MoodCheckIn> = initialMoodCheckIns(),
  val monthlyGoals: MonthlyGoals = MonthlyGoals(),
  val monthlyBudget: Double = 0.0,
  val savingsGoal: Double = 0.0,
  val savingsAchieved: Double = 0.0,
  val currentMonth: String = "2026-07",
  val monthlyRecords: Map<String, MonthlyRecord> = defaultMonthlyRecords(),
  val personalImages: List<String> = emptyList(),
  val customBannerPath: String? = null,
  val customCoverPath: String? = null,
  val selectedDate: String = getTodayDateString(),
  val dailyEventsMap: Map<String, List<DailyEvent>> = emptyMap(),
  val dailyNotesMap: Map<String, String> = emptyMap(),
  val waterGlassesMap: Map<String, Int> = emptyMap()
)

fun initialTasks() = emptyList<PlannerTask>()

fun initialShoppingItems() = emptyList<ShoppingItem>()

fun initialExpenses() = emptyList<Expense>()

fun initialHabits() = emptyList<HabitTracker>()

fun initialDailyEvents() = emptyList<DailyEvent>()

fun initialMoodCheckIns() = emptyList<MoodCheckIn>()
