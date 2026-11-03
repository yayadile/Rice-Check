package com.yayadile.ricecheck.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Version 1, exportSchema = true (schemas are written to app/schemas by KSP).
 * The instance is created once in AppContainer.
 */
@Database(entities = [ScanResultEntity::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scanDao(): ScanDao
}
