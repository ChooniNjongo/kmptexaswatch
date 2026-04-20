package com.jetbrains.spacetutorial.texaswatch.cache

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver

class IOSTexasWatchDriverFactory : TexasWatchDriverFactory {
    override fun createDriver(): SqlDriver {
        return NativeSqliteDriver(TexasWatchDatabase.Schema, "texaswatch.db")
    }
}
