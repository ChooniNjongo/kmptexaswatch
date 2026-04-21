package com.jetbrains.spacetutorial

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jetbrains.spacetutorial.texaswatch.theme.TexasWatchTheme
import com.jetbrains.spacetutorial.texaswatch.ui.OffenderCard
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OffenderListScreen() {
    TexasWatchTheme {
        val viewModel = koinViewModel<OffenderListViewModel>()
        val state by remember { viewModel.state }
        var isRefreshing by remember { mutableStateOf(false) }
        val colors = TexasWatchTheme.colors
        val typography = TexasWatchTheme.typography

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "Texas Sex Offender Registry",
                            style = typography.h3,
                            color = colors.invertedText
                        )
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = colors.primaryAccent
                    )
                )
            },
            containerColor = colors.mainBackground
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
                        CircularProgressIndicator(color = colors.primaryAccent)
                    }
                    state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Error: ${state.error}", style = typography.text1, color = colors.dangerText)
                    }
                    else -> LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(state.offenders) { offender ->
                            OffenderCard(offender = offender)
                        }
                    }
                }
            }
        }
    }
}
