package com.orcuspay.dakpion.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CredentialEntity::class],
    version = 2,
    exportSchema = true,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ]
)
abstract class DakpionDatabase : RoomDatabase() {
    abstract val dao: DakpionDao
}