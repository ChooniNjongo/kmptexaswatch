package com.jetbrains.spacetutorial

import com.jetbrains.spacetutorial.texaswatch.TexasWatchSDK
import com.jetbrains.spacetutorial.texaswatch.cache.AndroidTexasWatchDriverFactory
import com.jetbrains.spacetutorial.texaswatch.network.TexasWatchApi
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<TexasWatchApi> { TexasWatchApi(baseUrl = "http://10.0.2.2:8080") }
    single<TexasWatchSDK> {
        TexasWatchSDK(
            driverFactory = AndroidTexasWatchDriverFactory(androidContext()),
            api = get()
        )
    }
    viewModel { OffenderListViewModel(sdk = get()) }
    viewModel { NearbyOffendersViewModel(application = get(), sdk = get()) }
}
