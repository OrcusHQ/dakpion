package com.orcuspay.dakpion.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.OnConflictStrategy.REPLACE

@Dao
interface DakpionDao {


    @Insert(onConflict = REPLACE)
    suspend fun createCredential(credentialEntity: CredentialEntity)

    @Query("SELECT * FROM credentialentity")
    fun getCredentialsLiveData(): LiveData<List<CredentialEntity>>

    @Query("SELECT * FROM credentialentity")
    suspend fun getCredentials(): List<CredentialEntity>

    @Update
    suspend fun updateCredential(credentialEntity: CredentialEntity)

    @Delete
    suspend fun deleteCredential(credentialEntity: CredentialEntity)

    @Transaction
    @Query("SELECT * FROM credentialentity")
    suspend fun getCredentialsWithSMS(): List<CredentialEntitiesWithSMSEntities>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun createSMS(smsEntity: SMSEntity)

    @Update
    suspend fun updateSMS(smsEntity: SMSEntity)

}