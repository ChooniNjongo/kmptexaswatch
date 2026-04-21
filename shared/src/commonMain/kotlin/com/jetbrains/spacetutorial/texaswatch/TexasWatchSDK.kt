package com.jetbrains.spacetutorial.texaswatch

import com.jetbrains.spacetutorial.texaswatch.cache.TexasWatchCache
import com.jetbrains.spacetutorial.texaswatch.cache.TexasWatchDriverFactory
import com.jetbrains.spacetutorial.texaswatch.entity.MapOffender
import com.jetbrains.spacetutorial.texaswatch.entity.MapOffenderResponse
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
    suspend fun getOffendersByRadius(
        lat: Double, lon: Double, radiusMiles: Double, page: Int = 0, size: Int = 20
    ): OffenderSearchResponse {
        return api.getOffendersByRadius(lat, lon, radiusMiles, page, size)
    }

    @Throws(Exception::class)
    suspend fun getRiskStats(lat: Double, lon: Double, radiusMiles: Double = 5.0): RiskStats {
        return api.getRiskStats(lat, lon, radiusMiles)
    }

    /**
     * Returns a [NearbyResult] for the given location/radius.
     * If [forceReload] is false and a fresh cache entry exists, returns it immediately.
     * Otherwise fetches from the network, saves to SQLDelight, and returns fresh data.
     */
    @Throws(Exception::class)
    suspend fun getNearby(
        lat: Double, lon: Double, radiusMiles: Double, forceReload: Boolean = false
    ): NearbyResult {
        val key = nearbyKey(lat, lon, radiusMiles)
        if (!forceReload) {
            val cached = cache.getNearby(key)
            if (cached != null) return NearbyResult(cached.first, cached.second, fromCache = true)
        }
        val mapResult = api.getOffendersByRadiusForMap(lat, lon, radiusMiles, page = 0, size = 20)
        val total = mapResult.totalElements.toInt()
        val offenders = mapResult.content.map { m ->
            OffenderSummary(
                indIdn     = m.indIdn,
                dpsNumber  = m.dpsNumber,
                firstName  = m.fullName.substringBefore(" "),
                lastName   = m.fullName.substringAfterLast(" "),
                fullName   = m.fullName,
                photoUrl   = m.photoUrl,
                address    = m.address,
                age        = null,
                detailsUrl = "/api/offenders/${m.indIdn}",
                lat        = m.latitude,
                lon        = m.longitude,
            )
        }
        cache.saveNearby(key, total, offenders)
        return NearbyResult(total, offenders, fromCache = false)
    }

    /**
     * Fetches a single page of nearby offenders sorted by distance.
     * Always hits the network — used for infinite scroll pagination.
     */
    @Throws(Exception::class)
    suspend fun getOffendersPage(
        lat: Double, lon: Double, radiusMiles: Double, page: Int, size: Int
    ): PagedNearbyResult {
        val mapResult = api.getOffendersByRadiusForMap(lat, lon, radiusMiles, page = page, size = size)
        val offenders = mapResult.content.map { it.toOffenderSummary() }
        return PagedNearbyResult(
            totalCount = mapResult.totalElements.toInt(),
            totalPages = mapResult.totalPages,
            offenders = offenders,
        )
    }
}

private fun String.toTitleCase(): String =
    split(" ").joinToString(" ") { word ->
        word.lowercase().replaceFirstChar { it.uppercaseChar() }
    }

private fun MapOffender.toOffenderSummary(): OffenderSummary {
    val name = fullName.toTitleCase()
    return OffenderSummary(
        indIdn     = indIdn,
        dpsNumber  = dpsNumber,
        firstName  = name.substringBefore(" "),
        lastName   = name.substringAfterLast(" "),
        fullName   = name,
        photoUrl   = photoUrl,
        address    = address,
        age        = null,
        detailsUrl = "/api/offenders/$indIdn",
        lat        = latitude,
        lon        = longitude,
    )
}

data class PagedNearbyResult(
    val totalCount: Int,
    val totalPages: Int,
    val offenders: List<OffenderSummary>,
)

data class NearbyResult(
    val totalCount: Int,
    val offenders: List<OffenderSummary>,
    val fromCache: Boolean,
)

internal fun nearbyKey(lat: Double, lon: Double, radiusMiles: Double): String {
    // Round to 3 decimal places for lat/lon, 1 for radius — avoids JVM-only String.format
    fun Double.r3() = (this * 1000).toLong().toString().let { s ->
        val i = s.dropLast(3).ifEmpty { "0" }
        val d = s.takeLast(3).padStart(3, '0')
        "$i.$d"
    }
    fun Double.r1() = (this * 10).toLong().toString().let { s ->
        val i = s.dropLast(1).ifEmpty { "0" }
        val d = s.takeLast(1)
        "$i.$d"
    }
    return "${lat.r3()},${lon.r3()},${radiusMiles.r1()}"
}
