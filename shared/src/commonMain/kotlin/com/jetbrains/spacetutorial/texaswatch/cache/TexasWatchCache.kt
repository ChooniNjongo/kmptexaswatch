package com.jetbrains.spacetutorial.texaswatch.cache

import com.jetbrains.spacetutorial.texaswatch.entity.OffenderSummary

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
}
