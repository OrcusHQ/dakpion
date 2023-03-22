package com.orcuspay.dakpion.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [CredentialEntity::class, SMSEntity::class],
    version = 1,
    exportSchema = true,
    autoMigrations = [

    ]
)
@TypeConverters(Converters::class)
abstract class DakpionDatabase : RoomDatabase() {
    abstract val dao: DakpionDao
}