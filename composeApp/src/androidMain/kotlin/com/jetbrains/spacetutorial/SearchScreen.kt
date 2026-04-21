package com.jetbrains.spacetutorial

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.jetbrains.spacetutorial.texaswatch.theme.TexasWatchTheme
import com.jetbrains.spacetutorial.ui.MainHeaderSearchBar

@Composable
fun SearchScreen(onBack: () -> Unit) {
    val colors = TexasWatchTheme.colors
    var searchQuery by rememberSaveable { mutableStateOf("") }

    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.mainBackground),
    ) {
        MainHeaderSearchBar(
            searchValue = searchQuery,
            onSearchValueChange = { searchQuery = it },
            onClose = onBack,
            onClear = { searchQuery = "" },
        )
        HorizontalDivider(thickness = 1.dp, color = colors.strokePale)

        // Body — search results coming soon
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.mainBackground),
        )
    }
}
