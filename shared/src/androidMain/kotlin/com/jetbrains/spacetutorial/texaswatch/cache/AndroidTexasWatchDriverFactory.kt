package com.jetbrains.spacetutorial.texaswatch.cache

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver

class AndroidTexasWatchDriverFactory(private val context: Context) : TexasWatchDriverFactory {
    override fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(TexasWatchDatabase.Schema, context, "texaswatch.db")
    }
}
