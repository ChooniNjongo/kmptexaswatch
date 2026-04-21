package com.jetbrains.spacetutorial.texaswatch.cache

import com.jetbrains.spacetutorial.texaswatch.entity.OffenderSummary
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

private const val NEARBY_TTL_SECONDS = 600L // 10 minutes

@OptIn(ExperimentalTime::class)
private fun currentEpochSeconds(): Long = Clock.System.now().epochSeconds

internal class TexasWatchCache(driverFactory: TexasWatchDriverFactory) {

    private val database = TexasWatchDatabase(driverFactory.createDriver())
    private val dbQuery = database.texasWatchDatabaseQueries

    internal fun getAllOffenders(): List<OffenderSummary> {
        return dbQuery.selectAllOffenders().executeAsList().map { row ->
            OffenderSummary(
                indIdn      = row.indIdn.toInt(),
                dpsNumber   = row.dpsNumber,
                firstName   = row.firstName,
                lastName    = row.lastName,
                fullName    = row.fullName,
                photoUrl    = row.photoUrl,
                address     = row.address,
                age         = row.age?.toInt(),
                detailsUrl  = row.detailsUrl
            )
        }
    }

    internal fun clearAndInsert(offenders: List<OffenderSummary>) {
        dbQuery.transaction {
            dbQuery.removeAllOffenders()
            offenders.forEach { o ->
                dbQuery.insertOffender(
                    indIdn     = o.indIdn.toLong(),
                    dpsNumber  = o.dpsNumber,
                    firstName  = o.firstName,
                    lastName   = o.lastName,
                    fullName   = o.fullName,
                    photoUrl   = o.photoUrl,
                    address    = o.address,
                    age        = o.age?.toLong(),
                    detailsUrl = o.detailsUrl
                )
            }
        }
    }

    internal fun count(): Long = dbQuery.countOffenders().executeAsOne()

    // ── Nearby cache ──────────────────────────────────────────────────────────

    internal fun getNearby(cacheKey: String): Pair<Int, List<OffenderSummary>>? {
        val meta = dbQuery.selectNearbyMeta(cacheKey).executeAsOneOrNull() ?: return null
        val nowSeconds = currentEpochSeconds()
        if (nowSeconds - meta.cachedAt > NEARBY_TTL_SECONDS) return null
        val offenders = dbQuery.selectNearbyOffenders(cacheKey).executeAsList().map { row ->
            OffenderSummary(
                indIdn     = row.indIdn.toInt(),
                dpsNumber  = row.dpsNumber,
                firstName  = row.firstName,
                lastName   = row.lastName,
                fullName   = row.fullName,
                photoUrl   = row.photoUrl,
                address    = row.address,
                age        = row.age?.toInt(),
                detailsUrl = row.detailsUrl,
                lat        = row.lat,
                lon        = row.lon,
            )
        }
        return Pair(meta.totalCount.toInt(), offenders)
    }

    internal fun saveNearby(cacheKey: String, totalCount: Int, offenders: List<OffenderSummary>) {
        dbQuery.transaction {
            dbQuery.deleteNearbyOffenders(cacheKey)
            dbQuery.deleteNearbyMeta(cacheKey)
            dbQuery.insertNearbyMeta(cacheKey, totalCount.toLong(), currentEpochSeconds())
            offenders.forEach { o ->
                dbQuery.insertNearbyOffender(
                    cacheKey   = cacheKey,
                    indIdn     = o.indIdn.toLong(),
                    dpsNumber  = o.dpsNumber,
                    firstName  = o.firstName,
                    lastName   = o.lastName,
                    fullName   = o.fullName,
                    photoUrl   = o.photoUrl,
                    address    = o.address,
                    age        = o.age?.toLong(),
                    detailsUrl = o.detailsUrl,
                    lat        = o.lat,
                    lon        = o.lon,
                )
            }
        }
    }
}
