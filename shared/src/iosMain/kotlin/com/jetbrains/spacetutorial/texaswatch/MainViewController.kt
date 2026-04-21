package com.jetbrains.spacetutorial.texaswatch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeUIViewController
import com.jetbrains.spacetutorial.texaswatch.entity.OffenderSummary
import com.jetbrains.spacetutorial.texaswatch.theme.TexasWatchTheme
import com.jetbrains.spacetutorial.texaswatch.ui.OffenderCard
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import platform.UIKit.UIViewController

object TexasWatchIOS : KoinComponent {
    val sdk: TexasWatchSDK by inject()
}

fun MainViewController(): UIViewController = ComposeUIViewController {
    TexasWatchTheme {
        OffenderListContent()
    }
}

@Composable
fun OffenderListContent() {
    var offenders by remember { mutableStateOf<List<OffenderSummary>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        try {
            offenders = TexasWatchIOS.sdk.getOffenders(forceReload = false)
        } catch (e: Exception) {
            error = e.message ?: "Unknown error"
        } finally {
            isLoading = false
        }
    }

    val colors = TexasWatchTheme.colors
    val typography = TexasWatchTheme.typography

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.mainBackground)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.primaryAccent)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Text(
                "Texas Sex Offender Registry",
                style = typography.h3,
                color = colors.invertedText
            )
        }

        when {
            isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = colors.primaryAccent)
            }
            error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: $error", style = typography.text1, color = colors.dangerText)
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(offenders) { offender ->
                    OffenderCard(offender = offender)
                }
            }
        }
    }
}
