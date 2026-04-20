package com.jetbrains.spacetutorial.texaswatch.cache

import app.cash.sqldelight.db.SqlDriver

interface TexasWatchDriverFactory {
    fun createDriver(): SqlDriver
}
