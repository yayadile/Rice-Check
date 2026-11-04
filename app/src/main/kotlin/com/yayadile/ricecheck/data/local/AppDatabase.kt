package com.yayadile.ricecheck.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Version 2 adds `bleachPct` (nullable, so scans saved before the update still load).
 * exportSchema = true (schemas are written to app/schemas by KSP).
 * The instance is created once in AppContainer.
 */
@Database(entities = [ScanResultEntity::class], version = 2, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scanDao(): ScanDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE scan_results ADD COLUMN bleachPct INTEGER")
            }
        }
    }
}
