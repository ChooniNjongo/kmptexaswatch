package com.jetbrains.spacetutorial

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.jetbrains.spacetutorial.texaswatch.entity.TEXAS_COUNTIES
import com.jetbrains.spacetutorial.texaswatch.entity.TexasCounty
import com.jetbrains.spacetutorial.texaswatch.theme.TexasWatchTheme
import com.jetbrains.spacetutorial.texaswatch.ui.OffenderCard
import com.jetbrains.spacetutorial.ui.MainHeaderSearchBar
import org.koin.androidx.compose.koinViewModel
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.foundation.Canvas
import kotlin.math.min

// ── Filter chip label → API code maps ────────────────────────────────────────

private val RISK_MAP  = mapOf("Low Risk" to "1", "Moderate Risk" to "2", "High Risk" to "3")
private val RACE_MAP  = mapOf("White" to "W", "Black" to "B", "Asian" to "A",
                               "American Indian" to "I", "Other" to "O")
private val HAIR_MAP  = mapOf("Black" to "BLK", "Brown" to "BRO", "Blonde" to "BLN",
                               "Red" to "RED", "Gray" to "GRY", "White" to "WHI", "Bald" to "BAL")
private val EYE_MAP   = mapOf("Brown" to "BRO", "Blue" to "BLU", "Green" to "GRN",
                               "Hazel" to "HAZ", "Gray" to "GRY", "Black" to "BLK", "Maroon" to "MAR")

private data class FilterGroupDef(
    val title: String,
    val labels: List<String>,
    val codeMap: Map<String, String>,
)

private val FILTER_GROUPS = listOf(
    FilterGroupDef("Offender Level", RISK_MAP.keys.toList(), RISK_MAP),
    FilterGroupDef("Race",           RACE_MAP.keys.toList(), RACE_MAP),
    FilterGroupDef("Hair Color",     HAIR_MAP.keys.toList(), HAIR_MAP),
    FilterGroupDef("Eye Color",      EYE_MAP.keys.toList(),  EYE_MAP),
)

// ── SearchScreen ──────────────────────────────────────────────────────────────

@Composable
fun SearchScreen(onBack: () -> Unit, onOffender: (Int) -> Unit = {}, onScanContacts: () -> Unit = {}) {
    val colors = TexasWatchTheme.colors
    val vm: SearchViewModel = koinViewModel()
    val state by vm.state.collectAsState()
    val criteria by vm.criteria.collectAsState()

    var filtersExpanded by rememberSaveable { mutableStateOf(true) }
    var showCountySheet by rememberSaveable { mutableStateOf(false) }

    val listState = rememberLazyListState()

    // Infinite scroll — load next page when near bottom
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo }
            .collect { layout ->
                val lastVisible = layout.visibleItemsInfo.lastOrNull()?.index ?: 0
                val total = layout.totalItemsCount
                if (total > 0 && lastVisible >= total - 5) {
                    vm.loadNextPage()
                }
            }
    }

    BackHandler { onBack() }

    if (showCountySheet) {
        CountyPickerSheet(
            selected = criteria.county,
            onSelect = { county ->
                showCountySheet = false
                vm.onFilterChanged(criteria.copy(county = county))
            },
            onDismiss = { showCountySheet = false },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.mainBackground)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .imePadding(),
    ) {
        MainHeaderSearchBar(
            searchValue = criteria.name,
            onSearchValueChange = { vm.onNameChanged(it) },
            onClose = onBack,
            onClear = { vm.onNameChanged("") },
        )
        HorizontalDivider(thickness = 1.dp, color = colors.strokePale)

        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize(),
        ) {
            // ── Filter panel ──────────────────────────────────────────────────
            item(key = "filter_panel") {
                FilterPanel(
                    criteria = criteria,
                    expanded = filtersExpanded,
                    onExpandToggle = { filtersExpanded = !filtersExpanded },
                    onCountyClick = { showCountySheet = true },
                    onFilterChanged = { vm.onFilterChanged(it) },
                    onClearAll = { vm.clearAllFilters() },
                    modifier = Modifier.padding(horizontal = 12.dp).padding(top = 12.dp),
                )
            }

            // ── Results header ────────────────────────────────────────────────
            if (criteria.hasAnyFilter) {
                item(key = "results_header") {
                    val label = when {
                        state.isLoading     -> "Searching…"
                        state.results.isEmpty() -> "No results found"
                        else -> "${state.totalResults} offender${if (state.totalResults == 1L) "" else "s"} found"
                    }
                    Text(
                        text = label,
                        style = TexasWatchTheme.typography.text2,
                        color = colors.secondaryText,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }

            // ── Spinner ───────────────────────────────────────────────────────
            if (state.isLoading && criteria.hasAnyFilter) {
                item(key = "spinner") {
                    Box(modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                        contentAlignment = Alignment.Center) {
                        SpinningDisc(size = 64.dp)
                    }
                }
            }

            // ── Error ─────────────────────────────────────────────────────────
            state.error?.let { err ->
                item(key = "error") {
                    Text(
                        text = err,
                        style = TexasWatchTheme.typography.text2,
                        color = colors.dangerText,
                        modifier = Modifier.padding(horizontal = 16.dp),
                    )
                }
            }

            // ── Offender cards ────────────────────────────────────────────────
            items(state.results, key = { it.indIdn }) { offender ->
                OffenderCard(
                    offender = offender,
                    onClick = { onOffender(offender.indIdn) },
                    modifier = Modifier.padding(horizontal = 12.dp),
                )
            }

            // ── Load-more spinner ─────────────────────────────────────────────
            if (state.isLoadingMore) {
                item(key = "load_more") {
                    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center) {
                        SpinningDisc(size = 40.dp)
                    }
                }
            }

            item(key = "bottom_space") { Spacer(Modifier.height(24.dp)) }
        }
    }
}

