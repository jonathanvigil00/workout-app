package com.jvigil.hoofmode.ui.navigation

/**
 * All navigation destinations. Bottom-nav destinations are listed in [bottomNavScreens];
 * everything else is pushed on top of them.
 */
sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object History : Screen("history")
    data object BodyWeight : Screen("body_weight")
    data object Photos : Screen("photos")
    data object Charts : Screen("charts")
    data object Settings : Screen("settings")

    data object ActiveSession : Screen("session/{sessionId}") {
        const val ARG_SESSION_ID = "sessionId"
        fun route(sessionId: Long) = "session/$sessionId"
    }

    data object SessionDetail : Screen("session_detail/{sessionId}") {
        const val ARG_SESSION_ID = "sessionId"
        fun route(sessionId: Long) = "session_detail/$sessionId"
    }

    data object ExerciseLibrary : Screen("exercise_library")

    data object WorkoutList : Screen("workout_list")

    data object WorkoutBuilder : Screen("workout_builder?templateId={templateId}") {
        const val ARG_TEMPLATE_ID = "templateId"
        fun routeForNew() = "workout_builder"
        fun routeForEdit(templateId: Long) = "workout_builder?templateId=$templateId"
    }

    data object ScheduleList : Screen("schedule_list")

    data object ScheduleBuilder : Screen("schedule_builder/{scheduleId}") {
        const val ARG_SCHEDULE_ID = "scheduleId"
        fun route(scheduleId: Long) = "schedule_builder/$scheduleId"
    }

    data object PrHistory : Screen("pr_history")
}

val bottomNavScreens = listOf(
    Screen.Home,
    Screen.History,
    Screen.BodyWeight,
    Screen.Photos,
    Screen.Charts,
)
