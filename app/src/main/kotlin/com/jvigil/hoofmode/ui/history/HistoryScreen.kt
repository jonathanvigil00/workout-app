package com.jvigil.hoofmode.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import com.jvigil.hoofmode.data.local.entity.WorkoutSessionEntity
import com.jvigil.hoofmode.domain.model.CalendarDay
import com.jvigil.hoofmode.domain.model.CalendarDayStatus
import com.jvigil.hoofmode.ui.navigation.Screen
import com.jvigil.hoofmode.ui.theme.HoofAccent
import com.jvigil.hoofmode.ui.theme.HoofError
import com.jvigil.hoofmode.ui.theme.HoofOnPrimary
import com.jvigil.hoofmode.ui.theme.HoofPrimary
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private enum class HistoryTab { CALENDAR, LIST }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    navController: NavHostController,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    var tab by remember { mutableStateOf(HistoryTab.CALENDAR) }
    val month by viewModel.currentMonth.collectAsState()
    val calendarDays by viewModel.calendarDays.collectAsState()
    val streak by viewModel.currentStreak.collectAsState()
    val workoutsThisMonth by viewModel.workoutsThisMonth.collectAsState()
    val sessions by viewModel.allSessions.collectAsState()
    var dayDialog by remember { mutableStateOf<CalendarDay?>(null) }

    Scaffold(topBar = { TopAppBar(title = { Text("History") }) }) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                SegmentedButton(
                    selected = tab == HistoryTab.CALENDAR,
                    onClick = { tab = HistoryTab.CALENDAR },
                    shape = SegmentedButtonDefaults.itemShape(0, 2),
                ) { Text("Calendar") }
                SegmentedButton(
                    selected = tab == HistoryTab.LIST,
                    onClick = { tab = HistoryTab.LIST },
                    shape = SegmentedButtonDefaults.itemShape(1, 2),
                ) { Text("List") }
            }

            if (tab == HistoryTab.CALENDAR) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { viewModel.goToPreviousMonth() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous month")
                    }
                    Text(
                        "${month.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${month.year}",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    IconButton(onClick = { viewModel.goToNextMonth() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next month")
                    }
                }

                Text(
                    "Streak: $streak day${if (streak == 1) "" else "s"} · $workoutsThisMonth workout${if (workoutsThisMonth == 1) "" else "s"} this month",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )

                CalendarGrid(
                    month = month,
                    calendarDays = calendarDays,
                    onDayClick = { day ->
                        if (day.status == CalendarDayStatus.WORKOUT && day.sessionId != null) {
                            navController.navigate(Screen.SessionDetail.route(day.sessionId))
                        } else if (day.status != CalendarDayStatus.NONE) {
                            dayDialog = day
                        }
                    },
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(sessions, key = { it.id }) { session ->
                        Column(modifier = Modifier.animateItem()) {
                            SessionListRow(session) { navController.navigate(Screen.SessionDetail.route(session.id)) }
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }

    dayDialog?.let { day ->
        val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
        AlertDialog(
            onDismissRequest = { dayDialog = null },
            title = {
                Text(
                    when (day.status) {
                        CalendarDayStatus.REST -> "Rest Day"
                        CalendarDayStatus.MISSED -> "Missed"
                        else -> day.date.format(formatter)
                    },
                )
            },
            text = {
                Text(
                    when (day.status) {
                        CalendarDayStatus.REST -> "Completed as a rest day on ${day.date.format(formatter)}."
                        CalendarDayStatus.MISSED -> "Nothing was logged on ${day.date.format(formatter)}."
                        CalendarDayStatus.WORKOUT -> day.label ?: "Workout completed on ${day.date.format(formatter)}."
                        CalendarDayStatus.NONE -> ""
                    },
                )
            },
            confirmButton = { TextButton(onClick = { dayDialog = null }) { Text("OK") } },
        )
    }
}

@Composable
private fun SessionListRow(session: WorkoutSessionEntity, onClick: () -> Unit) {
    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a")
    ListItem(
        headlineContent = { Text(session.startTime.atZone(java.time.ZoneId.systemDefault()).format(formatter)) },
        supportingContent = { Text(if (session.isAdHoc) "Ad-hoc workout" else "Scheduled workout") },
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
    )
}

@Composable
private fun CalendarGrid(
    month: YearMonth,
    calendarDays: List<CalendarDay>,
    onDayClick: (CalendarDay) -> Unit,
) {
    val firstOfMonth = month.atDay(1)
    val leadingBlanks = firstOfMonth.dayOfWeek.value % 7 // Monday=1..Sunday=7 -> Sunday-first grid blanks
    val cells: List<CalendarDay?> = List(leadingBlanks) { null } + calendarDays
    val rows = (cells.size + 6) / 7

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach {
                Text(it, modifier = Modifier.weight(1f), textAlign = TextAlign.Center)
            }
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.fillMaxWidth().height((rows * 64).dp),
        ) {
            gridItems(cells) { day ->
                if (day == null) {
                    Box(modifier = Modifier.aspectRatio(1f))
                } else {
                    CalendarDayCell(day = day, onClick = { onDayClick(day) })
                }
            }
        }
    }
}

@Composable
private fun CalendarDayCell(day: CalendarDay, onClick: () -> Unit) {
    val backgroundColor = when (day.status) {
        CalendarDayStatus.WORKOUT -> HoofPrimary
        CalendarDayStatus.REST -> HoofAccent
        CalendarDayStatus.MISSED -> HoofError
        CalendarDayStatus.NONE -> Color.Transparent
    }
    val onBackgroundColor = if (day.status == CalendarDayStatus.NONE) {
        MaterialTheme.colorScheme.onSurface
    } else {
        HoofOnPrimary
    }

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .background(backgroundColor, RoundedCornerShape(8.dp))
            .clickable(enabled = day.status != CalendarDayStatus.NONE, onClick = onClick)
            .padding(2.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                day.date.dayOfMonth.toString(),
                color = onBackgroundColor,
                fontSize = 11.sp,
                style = MaterialTheme.typography.bodySmall,
            )
            val subtitle = when (day.status) {
                CalendarDayStatus.MISSED -> "Missed"
                CalendarDayStatus.REST -> "Rest"
                CalendarDayStatus.WORKOUT -> day.label
                CalendarDayStatus.NONE -> null
            }
            if (subtitle != null) {
                Text(
                    subtitle,
                    color = onBackgroundColor,
                    fontSize = 8.sp,
                    lineHeight = 9.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
