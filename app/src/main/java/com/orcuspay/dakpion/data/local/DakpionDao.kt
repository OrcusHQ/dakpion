package com.orcuspay.dakpion.data.local

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface DakpionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
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

    @Transaction
    @Query("SELECT * FROM credentialentity")
    fun getCredentialsWithSMSLiveData(): LiveData<List<CredentialEntitiesWithSMSEntities>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun createSMS(smsEntity: SMSEntity)

    @Update
    suspend fun updateSMS(smsEntity: SMSEntity)

    @Query("SELECT * FROM smsentity WHERE credentialId = :credentialId AND sender = :sender AND status = 'STORED' ORDER BY date DESC LIMIT 1")
    suspend fun getLastStoredSMS(credentialId: Int, sender: String): SMSEntity?

    // Analytics Queries
    @Query("SELECT COUNT(*) FROM smsentity WHERE status = 'STORED'")
    fun getTotalPaymentsCount(): LiveData<Int>

    @Query("SELECT sender, COUNT(*) as count FROM smsentity WHERE status = 'STORED' GROUP BY sender")
    fun getPaymentCountBySender(): LiveData<List<SenderStat>>

    @Query("SELECT status, COUNT(*) as count FROM smsentity GROUP BY status")
    fun getPaymentCountByStatus(): LiveData<List<StatusStat>>

    @Query("SELECT * FROM smsentity WHERE status = 'STORED' ORDER BY date DESC")
    fun getAllStoredSMS(): LiveData<List<SMSEntity>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun createFilter(filterEntity: FilterEntity)

    @Query("SELECT * FROM filterentity")
    suspend fun getFilters(): List<FilterEntity>

    @Query("SELECT * FROM filterentity WHERE enabled")
    suspend fun getEnabledFilters(): List<FilterEntity>

    @Query("SELECT * FROM filterentity")
    fun getFiltersLiveData(): LiveData<List<FilterEntity>>

    @Update
    suspend fun updateFilter(filterEntity: FilterEntity)

    @Delete
    suspend fun deleteFilter(filterEntity: FilterEntity)
}

data class SenderStat(
    val sender: String,
    val count: Int
)

data class StatusStat(
    val status: com.orcuspay.dakpion.domain.model.SMSStatus,
    val count: Int
)
