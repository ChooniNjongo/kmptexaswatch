package com.jetbrains.spacetutorial.texaswatch.network

import com.jetbrains.spacetutorial.texaswatch.entity.MapOffender
import com.jetbrains.spacetutorial.texaswatch.entity.MapOffenderResponse
import com.jetbrains.spacetutorial.texaswatch.entity.OffenderDetail
import com.jetbrains.spacetutorial.texaswatch.entity.OffenderSearchResponse
import com.jetbrains.spacetutorial.texaswatch.entity.RiskStats
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class TexasWatchApi(private val baseUrl: String = "http://localhost:8080") {

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                useAlternativeNames = false
            })
        }
    }

    // Paginated offender list
    suspend fun getOffenders(page: Int = 0, size: Int = 20): OffenderSearchResponse {
        return httpClient.get("$baseUrl/api/offenders") {
            parameter("page", page)
            parameter("size", size)
            parameter("sort", "indIdn")
        }.body()
    }

    // Search by name
    suspend fun searchByName(query: String, page: Int = 0, size: Int = 20): OffenderSearchResponse {
        return httpClient.get("$baseUrl/api/offenders/search-advanced-name") {
            parameter("q", query)
            parameter("page", page)
            parameter("size", size)
        }.body()
    }

    // Search by address
    suspend fun searchByAddress(address: String, page: Int = 0, size: Int = 20): OffenderSearchResponse {
        return httpClient.get("$baseUrl/api/offenders/search-advanced-address") {
            parameter("address", address)
            parameter("page", page)
            parameter("size", size)
        }.body()
    }

    // Offenders within radius (paginated)
    suspend fun getOffendersByRadius(
        lat: Double, lon: Double, radiusMiles: Double, page: Int = 0, size: Int = 20
    ): OffenderSearchResponse {
        return httpClient.get("$baseUrl/api/offenders/search-radius") {
            parameter("lat", lat)
            parameter("lon", lon)
            parameter("radiusMiles", radiusMiles)
            parameter("page", page)
            parameter("size", size)
        }.body()
    }

    // Offenders within radius with lat/lon coordinates (for distance display)
    suspend fun getOffendersByRadiusForMap(
        lat: Double, lon: Double, radiusMiles: Double, page: Int = 0, size: Int = 50
    ): MapOffenderResponse {
        return httpClient.get("$baseUrl/api/offenders/map-radius") {
            parameter("lat", lat)
            parameter("lon", lon)
            parameter("radiusMiles", radiusMiles)
            parameter("page", page)
            parameter("size", size)
        }.body()
    }

    // Risk stats for a location radius
    suspend fun getRiskStats(lat: Double, lon: Double, radiusMiles: Double = 5.0): RiskStats {
        return httpClient.get("$baseUrl/api/offenders/risk-stats-radius") {
            parameter("lat", lat)
            parameter("lon", lon)
            parameter("radiusMiles", radiusMiles)
        }.body()
    }

    // Full offender detail
    suspend fun getOffenderDetail(indIdn: Int): OffenderDetail {
        return httpClient.get("$baseUrl/api/offenders/$indIdn").body()
    }
}
