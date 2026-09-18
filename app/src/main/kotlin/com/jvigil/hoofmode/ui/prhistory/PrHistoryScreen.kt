package com.jvigil.hoofmode.ui.prhistory

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.jvigil.hoofmode.ui.components.BackIconButton
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrHistoryScreen(
    navController: NavHostController,
    viewModel: PrHistoryViewModel = hiltViewModel(),
) {
    val bestSets by viewModel.bestSets.collectAsState()
    val formatter = DateTimeFormatter.ofPattern("MMM d, yyyy")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PR History") },
                navigationIcon = { BackIconButton(onClick = { navController.popBackStack() }) },
            )
        },
    ) { padding ->
        if (bestSets.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No personal records yet — log some sets to get started.", style = MaterialTheme.typography.bodyMedium)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding)) {
                items(bestSets, key = { it.exerciseId }) { row ->
                    Column(modifier = Modifier.animateItem()) {
                        ListItem(
                            headlineContent = { Text(row.exerciseName) },
                            supportingContent = {
                                Text("${row.weight} × ${row.reps} — ${row.achievedAt.atZone(ZoneId.systemDefault()).format(formatter)}")
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                        HorizontalDivider()
                    }
                }
            }
        }
    }
}
