package com.jetbrains.spacetutorial

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.jetbrains.spacetutorial.texaswatch.theme.TexasWatchTheme
import com.jetbrains.spacetutorial.ui.MainHeaderSearchBar

// ── Filter data model ─────────────────────────────────────────────────────────

private enum class FilterGroup { OffenderLevel, Race, HairColor, EyeColor }

private data class FilterChip(
    val group: FilterGroup,
    val label: String,
    val selected: Boolean = false,
)

private fun defaultFilters() = listOf(
    // Offender Level
    FilterChip(FilterGroup.OffenderLevel, "Low Risk"),
    FilterChip(FilterGroup.OffenderLevel, "Moderate Risk"),
    FilterChip(FilterGroup.OffenderLevel, "High Risk"),

    // Race
    FilterChip(FilterGroup.Race, "White"),
    FilterChip(FilterGroup.Race, "Black"),
    FilterChip(FilterGroup.Race, "Asian"),
    FilterChip(FilterGroup.Race, "American Indian"),
    FilterChip(FilterGroup.Race, "Other"),

    // Hair Color
    FilterChip(FilterGroup.HairColor, "Black"),
    FilterChip(FilterGroup.HairColor, "Brown"),
    FilterChip(FilterGroup.HairColor, "Blonde"),
    FilterChip(FilterGroup.HairColor, "Red"),
    FilterChip(FilterGroup.HairColor, "Gray"),
    FilterChip(FilterGroup.HairColor, "White"),
    FilterChip(FilterGroup.HairColor, "Bald"),

    // Eye Color
    FilterChip(FilterGroup.EyeColor, "Brown"),
    FilterChip(FilterGroup.EyeColor, "Blue"),
    FilterChip(FilterGroup.EyeColor, "Green"),
    FilterChip(FilterGroup.EyeColor, "Hazel"),
    FilterChip(FilterGroup.EyeColor, "Gray"),
    FilterChip(FilterGroup.EyeColor, "Black"),
    FilterChip(FilterGroup.EyeColor, "Maroon"),
)

// ── SearchScreen ──────────────────────────────────────────────────────────────

@Composable
fun SearchScreen(onBack: () -> Unit) {
    val colors = TexasWatchTheme.colors
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var filters by remember { mutableStateOf(defaultFilters()) }

    BackHandler { onBack() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.mainBackground)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .imePadding(),
    ) {
        MainHeaderSearchBar(
            searchValue = searchQuery,
            onSearchValueChange = { searchQuery = it },
            onClose = onBack,
            onClear = { searchQuery = "" },
        )
        HorizontalDivider(thickness = 1.dp, color = colors.strokePale)

        // Scrollable body
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 12.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            FilterPanel(
                filters = filters,
                onToggle = { toggled ->
                    filters = filters.map {
                        if (it.group == toggled.group && it.label == toggled.label)
                            it.copy(selected = !it.selected)
                        else it
                    }
                },
            )
        }

        // ── Bottom action button ───────────────────────────────────────────────
        val selectedCount = filters.count { it.selected }
        val buttonLabel = if (selectedCount == 0 && searchQuery.isEmpty()) {
            "Search all offenders"
        } else {
            "See matching offenders now!"
        }

        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(colors.primaryAccent)
                .clickable { /* TODO: trigger search */ },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = buttonLabel,
                style = TexasWatchTheme.typography.h4,
                color = colors.invertedText,
                maxLines = 1,
            )
        }
    }
}

// ── Filter panel — collapsible "Filter by tags" card ─────────────────────────

@Composable
private fun FilterPanel(
    filters: List<FilterChip>,
    onToggle: (FilterChip) -> Unit,
) {
    val colors = TexasWatchTheme.colors
    val typography = TexasWatchTheme.typography
    var expanded by rememberSaveable { mutableStateOf(true) }
    val chevronRotation by animateFloatAsState(
        targetValue = if (expanded) 0f else 180f,
        label = "chevron",
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.surfaceBackground),
    ) {
        // ── Header row ────────────────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 14.dp),
        ) {
            Text(
                text = "Filter by tags",
                style = typography.h4,
                color = colors.primaryText,
            )
            Spacer(Modifier.size(8.dp))
            Icon(
                painter = painterResource(R.drawable.arrow_left_24),
                contentDescription = if (expanded) "Collapse" else "Expand",
                tint = colors.primaryText,
                modifier = Modifier
                    .size(18.dp)
                    .rotate(chevronRotation - 90f),
            )
        }

        // ── Collapsible content ────────────────────────────────────────────────
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) + expandVertically(),
            exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.Top),
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(start = 12.dp, end = 12.dp, bottom = 16.dp),
            ) {
                /// I Want to add a dropmdwn nto select counties can you add for me


                FilterGroup(
                    title = "Offender Level",
                    chips = filters.filter { it.group == FilterGroup.OffenderLevel },
                    onToggle = onToggle,
                )
                FilterGroup(
                    title = "Race",
                    chips = filters.filter { it.group == FilterGroup.Race },
                    onToggle = onToggle,
                )
                FilterGroup(
                    title = "Hair Color",
                    chips = filters.filter { it.group == FilterGroup.HairColor },
                    onToggle = onToggle,
                )
                FilterGroup(
                    title = "Eye Color",
                    chips = filters.filter { it.group == FilterGroup.EyeColor },
                    onToggle = onToggle,
                )
            }
        }
    }
}

// ── Filter group — labelled section with FlowRow of chips ────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FilterGroup(
    title: String,
    chips: List<FilterChip>,
    onToggle: (FilterChip) -> Unit,
) {
    val colors = TexasWatchTheme.colors
    val typography = TexasWatchTheme.typography

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = title,
            style = typography.text2,
            color = colors.secondaryText,
        )
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            chips.forEach { chip ->
                FilterChipTag(chip = chip, onToggle = { onToggle(chip) })
            }
        }
    }
}

// ── Single filter chip tag ────────────────────────────────────────────────────

private val ChipShape = RoundedCornerShape(8.dp)

@Composable
private fun FilterChipTag(
    chip: FilterChip,
    onToggle: () -> Unit,
) {
    val colors = TexasWatchTheme.colors

    val bgColor by animateColorAsState(
        targetValue = if (chip.selected) colors.primaryAccent else colors.mainBackground,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "chipBg",
    )
    val textColor by animateColorAsState(
        targetValue = if (chip.selected) colors.invertedText else colors.primaryText,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "chipText",
    )
    val borderColor by animateColorAsState(
        targetValue = if (chip.selected) colors.primaryAccent else colors.strokeFull,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "chipBorder",
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .clip(ChipShape)
            .border(1.dp, borderColor, ChipShape)
            .background(bgColor)
            .clickable(onClick = onToggle)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(
            text = chip.label,
            style = TexasWatchTheme.typography.text2,
            color = textColor,
        )
    }
}
