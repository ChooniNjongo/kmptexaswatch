package com.jetbrains.spacetutorial

import com.jetbrains.spacetutorial.texaswatch.TexasWatchSDK
import com.jetbrains.spacetutorial.texaswatch.cache.IOSTexasWatchDriverFactory
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
    suspend fun getRiskStats(lat: Double, lon: Double, radiusMiles: Double): RiskStats {
        return sdk.getRiskStats(lat = lat, lon = lon, radiusMiles = radiusMiles)
    }
}

fun initKoin() {
    startKoin {
        modules(module {
            single<TexasWatchApi> { TexasWatchApi(baseUrl = "http://192.168.1.141:8080") }
            single<TexasWatchSDK> {
                TexasWatchSDK(driverFactory = IOSTexasWatchDriverFactory(), api = get())
            }
        })
    }
}
