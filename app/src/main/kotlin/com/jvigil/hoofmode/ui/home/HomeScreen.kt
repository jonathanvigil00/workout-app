package com.jvigil.hoofmode.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.jvigil.hoofmode.data.local.entity.ActivityType
import com.jvigil.hoofmode.data.local.entity.ScheduleItemType
import com.jvigil.hoofmode.domain.model.CalendarDayStatus
import com.jvigil.hoofmode.domain.model.WeekDayEntry
import com.jvigil.hoofmode.ui.navigation.Screen
import com.jvigil.hoofmode.ui.theme.HoofAccent
import com.jvigil.hoofmode.ui.theme.HoofError
import com.jvigil.hoofmode.ui.theme.HoofOnPrimary
import com.jvigil.hoofmode.ui.theme.HoofPrimary
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavHostController,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val activeState by viewModel.activeState.collectAsState()
    val isCompletedToday by viewModel.isCompletedToday.collectAsState()
    val streak by viewModel.currentStreak.collectAsState()
    val weekStart by viewModel.weekStart.collectAsState()
    val weekDays by viewModel.weekDays.collectAsState()
    var showMenu by remember { mutableStateOf(false) }
    var dayDialog by remember { mutableStateOf<WeekDayEntry?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hoof Mode") },
                actions = {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(Icons.Filled.MoreVert, contentDescription = "Manage")
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(text = { Text("Workout templates") }, onClick = {
                            showMenu = false
                            navController.navigate(Screen.WorkoutList.route)
                        })
                        DropdownMenuItem(text = { Text("Schedules") }, onClick = {
                            showMenu = false
                            navController.navigate(Screen.ScheduleList.route)
                        })
                        DropdownMenuItem(text = { Text("PR history") }, onClick = {
                            showMenu = false
                            navController.navigate(Screen.PrHistory.route)
                        })
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            Text("Current streak: $streak day${if (streak == 1) "" else "s"}", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(16.dp))

            val state = activeState
            if (state == null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("No active schedule", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "Build a schedule and set it active to see what's up next here.",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            } else if (state.currentItem == null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(state.schedule.name, style = MaterialTheme.typography.titleMedium)
                        Text("This schedule has no items yet.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else if (isCompletedToday) {
                val completedType = state.schedule.lastCompletionType
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(state.schedule.name, style = MaterialTheme.typography.labelLarge)
                        Text(
                            if (completedType == ActivityType.REST) "Rest Day Completed" else "Workout Completed",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Text(
                            "Nice work. What's up next unlocks after midnight.",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                    }
                }
            } else {
                val item = state.currentItem
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(state.schedule.name, style = MaterialTheme.typography.labelLarge)
                        Text(
                            if (item.type == ScheduleItemType.REST) "Rest Day" else item.workoutTemplateName ?: "Workout",
                            style = MaterialTheme.typography.titleLarge,
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        if (item.type == ScheduleItemType.REST) {
                            Text(
                                "Marking as completed…",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        viewModel.startOrResumeCurrentWorkout(item.id) { sessionId ->
                                            navController.navigate(Screen.ActiveSession.route(sessionId))
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                ) { Text("Start Workout") }
                                OutlinedButton(
                                    onClick = { viewModel.completeCurrentItem(state.schedule.id) },
                                    modifier = Modifier.weight(1f),
                                ) { Text("Complete Workout") }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            WeekCalendarSection(
                weekStart = weekStart,
                weekDays = weekDays,
                onPreviousWeek = viewModel::goToPreviousWeek,
                onNextWeek = viewModel::goToNextWeek,
                onDayClick = { entry ->
                    if (entry is WeekDayEntry.Actual && entry.day.status == CalendarDayStatus.WORKOUT && entry.day.sessionId != null) {
                        navController.navigate(Screen.SessionDetail.route(entry.day.sessionId))
                    } else {
                        dayDialog = entry
                    }
                },
            )

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = {
                    viewModel.startAdHocSession { sessionId ->
                        navController.navigate(Screen.ActiveSession.route(sessionId))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("Start Ad-Hoc Workout") }
        }
    }

    dayDialog?.let { entry ->
        DayDetailDialog(entry = entry, onDismiss = { dayDialog = null })
    }
}

/**
 * A calendar-week strip: real dates, today marked, past/today days show what actually happened,
 * future days show a rotation projection (not a promise — only completion actually advances the
 * schedule). Prev/next arrows browse other weeks, past or future.
 */
@Composable
private fun WeekCalendarSection(
    weekStart: LocalDate,
    weekDays: List<WeekDayEntry>,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit,
    onDayClick: (WeekDayEntry) -> Unit,
) {
    val today = LocalDate.now()
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("This Week", style = MaterialTheme.typography.titleMedium)
        Text(
            "Today: ${today.format(DateTimeFormatter.ofPattern("EEEE, MMM d"))}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 2.dp, bottom = 8.dp),
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onPreviousWeek) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous week")
            }
            val weekEnd = weekStart.plusDays(6)
            val sameMonth = weekStart.month == weekEnd.month
            val rangeText = if (sameMonth) {
                "${weekStart.format(DateTimeFormatter.ofPattern("MMM d"))} – ${weekEnd.format(DateTimeFormatter.ofPattern("d, yyyy"))}"
            } else {
                "${weekStart.format(DateTimeFormatter.ofPattern("MMM d"))} – ${weekEnd.format(DateTimeFormatter.ofPattern("MMM d, yyyy"))}"
            }
            Text(rangeText, style = MaterialTheme.typography.titleSmall)
            IconButton(onClick = onNextWeek) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next week")
            }
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            weekDays.forEach { entry ->
                WeekDayCell(
                    entry = entry,
                    isToday = entry.date == today,
                    modifier = Modifier.weight(1f),
                    onClick = { onDayClick(entry) },
                )
            }
        }
    }
}

@Composable
private fun WeekDayCell(entry: WeekDayEntry, isToday: Boolean, modifier: Modifier, onClick: () -> Unit) {
    val dayLetter = entry.date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault())
    val label: String?
    val backgroundColor: Color
    val isProjected = entry is WeekDayEntry.Projected

    when (entry) {
        is WeekDayEntry.Actual -> {
            backgroundColor = when (entry.day.status) {
                CalendarDayStatus.WORKOUT -> HoofPrimary
                CalendarDayStatus.REST -> HoofAccent
                CalendarDayStatus.MISSED -> HoofError
                CalendarDayStatus.NONE -> Color.Transparent
            }
            label = entry.day.label ?: if (entry.day.status == CalendarDayStatus.MISSED) "Missed" else null
        }
        is WeekDayEntry.Projected -> {
            val baseColor = when (entry.itemType) {
                ScheduleItemType.WORKOUT -> HoofPrimary
                ScheduleItemType.REST -> HoofAccent
                null -> Color.Transparent
            }
            backgroundColor = if (baseColor == Color.Transparent) baseColor else baseColor.copy(alpha = 0.35f)
            label = entry.label
        }
    }

    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(dayLetter, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Box(
            modifier = Modifier
                .padding(vertical = 2.dp)
                .size(30.dp)
                .background(backgroundColor, RoundedCornerShape(8.dp))
                .then(
                    if (isToday) {
                        Modifier.border(2.dp, MaterialTheme.colorScheme.onSurface, RoundedCornerShape(8.dp))
                    } else {
                        Modifier
                    },
                ),
            contentAlignment = Alignment.Center,
        ) {
            val textColor = if (backgroundColor != Color.Transparent && !isProjected) HoofOnPrimary else MaterialTheme.colorScheme.onSurface
            Text(entry.date.dayOfMonth.toString(), style = MaterialTheme.typography.bodySmall, color = textColor)
        }
        if (label != null) {
            Text(
                label,
                fontSize = 8.sp,
                lineHeight = 9.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                color = if (isProjected) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun DayDetailDialog(entry: WeekDayEntry, onDismiss: () -> Unit) {
    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
    val (title, message) = when (entry) {
        is WeekDayEntry.Actual -> when (entry.day.status) {
            CalendarDayStatus.WORKOUT -> entry.day.label.orEmpty() to "Completed on ${entry.date.format(formatter)}."
            CalendarDayStatus.REST -> "Rest Day" to "Completed as a rest day on ${entry.date.format(formatter)}."
            CalendarDayStatus.MISSED -> "Missed" to "Nothing was logged on ${entry.date.format(formatter)}."
            CalendarDayStatus.NONE -> entry.date.format(formatter) to "No activity yet."
        }
        is WeekDayEntry.Projected -> {
            val what = entry.label ?: "Nothing scheduled"
            "Projected: $what" to "If you complete one item per day, this is what ${entry.date.format(formatter)} would be — " +
                "it only becomes real once you actually complete it."
        }
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = { TextButton(onClick = onDismiss) { Text("OK") } },
    )
}
