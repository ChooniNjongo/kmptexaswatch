package com.jetbrains.spacetutorial

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.jetbrains.spacetutorial.texaswatch.entity.OffenderSummary
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OffenderListScreen() {
    val viewModel = koinViewModel<OffenderListViewModel>()
    val state by remember { viewModel.state }
    var isRefreshing by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Texas Sex Offender Registry") })
        }
    ) { padding ->
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                viewModel.loadOffenders(forceReload = true)
                isRefreshing = false
            },
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            when {
                state.isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Error: ${state.error}", color = MaterialTheme.colorScheme.error)
                }
                else -> LazyColumn(Modifier.fillMaxSize()) {
                    items(state.offenders) { OffenderRow(it) }
                }
            }
        }
    }
}

@Composable
fun OffenderRow(offender: OffenderSummary) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(offender.fullName, fontWeight = FontWeight.Bold)
        Text("DPS: ${offender.dpsNumber}", style = MaterialTheme.typography.bodySmall)
        if (offender.age != null) Text("Age: ${offender.age}", style = MaterialTheme.typography.bodySmall)
        if (!offender.address.isNullOrBlank()) Text(offender.address ?: "", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
    Divider()
}
