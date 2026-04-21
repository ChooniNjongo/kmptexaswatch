package com.jetbrains.spacetutorial.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.jetbrains.spacetutorial.R
import com.jetbrains.spacetutorial.texaswatch.theme.TexasWatchTheme

// ── MainHeaderTitleBar ────────────────────────────────────────────────────────

@Composable
fun MainHeaderTitleBar(
    title: String,
    modifier: Modifier = Modifier,
    startContent: @Composable RowScope.() -> Unit = {},
    endContent: @Composable RowScope.() -> Unit = {},
) {
    Box(
        modifier = modifier
            .height(48.dp)
            .fillMaxWidth()
            .background(TexasWatchTheme.colors.mainBackground),
        contentAlignment = Alignment.Center,
    ) {
        Row(Modifier.align(Alignment.CenterStart)) {
            startContent()
        }
        Text(
            text = title,
            style = TexasWatchTheme.typography.h3,
            color = TexasWatchTheme.colors.primaryText,
        )
        Row(Modifier.align(Alignment.CenterEnd)) {
            endContent()
        }
    }
}

// ── Header state machine (mirrors KotlinConf MainHeaderContainerState) ────────

enum class MainHeaderContainerState { Title, Search }

// ── MainHeaderContainer — AnimatedContent switcher ───────────────────────────

@Composable
fun MainHeaderContainer(
    state: MainHeaderContainerState,
    modifier: Modifier = Modifier,
    titleContent: @Composable () -> Unit = {},
    searchContent: @Composable () -> Unit = {},
) {
    AnimatedContent(
        targetState = state,
        transitionSpec = {
            (fadeIn(tween(50)) + slideIntoContainer(SlideDirection.Down)) togetherWith
                    (fadeOut(tween(50)) + slideOutOfContainer(SlideDirection.Up))
        },
        modifier = modifier
            .height(48.dp)
            .fillMaxWidth()
            .background(TexasWatchTheme.colors.mainBackground),
        label = "HeaderState",
    ) { target ->
        when (target) {
            MainHeaderContainerState.Title -> titleContent()
            MainHeaderContainerState.Search -> searchContent()
        }
    }
}

// ── TopMenuButton — clickable icon button ─────────────────────────────────────

@Composable
fun TopMenuButton(
    iconRes: Int,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TexasWatchTheme.colors
    Icon(
        painter = painterResource(iconRes),
        contentDescription = contentDescription,
        tint = colors.primaryText,
        modifier = modifier
            .padding(6.dp)
            .size(36.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            .padding(6.dp),
    )
}

// ── MainHeaderSearchBar ───────────────────────────────────────────────────────

@Composable
fun MainHeaderSearchBar(
    searchValue: String,
    onSearchValueChange: (String) -> Unit,
    onClose: () -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = TexasWatchTheme.colors

    Row(
        modifier = modifier
            .height(48.dp)
            .fillMaxWidth()
            .background(colors.mainBackground),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        TopMenuButton(
            iconRes = R.drawable.arrow_left_24,
            contentDescription = "Back",
            onClick = {
                onClose()
                onSearchValueChange("")
            },
        )

        var focusRequested by rememberSaveable { mutableStateOf(false) }
        val focusRequester = remember { FocusRequester() }
        if (!focusRequested) {
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
                focusRequested = true
            }
        }

        SearchInput(
            searchValue = searchValue,
            onSearchValueChange = onSearchValueChange,
            hint = "Search offenders...",
            focusRequester = focusRequester,
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        )

        AnimatedVisibility(
            visible = searchValue.isNotEmpty(),
            enter = fadeIn(tween(200)),
            exit = fadeOut(tween(100)),
        ) {
            TopMenuButton(
                iconRes = R.drawable.close_24,
                contentDescription = "Clear",
                onClick = {
                    onSearchValueChange("")
                    onClear()
                    focusRequester.requestFocus()
                },
            )
        }
    }
}

// ── SearchInput — BasicTextField with animated hint ───────────────────────────

@Composable
internal fun SearchInput(
    searchValue: String,
    onSearchValueChange: (String) -> Unit,
    hint: String,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    val colors = TexasWatchTheme.colors
    val typography = TexasWatchTheme.typography

    Box(
        modifier = modifier,
        contentAlignment = Alignment.CenterStart,
    ) {
        BasicTextField(
            value = searchValue,
            onValueChange = onSearchValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester),
            singleLine = true,
            textStyle = typography.text1.copy(color = colors.primaryText),
            cursorBrush = SolidColor(colors.primaryText),
        )
        AnimatedVisibility(
            visible = searchValue.isEmpty(),
            enter = fadeIn(tween(10)),
            exit = fadeOut(tween(10)),
        ) {
            Text(
                text = hint,
                style = typography.text1,
                color = colors.secondaryText,
            )
        }
    }
}
