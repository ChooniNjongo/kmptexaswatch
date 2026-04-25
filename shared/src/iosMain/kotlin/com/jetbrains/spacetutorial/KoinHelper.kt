package com.jetbrains.spacetutorial

import com.jetbrains.spacetutorial.onboarding.IOSOnboardingStorage
import com.jetbrains.spacetutorial.onboarding.OnboardingStorage
import com.jetbrains.spacetutorial.texaswatch.TexasWatchSDK
import com.jetbrains.spacetutorial.texaswatch.cache.IOSTexasWatchDriverFactory
import com.jetbrains.spacetutorial.texaswatch.NearbyResult
import com.jetbrains.spacetutorial.texaswatch.PagedNearbyResult
import com.jetbrains.spacetutorial.texaswatch.entity.ContactScanResponse
import com.jetbrains.spacetutorial.texaswatch.entity.MapOffender
import com.jetbrains.spacetutorial.texaswatch.entity.MapOffenderResponse
import com.jetbrains.spacetutorial.texaswatch.entity.OffenderDetail
import com.jetbrains.spacetutorial.texaswatch.entity.OffenderSearchResponse
import com.jetbrains.spacetutorial.texaswatch.entity.OffenderSummary
import com.jetbrains.spacetutorial.texaswatch.entity.RiskStats
import com.jetbrains.spacetutorial.texaswatch.network.TexasWatchApi
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.dsl.module

class TexasWatchHelper : KoinComponent {
    private val sdk: TexasWatchSDK by inject<TexasWatchSDK>()

    suspend fun getOffenders(forceReload: Boolean): List<OffenderSummary> {
        return sdk.getOffenders(forceReload = forceReload)
    }

    @Throws(Exception::class)
    suspend fun getOffendersByRadius(
        lat: Double, lon: Double, radiusMiles: Double, page: Int, size: Int
    ): OffenderSearchResponse {
        return sdk.getOffendersByRadius(lat, lon, radiusMiles, page, size)
    }

    @Throws(Exception::class)
    suspend fun getRiskStats(lat: Double, lon: Double, radiusMiles: Double): RiskStats {
        return sdk.getRiskStats(lat = lat, lon = lon, radiusMiles = radiusMiles)
    }

    @Throws(Exception::class)
    suspend fun getNearby(lat: Double, lon: Double, radiusMiles: Double, forceReload: Boolean): NearbyResult {
        return sdk.getNearby(lat = lat, lon = lon, radiusMiles = radiusMiles, forceReload = forceReload)
    }

    @Throws(Exception::class)
    suspend fun getOffendersPage(lat: Double, lon: Double, radiusMiles: Double, page: Int, size: Int): PagedNearbyResult {
        return sdk.getOffendersPage(lat = lat, lon = lon, radiusMiles = radiusMiles, page = page, size = size)
    }

    @Throws(Exception::class)
    suspend fun getOffendersForMap(
        lat: Double, lon: Double, radiusMiles: Double, page: Int, size: Int
    ): MapOffenderResponse {
        return sdk.getOffendersByRadiusForMap(lat, lon, radiusMiles, page, size)
    }

    @Throws(Exception::class)
    suspend fun getOffenderDetail(indIdn: Int): OffenderDetail {
        return sdk.getOffenderDetail(indIdn)
    }

    @Throws(Exception::class)
    suspend fun searchByContacts(names: List<String>): ContactScanResponse {
        return sdk.searchByContacts(names)
    }

    @Throws(Exception::class)
    suspend fun searchComprehensive(
        name: String?,
        countyName: String?,
        riskLevels: List<String>?,
        races: List<String>?,
        hairColors: List<String>?,
        eyeColors: List<String>?,
        page: Int,
        size: Int,
    ): OffenderSearchResponse {
        return sdk.searchComprehensive(
            name = name,
            countyName = countyName,
            riskLevels = riskLevels,
            races = races,
            hairColors = hairColors,
            eyeColors = eyeColors,
            page = page,
            size = size,
        )
    }
}

class OnboardingHelper : KoinComponent {
    private val storage: OnboardingStorage by inject<OnboardingStorage>()

    fun isOnboardingComplete(): Boolean = storage.isOnboardingComplete()
    fun completeOnboarding() = storage.setOnboardingComplete(true)
}

fun initKoin() {
    startKoin {
        modules(module {
            single<TexasWatchApi> { TexasWatchApi(baseUrl = "http://localhost:8080") }
            single<TexasWatchSDK> {
                TexasWatchSDK(driverFactory = IOSTexasWatchDriverFactory(), api = get())
            }
            single<OnboardingStorage> { IOSOnboardingStorage() }
        })
    }
}
