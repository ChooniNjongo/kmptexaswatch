package com.jetbrains.spacetutorial

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.SubcomposeAsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.jetbrains.spacetutorial.navigation.OffenderDetailRoute
import com.jetbrains.spacetutorial.texaswatch.entity.OffenderDetail
import com.jetbrains.spacetutorial.texaswatch.theme.TexasWatchTheme
import com.jetbrains.spacetutorial.texaswatch.ui.InitialsAvatar
import com.jetbrains.spacetutorial.ui.MainHeaderTitleBar
import com.jetbrains.spacetutorial.ui.TopMenuButton
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun OffenderDetailScreen(indIdn: Int, distanceMiles: Double? = null, onBack: () -> Unit) {
    val colors = TexasWatchTheme.colors
    val typography = TexasWatchTheme.typography

    val viewModel: OffenderDetailViewModel = koinViewModel { parametersOf(indIdn) }
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.mainBackground),
    ) {
        // ── Header ────────────────────────────────────────────────────────────
        val title = when (val s = state) {
            is OffenderDetailState.Success -> s.detail.names?.baseName?.fullName ?: ""
            else -> ""
        }
        MainHeaderTitleBar(
            title = title,
            startContent = {
                TopMenuButton(
                    iconRes = R.drawable.arrow_left_24,
                    contentDescription = "Back",
                    onClick = onBack,
                )
            },
        )
        HorizontalDivider(thickness = 1.dp, color = colors.strokePale)

        when (val s = state) {
            is OffenderDetailState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = colors.ringActive)
                }
            }
            is OffenderDetailState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = s.message,
                            style = typography.text2,
                            color = colors.dangerText,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = { viewModel.load() }) {
                            Text("Retry", style = typography.text2)
                        }
                    }
                }
            }
            is OffenderDetailState.Success -> {
                OffenderDetailContent(detail = s.detail, distanceMiles = distanceMiles)
            }
        }
    }
}

