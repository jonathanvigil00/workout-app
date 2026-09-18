package com.jvigil.hoofmode.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Adds the "last completed today" tracking used to hold the Home screen on a completed state until midnight. */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE schedules ADD COLUMN lastCompletionDate TEXT DEFAULT NULL")
        db.execSQL("ALTER TABLE schedules ADD COLUMN lastCompletionType TEXT DEFAULT NULL")
    }
}
