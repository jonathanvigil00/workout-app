package com.jvigil.hoofmode.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Photo
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jvigil.hoofmode.ui.bodyweight.BodyWeightScreen
import com.jvigil.hoofmode.ui.charts.ChartsScreen
import com.jvigil.hoofmode.ui.exerciselibrary.ExerciseLibraryScreen
import com.jvigil.hoofmode.ui.history.HistoryScreen
import com.jvigil.hoofmode.ui.home.HomeScreen
import com.jvigil.hoofmode.ui.photos.PhotosScreen
import com.jvigil.hoofmode.ui.prhistory.PrHistoryScreen
import com.jvigil.hoofmode.ui.schedulebuilder.ScheduleBuilderScreen
import com.jvigil.hoofmode.ui.schedulebuilder.ScheduleListScreen
import com.jvigil.hoofmode.ui.session.ActiveSessionScreen
import com.jvigil.hoofmode.ui.sessiondetail.SessionDetailScreen
import com.jvigil.hoofmode.ui.settings.SettingsScreen
import com.jvigil.hoofmode.ui.workoutbuilder.WorkoutBuilderScreen
import com.jvigil.hoofmode.ui.workoutbuilder.WorkoutListScreen

@Composable
fun HoofNavHost(navController: NavHostController = rememberNavController()) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val showBottomBar = backStackEntry?.destination?.route in bottomNavScreens.map { it.route }

    Scaffold(
        bottomBar = { if (showBottomBar) HoofBottomBar(navController) },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = androidx.compose.ui.Modifier.padding(innerPadding),
        ) {
            composable(Screen.Home.route) { HomeScreen(navController) }
            composable(Screen.History.route) { HistoryScreen(navController) }
            composable(Screen.BodyWeight.route) { BodyWeightScreen() }
            composable(Screen.Photos.route) { PhotosScreen() }
            composable(Screen.Charts.route) { ChartsScreen() }
            composable(Screen.Settings.route) { SettingsScreen(navController) }

            composable(
                Screen.ActiveSession.route,
                arguments = listOf(navArgument(Screen.ActiveSession.ARG_SESSION_ID) { type = NavType.LongType }),
            ) { ActiveSessionScreen(navController) }

            composable(
                Screen.SessionDetail.route,
                arguments = listOf(navArgument(Screen.SessionDetail.ARG_SESSION_ID) { type = NavType.LongType }),
            ) { SessionDetailScreen(navController) }

            composable(Screen.ExerciseLibrary.route) { ExerciseLibraryScreen(navController) }
            composable(Screen.WorkoutList.route) { WorkoutListScreen(navController) }

            composable(
                Screen.WorkoutBuilder.route,
                arguments = listOf(
                    navArgument(Screen.WorkoutBuilder.ARG_TEMPLATE_ID) {
                        type = NavType.StringType
                        nullable = true
                    },
                ),
            ) { WorkoutBuilderScreen(navController) }

            composable(Screen.ScheduleList.route) { ScheduleListScreen(navController) }

            composable(
                Screen.ScheduleBuilder.route,
                arguments = listOf(navArgument(Screen.ScheduleBuilder.ARG_SCHEDULE_ID) { type = NavType.LongType }),
            ) { ScheduleBuilderScreen(navController) }

            composable(Screen.PrHistory.route) { PrHistoryScreen(navController) }
        }
    }
}

private data class BottomNavItem(val screen: Screen, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val bottomNavItems = listOf(
    BottomNavItem(Screen.Home, "Today", Icons.Filled.FitnessCenter),
    BottomNavItem(Screen.History, "History", Icons.Filled.CalendarMonth),
    BottomNavItem(Screen.BodyWeight, "Weight", Icons.Filled.MonitorWeight),
    BottomNavItem(Screen.Photos, "Photos", Icons.Filled.Photo),
    BottomNavItem(Screen.Charts, "Charts", Icons.AutoMirrored.Filled.ShowChart),
)

@Composable
private fun HoofBottomBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    NavigationBar {
        bottomNavItems.forEach { item ->
            NavigationBarItem(
                selected = currentRoute == item.screen.route,
                onClick = {
                    navController.navigate(item.screen.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
            )
        }
    }
}
