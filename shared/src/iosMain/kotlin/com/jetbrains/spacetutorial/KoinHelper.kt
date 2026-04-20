package com.jetbrains.spacetutorial

import com.jetbrains.spacetutorial.texaswatch.TexasWatchSDK
import com.jetbrains.spacetutorial.texaswatch.cache.IOSTexasWatchDriverFactory
import com.jetbrains.spacetutorial.texaswatch.entity.OffenderSummary
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
}

fun initKoin() {
    startKoin {
        modules(module {
            single<TexasWatchApi> { TexasWatchApi(baseUrl = "http://localhost:8080") }
            single<TexasWatchSDK> {
                TexasWatchSDK(driverFactory = IOSTexasWatchDriverFactory(), api = get())
            }
        })
    }
}
