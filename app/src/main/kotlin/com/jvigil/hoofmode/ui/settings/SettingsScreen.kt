package com.jvigil.hoofmode.ui.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.jvigil.hoofmode.data.prefs.WeightUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavHostController,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsState()

    val notificationPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {}

    Scaffold(topBar = { TopAppBar(title = { Text("Settings") }) }) { padding ->
        Column(modifier = Modifier.fillMaxWidth().padding(padding).padding(16.dp)) {
            Text("Weight unit", style = MaterialTheme.typography.titleMedium)
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                SegmentedButton(
                    selected = settings.weightUnit == WeightUnit.LB,
                    onClick = { viewModel.setWeightUnit(WeightUnit.LB) },
                    shape = SegmentedButtonDefaults.itemShape(0, 2),
                ) { Text("lb") }
                SegmentedButton(
                    selected = settings.weightUnit == WeightUnit.KG,
                    onClick = { viewModel.setWeightUnit(WeightUnit.KG) },
                    shape = SegmentedButtonDefaults.itemShape(1, 2),
                ) { Text("kg") }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp))

            Text("Body weight reminders", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            ) {
                Text("Enabled", modifier = Modifier.weight(1f))
                Switch(
                    checked = settings.bodyWeightReminderEnabled,
                    onCheckedChange = { enabled ->
                        if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                        viewModel.setBodyWeightReminder(enabled, settings.reminderFrequencyDays)
                    },
                )
            }
            if (settings.bodyWeightReminderEnabled) {
                Text("Every ${settings.reminderFrequencyDays} day(s)", modifier = Modifier.padding(top = 8.dp))
                Slider(
                    value = settings.reminderFrequencyDays.toFloat(),
                    onValueChange = { viewModel.setBodyWeightReminder(true, it.toInt().coerceIn(1, 14)) },
                    valueRange = 1f..14f,
                    steps = 12,
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp))

            Text("Rest timer", style = MaterialTheme.typography.titleMedium)
            Text("Default: ${settings.restTimerDefaultSeconds}s", modifier = Modifier.padding(top = 8.dp))
            Slider(
                value = settings.restTimerDefaultSeconds.toFloat(),
                onValueChange = {
                    viewModel.setRestTimerDefaults(it.toInt().coerceIn(15, 300), settings.restTimerVibrate)
                },
                valueRange = 15f..300f,
            )
            Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                Text("Vibrate when done", modifier = Modifier.weight(1f))
                Switch(
                    checked = settings.restTimerVibrate,
                    onCheckedChange = { viewModel.setRestTimerDefaults(settings.restTimerDefaultSeconds, it) },
                )
            }
        }
    }
}