// ── Filter panel card ─────────────────────────────────────────────────────────

@Composable
private fun FilterPanel(
    criteria: SearchCriteria,
    expanded: Boolean,
    onExpandToggle: () -> Unit,
    onCountyClick: () -> Unit,
    onFilterChanged: (SearchCriteria) -> Unit,
    onClearAll: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TexasWatchTheme.colors
    val typography = TexasWatchTheme.typography
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 0f else 180f, label = "chevron"
    )
    val activeCount = criteria.riskLevels.size + criteria.races.size +
            criteria.hairColors.size + criteria.eyeColors.size +
            (if (criteria.county != null) 1 else 0)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceBackground),
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onExpandToggle,
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Text("Filters", style = typography.h4, color = colors.primaryText)
            if (activeCount > 0) {
                Spacer(Modifier.size(8.dp))
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(colors.primaryAccent)
                        .padding(horizontal = 7.dp, vertical = 2.dp),
                ) {
                    Text(
                        text = "$activeCount",
                        style = typography.label,
                        color = colors.invertedText,
                    )
                }
            }
            Spacer(Modifier.weight(1f))
            if (activeCount > 0) {
                Text(
                    text = "Clear all",
                    style = typography.text2,
                    color = colors.primaryAccent,
                    modifier = Modifier
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = onClearAll,
                        )
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                )
            }
            Icon(
                painter = painterResource(R.drawable.arrow_left_24),
                contentDescription = null,
                tint = colors.primaryText,
                modifier = Modifier.size(18.dp).rotate(chevronRotation - 90f),
            )
        }

        // Collapsible content
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) + expandVertically(),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 16.dp),
            ) {
                HorizontalDivider(thickness = 0.5.dp, color = colors.strokePale)

                // County
                FilterSectionLabel("County")
                CountyRow(selected = criteria.county, onTap = onCountyClick)

                // Chip groups
                FILTER_GROUPS.forEach { group ->
                    FilterSectionLabel(group.title)
                    ChipGroup(
                        group = group,
                        activeSet = when (group.title) {
                            "Offender Level" -> criteria.riskLevels
                            "Race"           -> criteria.races
                            "Hair Color"     -> criteria.hairColors
                            else             -> criteria.eyeColors
                        },
                        onToggle = { code ->
                            val updated = criteria.copy(
                                riskLevels = if (group.title == "Offender Level")
                                    criteria.riskLevels.toggle(code) else criteria.riskLevels,
                                races = if (group.title == "Race")
                                    criteria.races.toggle(code) else criteria.races,
                                hairColors = if (group.title == "Hair Color")
                                    criteria.hairColors.toggle(code) else criteria.hairColors,
                                eyeColors = if (group.title == "Eye Color")
                                    criteria.eyeColors.toggle(code) else criteria.eyeColors,
                            )
                            onFilterChanged(updated)
                        },
                    )
                }
            }
        }
    }
}

private fun Set<String>.toggle(value: String): Set<String> =
    if (contains(value)) this - value else this + value

// ── County row ────────────────────────────────────────────────────────────────

@Composable
private fun CountyRow(selected: TexasCounty?, onTap: () -> Unit) {
    val colors = TexasWatchTheme.colors
    val typography = TexasWatchTheme.typography
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = if (selected != null) 1.5.dp else 0.5.dp,
                color = if (selected != null) colors.primaryAccent else colors.strokeFull,
                shape = RoundedCornerShape(8.dp),
            )
            .background(colors.mainBackground)
            .clickable(onClick = onTap)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = selected?.name?.lowercase()?.replaceFirstChar { it.uppercase() }
                ?: "Select a county",
            style = typography.text2,
            color = if (selected != null) colors.primaryText else colors.secondaryText,
        )
        Icon(
            painter = painterResource(R.drawable.arrow_left_24),
            contentDescription = null,
            tint = colors.secondaryText,
            modifier = Modifier.size(16.dp).rotate(-90f),
        )
    }
}

