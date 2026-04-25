package com.jetbrains.spacetutorial

import android.Manifest
import android.content.ContentResolver
import android.content.pm.PackageManager
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jetbrains.spacetutorial.texaswatch.TexasWatchSDK
import com.jetbrains.spacetutorial.texaswatch.entity.ContactMatchResult
import com.jetbrains.spacetutorial.texaswatch.theme.Colors
import com.jetbrains.spacetutorial.texaswatch.theme.TexasWatchTheme
import com.jetbrains.spacetutorial.texaswatch.theme.Typography as TWTypography
import com.jetbrains.spacetutorial.texaswatch.ui.OffenderCard
import com.jetbrains.spacetutorial.ui.MainHeaderTitleBar
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

// ── ViewModel ─────────────────────────────────────────────────────────────────

sealed class ContactScanState {
    object Idle : ContactScanState()
    object LoadingContacts : ContactScanState()
    data class Scanning(val progress: Int, val total: Int) : ContactScanState()
    data class Done(val results: List<ContactMatchResult>, val totalMatches: Int, val contactsWithMatches: Int) : ContactScanState()
    data class Error(val message: String) : ContactScanState()
}

class ContactScanViewModel : ViewModel(), KoinComponent {
    private val sdk: TexasWatchSDK by inject()

    var state by mutableStateOf<ContactScanState>(ContactScanState.Idle)
        private set

    fun scan(contentResolver: ContentResolver) {
        viewModelScope.launch {
            state = ContactScanState.LoadingContacts
            try {
                val names = loadContactNames(contentResolver)
                if (names.isEmpty()) {
                    state = ContactScanState.Done(emptyList(), 0, 0)
                    return@launch
                }

                // Batch in chunks of 20 to show progress and avoid one giant request
                val allResults = mutableListOf<ContactMatchResult>()
                var totalMatches = 0
                var contactsWithMatches = 0
                val chunks = names.chunked(20)

                chunks.forEachIndexed { i, chunk ->
                    state = ContactScanState.Scanning(
                        progress = i * 20,
                        total = names.size
                    )
                    val response = sdk.searchByContacts(chunk)
                    allResults += response.results
                    totalMatches += response.totalMatches
                    contactsWithMatches += response.contactsWithMatches
                }

                state = ContactScanState.Done(allResults, totalMatches, contactsWithMatches)
            } catch (e: Exception) {
                state = ContactScanState.Error(e.message ?: "Scan failed")
            }
        }
    }

    private fun loadContactNames(resolver: ContentResolver): List<String> {
        val names = mutableListOf<String>()
        val cursor = resolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            arrayOf(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY),
            "${ContactsContract.Contacts.HAS_PHONE_NUMBER} > 0",
            null,
            ContactsContract.Contacts.DISPLAY_NAME_PRIMARY
        ) ?: return names
        cursor.use {
            while (it.moveToNext()) {
                val name = it.getString(it.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY))
                    ?.trim() ?: continue
                if (name.isNotBlank()) names.add(name)
            }
        }
        return names.distinct()
    }
}

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun ContactScanScreen(
    onBack: () -> Unit,
    onOffender: (Int) -> Unit,
) {
    val colors = TexasWatchTheme.colors
    val typography = TexasWatchTheme.typography
    val vm: ContactScanViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
    val context = LocalContext.current

    var permissionGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        permissionGranted = granted
        if (granted) vm.scan(context.contentResolver)
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(colors.mainBackground)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        MainHeaderTitleBar(
            title = "Scan Contacts",
            startContent = {
                IconButton(onClick = onBack) {
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(R.drawable.arrow_left_24),
                        contentDescription = "Back",
                        tint = TexasWatchTheme.colors.primaryText,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        )

        when (val s = vm.state) {
            is ContactScanState.Idle -> {
                // Show permission prompt or start button
                Column(
                    Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        "Scan your contacts to check if any match registered sex offenders.",
                        style = typography.text1,
                        color = colors.secondaryText,
                        modifier = Modifier.padding(bottom = 32.dp),
                    )
                    Button(
                        onClick = {
                            if (permissionGranted) vm.scan(context.contentResolver)
                            else permissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = colors.primaryAccent),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                    ) {
                        Text(
                            if (permissionGranted) "Start Scan" else "Allow Contacts & Scan",
                            style = typography.h4,
                            color = colors.invertedText,
                        )
                    }
                }
            }

            is ContactScanState.LoadingContacts -> {
                CenteredStatus("Loading contacts…", loading = true, colors = colors, typography = typography)
            }

            is ContactScanState.Scanning -> {
                Column(
                    Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    SpinningDisc(size = 56.dp)
                    Spacer(Modifier.height(20.dp))
                    Text(
                        "Scanning ${s.progress} / ${s.total} contacts…",
                        style = typography.text1,
                        color = colors.secondaryText,
                    )
                    Spacer(Modifier.height(12.dp))
                    LinearProgressIndicator(
                        progress = { if (s.total > 0) s.progress.toFloat() / s.total else 0f },
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(4.dp)),
                        color = colors.primaryAccent,
                        trackColor = colors.strokePale,
                    )
                }
            }

            is ContactScanState.Done -> {
                if (s.results.isEmpty()) {
                    CenteredStatus("No matches found in your contacts.", loading = false, colors = colors, typography = typography)
                } else {
                    LazyColumn(Modifier.fillMaxSize()) {
                        item {
                            ScanSummaryHeader(s.contactsWithMatches, s.totalMatches, colors, typography)
                        }
                        s.results.forEach { group ->
                            item(key = "header_${group.contactName}") {
                                ContactGroupHeader(group.contactName, group.matches.size, colors, typography)
                            }
                            items(group.matches, key = { "${group.contactName}_${it.indIdn}" }) { offender ->
                                OffenderCard(
                                    offender = offender,
                                    onClick = { onOffender(offender.indIdn) },
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp),
                                )
                            }
                        }
                        item { Spacer(Modifier.height(24.dp)) }
                    }
                }
            }

            is ContactScanState.Error -> {
                CenteredStatus("Error: ${s.message}", loading = false, colors = colors, typography = typography)
            }
        }
    }
}

@Composable
private fun ScanSummaryHeader(contactsWithMatches: Int, totalMatches: Int, colors: Colors, typography: TWTypography) {
    Row(
        Modifier.fillMaxWidth().padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                "$contactsWithMatches contact${if (contactsWithMatches == 1) "" else "s"} matched",
                style = typography.h4,
                color = colors.primaryText,
            )
            Text(
                "$totalMatches offender result${if (totalMatches == 1) "" else "s"} found",
                style = typography.text2,
                color = colors.secondaryText,
            )
        }
    }
}

@Composable
private fun ContactGroupHeader(contactName: String, matchCount: Int, colors: Colors, typography: TWTypography) {
    Row(
        Modifier
            .fillMaxWidth()
            .background(colors.surfaceBackground)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            contactName,
            style = typography.text1.copy(fontWeight = FontWeight.SemiBold),
            color = colors.primaryText,
            modifier = Modifier.weight(1f),
        )
        Box(
            Modifier
                .clip(RoundedCornerShape(50))
                .background(colors.dangerBadge.copy(alpha = 0.15f))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                "$matchCount match${if (matchCount == 1) "" else "es"}",
                style = typography.label,
                color = colors.dangerText,
            )
        }
    }
}

@Composable
private fun CenteredStatus(text: String, loading: Boolean, colors: Colors, typography: TWTypography) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (loading) {
            SpinningDisc(size = 56.dp)
            Spacer(Modifier.height(16.dp))
        }
        Text(text, style = typography.text1, color = colors.secondaryText)
    }
}
