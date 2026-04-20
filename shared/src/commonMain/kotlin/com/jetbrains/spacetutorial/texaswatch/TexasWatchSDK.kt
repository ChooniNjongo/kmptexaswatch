package com.jetbrains.spacetutorial.texaswatch

import com.jetbrains.spacetutorial.texaswatch.cache.TexasWatchCache
import com.jetbrains.spacetutorial.texaswatch.cache.TexasWatchDriverFactory
import com.jetbrains.spacetutorial.texaswatch.entity.OffenderDetail
import com.jetbrains.spacetutorial.texaswatch.entity.OffenderSearchResponse
import com.jetbrains.spacetutorial.texaswatch.entity.OffenderSummary
import com.jetbrains.spacetutorial.texaswatch.entity.RiskStats
import com.jetbrains.spacetutorial.texaswatch.network.TexasWatchApi

class TexasWatchSDK(
    driverFactory: TexasWatchDriverFactory,
    private val api: TexasWatchApi
) {
    private val cache = TexasWatchCache(driverFactory)

    // Returns cached offenders or fetches fresh from API
    @Throws(Exception::class)
    suspend fun getOffenders(forceReload: Boolean = false): List<OffenderSummary> {
        val cached = cache.getAllOffenders()
        return if (cached.isNotEmpty() && !forceReload) {
            cached
        } else {
            val response = api.getOffenders(page = 0, size = 50)
            cache.clearAndInsert(response.content)
            response.content
        }
    }

    // Always hits the network — used for search
    @Throws(Exception::class)
    suspend fun searchByName(query: String, page: Int = 0): OffenderSearchResponse {
        return api.searchByName(query = query, page = page)
    }

    @Throws(Exception::class)
    suspend fun searchByAddress(address: String, page: Int = 0): OffenderSearchResponse {
        return api.searchByAddress(address = address, page = page)
    }

    @Throws(Exception::class)
    suspend fun getOffenderDetail(indIdn: Int): OffenderDetail {
        return api.getOffenderDetail(indIdn)
    }

    @Throws(Exception::class)
    suspend fun getRiskStats(lat: Double, lon: Double, radiusMiles: Double = 5.0): RiskStats {
        return api.getRiskStats(lat, lon, radiusMiles)
    }
}