// ── Section label ─────────────────────────────────────────────────────────────

@Composable
private fun FilterSectionLabel(title: String) {
    Text(
        text = title,
        style = TexasWatchTheme.typography.text2,
        color = TexasWatchTheme.colors.secondaryText,
    )
}

// ── Chip group ────────────────────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ChipGroup(
    group: FilterGroupDef,
    activeSet: Set<String>,
    onToggle: (String) -> Unit,
) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        group.labels.forEach { label ->
            val code = group.codeMap[label] ?: label
            FilterChipTag(label = label, selected = activeSet.contains(code)) {
                onToggle(code)
            }
        }
    }
}

// ── Filter chip ───────────────────────────────────────────────────────────────

private val ChipShape = RoundedCornerShape(8.dp)

@Composable
private fun FilterChipTag(label: String, selected: Boolean, onToggle: () -> Unit) {
    val colors = TexasWatchTheme.colors
    val bgColor by animateColorAsState(
        targetValue = if (selected) colors.primaryAccent else colors.mainBackground,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow), label = "bg"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) colors.invertedText else colors.primaryText,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow), label = "text"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) colors.primaryAccent else colors.strokeFull,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow), label = "border"
    )
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(ChipShape)
            .border(if (selected) 0.dp else 0.5.dp, borderColor, ChipShape)
            .background(bgColor)
            .clickable(onClick = onToggle)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(label, style = TexasWatchTheme.typography.text2, color = textColor)
    }
}

// ── Spinning disc loader ──────────────────────────────────────────────────────

@Composable
internal fun SpinningDisc(size: androidx.compose.ui.unit.Dp) {
    val colors = TexasWatchTheme.colors
    val accent = colors.primaryAccent

    val infiniteTransition = rememberInfiniteTransition(label = "disc")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "rotation",
    )
    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 450, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "pulse",
    )

    Canvas(modifier = Modifier.size(size)) {
        val stroke = size.toPx() * 0.12f
        val radius = (min(this.size.width, this.size.height) - stroke) / 2f
        val center = androidx.compose.ui.geometry.Offset(this.size.width / 2, this.size.height / 2)

        // Track ring
        drawCircle(
            color = accent.copy(alpha = 0.15f),
            radius = radius,
            style = Stroke(width = stroke),
        )

        // Spinning arc
        drawArc(
            brush = Brush.sweepGradient(
                colors = listOf(accent.copy(alpha = 0.1f), accent),
                center = center,
            ),
            startAngle = rotation,
            sweepAngle = 260f,
            useCenter = false,
            style = Stroke(width = stroke, cap = StrokeCap.Round),
        )

        // Centre dot
        drawCircle(
            color = accent,
            radius = stroke * pulse,
        )
    }
}

// ── County picker bottom sheet ────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CountyPickerSheet(
    selected: TexasCounty?,
    onSelect: (TexasCounty) -> Unit,
    onDismiss: () -> Unit,
) {
    val colors = TexasWatchTheme.colors
    val typography = TexasWatchTheme.typography
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var query by rememberSaveable { mutableStateOf("") }
    val filtered by remember {
        derivedStateOf {
            if (query.isBlank()) TEXAS_COUNTIES
            else TEXAS_COUNTIES.filter { it.name.contains(query.trim(), ignoreCase = true) }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = colors.surfaceBackground,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("Select County", style = typography.h4, color = colors.primaryText)

            // Search field
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .border(1.dp, colors.strokeFull, RoundedCornerShape(8.dp))
                    .background(colors.mainBackground)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = typography.text2.copy(color = colors.primaryText),
                    cursorBrush = SolidColor(colors.primaryAccent),
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Search,
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    decorationBox = { inner ->
                        if (query.isEmpty()) {
                            Text("Search counties…", style = typography.text2,
                                color = colors.secondaryText)
                        }
                        inner()
                    },
                )
            }

            LazyColumn(modifier = Modifier.heightIn(max = 400.dp)) {
                items(filtered, key = { it.code }) { county ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(county) }
                            .padding(vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = county.name.lowercase().replaceFirstChar { it.uppercase() },
                            style = typography.text2,
                            color = if (county.code == selected?.code) colors.primaryAccent
                            else colors.primaryText,
                        )
                        if (county.code == selected?.code) {
                            Icon(
                                painter = painterResource(R.drawable.arrow_left_24),
                                contentDescription = null,
                                tint = colors.primaryAccent,
                                modifier = Modifier.size(14.dp),
                            )
                        }
                    }
                    HorizontalDivider(thickness = 0.5.dp, color = colors.strokePale)
                }
            }
        }
    }
}
