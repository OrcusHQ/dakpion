package com.orcuspay.dakpion.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [CredentialEntity::class],
    version = 1,
    exportSchema = true
)
abstract class DakpionDatabase : RoomDatabase() {
    abstract val dao: DakpionDao
}