@Composable
private fun OffenderDetailContent(detail: OffenderDetail, distanceMiles: Double? = null) {
    val colors = TexasWatchTheme.colors
    val typography = TexasWatchTheme.typography
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.height(24.dp))

        // ── Large photo ───────────────────────────────────────────────────────
        val photoUrl = detail.photos?.currentPhoto?.photoUrl
        val baseName = detail.names?.baseName
        val firstName = baseName?.firstName.orEmpty()
        val lastName = baseName?.lastName.orEmpty()
        val initials = buildString {
            firstName.trim().firstOrNull()?.uppercaseChar()?.let { append(it) }
            lastName.trim().firstOrNull()?.uppercaseChar()?.let { append(it) }
        }
        val photoShape = RoundedCornerShape(12.dp)
        val photoModifier = Modifier
            .widthIn(max = 300.dp)
            .aspectRatio(1f)
            .clip(photoShape)

        if (!photoUrl.isNullOrBlank()) {
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .data(photoUrl)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = photoModifier,
                contentScale = ContentScale.Crop,
                loading = { InitialsAvatar(initials = initials, modifier = Modifier.fillMaxSize()) },
                error = { InitialsAvatar(initials = initials, modifier = Modifier.fillMaxSize()) },
            )
        } else {
            InitialsAvatar(initials = initials, modifier = photoModifier)
        }

        Spacer(Modifier.height(24.dp))

        // ── Full name ─────────────────────────────────────────────────────────
        val fullName = baseName?.fullName.orEmpty()
        Text(
            text = fullName,
            style = typography.h2,
            color = colors.primaryText,
        )

        Spacer(Modifier.height(4.dp))

        // ── Subtitle: Age + DPS ───────────────────────────────────────────────
        val age = detail.birthInfo?.age
        val subtitle = buildString {
            if (age != null) append("Age $age  ·  ")
            append("DPS: ${detail.dpsNumber}")
        }
        Text(
            text = subtitle,
            style = typography.text2,
            color = colors.secondaryText,
        )

        Spacer(Modifier.height(24.dp))

        // ── Risk + Distance badges ────────────────────────────────────────────
        val riskLevel = detail.registryInfo?.riskLevel
        if (riskLevel != null || distanceMiles != null) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (riskLevel != null) {
                    val (badgeColor, badgeLabel) = when (riskLevel) {
                        "1" -> colors.successBadge to "LOW RISK"
                        "2" -> colors.neutralBadge to "MODERATE RISK"
                        "3" -> colors.dangerBadge to "HIGH RISK"
                        else -> colors.neutralBadge to riskLevel
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(badgeColor)
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = badgeLabel,
                            style = typography.label,
                            color = colors.invertedText,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
                if (distanceMiles != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(colors.primaryAccent)
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = "${"%.1f".format(distanceMiles)} mi away",
                            style = typography.label,
                            color = colors.invertedText,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // ── Physical Description ──────────────────────────────────────────────
        val phys = detail.physicalDescription
        if (phys != null) {
            Text(
                text = "Physical Description",
                style = typography.h4,
                color = colors.primaryText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
            )
            phys.raceDescription?.takeIf { it.isNotBlank() }?.let {
                PhysicalRow(label = "Race", value = it)
            }
            phys.hairColorDescription?.takeIf { it.isNotBlank() }?.let {
                PhysicalRow(label = "Hair Color", value = it)
            }
            phys.eyeColorDescription?.takeIf { it.isNotBlank() }?.let {
                PhysicalRow(label = "Eye Color", value = it)
            }
            phys.heightFormatted?.takeIf { it.isNotBlank() }?.let {
                PhysicalRow(label = "Height", value = it)
            }
            if (phys.weightPounds != null) {
                PhysicalRow(label = "Weight", value = "${phys.weightPounds} lbs")
            }
            Spacer(Modifier.height(16.dp))
        }

        // ── Offenses ──────────────────────────────────────────────────────────
        if (detail.offenses.isNotEmpty()) {
            HorizontalDivider(thickness = 1.dp, color = colors.strokePale)
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Offenses",
                style = typography.h4,
                color = colors.primaryText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                detail.offenses.forEach { offense ->
                    Column {
                        offense.offenseDescription?.takeIf { it.isNotBlank() }?.let {
                            Text(
                                text = it,
                                style = typography.h4,
                                color = colors.primaryText,
                            )
                        }
                        val offenseSubtitle = buildList {
                            offense.convictionDate?.let { add("Convicted: $it") }
                            offense.statute?.let { add("Statute: $it") }
                        }.joinToString("  ·  ")
                        if (offenseSubtitle.isNotBlank()) {
                            Text(
                                text = offenseSubtitle,
                                style = typography.text2,
                                color = colors.secondaryText,
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        // ── Known Addresses ───────────────────────────────────────────────────
        if (detail.addresses.isNotEmpty()) {
            HorizontalDivider(thickness = 1.dp, color = colors.strokePale)
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Known Addresses",
                style = typography.h4,
                color = colors.primaryText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                detail.addresses.forEach { address ->
                    Column {
                        address.fullAddress?.takeIf { it.isNotBlank() }?.let {
                            Text(
                                text = it,
                                style = typography.text2,
                                color = colors.primaryText,
                            )
                        }
                        val cityLine = buildList {
                            val cityState = listOfNotNull(address.city, address.state, address.zipCode)
                                .joinToString(", ")
                            if (cityState.isNotBlank()) add(cityState)
                            address.countyName?.let { add("$it County") }
                        }.joinToString("  ·  ")
                        if (cityLine.isNotBlank()) {
                            Text(
                                text = cityLine,
                                style = typography.text2,
                                color = colors.secondaryText,
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun PhysicalRow(label: String, value: String) {
    val colors = TexasWatchTheme.colors
    val typography = TexasWatchTheme.typography
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(text = label, style = typography.text2, color = colors.secondaryText)
        Text(text = value, style = typography.text2, color = colors.primaryText)
    }
}
