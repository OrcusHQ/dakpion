package com.orcuspay.dakpion.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE

@Dao
interface DakpionDao {


    @Insert(onConflict = REPLACE)
    suspend fun createCredential(credentialEntity: CredentialEntity)

    @Query("SELECT * FROM credentialentity")
    fun getCredentials(): LiveData<List<CredentialEntity>>

    @Update
    suspend fun updateCredential(credentialEntity: CredentialEntity)

    @Delete
    suspend fun deleteCredential(credentialEntity: CredentialEntity)
}