package com.orcuspay.dakpion.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.orcuspay.dakpion.data.exception.DuplicateRequestException
import com.orcuspay.dakpion.data.exception.InternalServerException
import com.orcuspay.dakpion.data.exception.InvalidCredentialException
import com.orcuspay.dakpion.data.local.CredentialEntity
import com.orcuspay.dakpion.data.local.DakpionDatabase
import com.orcuspay.dakpion.data.local.SenderStat
import com.orcuspay.dakpion.data.local.StatusStat
import com.orcuspay.dakpion.data.mapper.*
import com.orcuspay.dakpion.data.remote.ApiResult
import com.orcuspay.dakpion.data.remote.DakpionApi
import com.orcuspay.dakpion.domain.model.*
import com.orcuspay.dakpion.domain.repository.DakpionRepository
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DakpionRepositoryImp @Inject constructor(
    private val api: DakpionApi,
    private val db: DakpionDatabase,
) : DakpionRepository {

    private val dao = db.dao

    private fun getErrorMessage(e: Throwable?): String {
        return when (e) {
            is HttpException -> {
                when (e.code()) {
                    502 -> "Server is temporarily unavailable (502). Please try again later."
                    500 -> "Internal Server Error (500). Please contact support."
                    404 -> "Service not found (404)."
                    else -> "Network error: ${e.code()} ${e.message()}"
                }
            }
            is IOException -> "Network timeout or connection issue. Please check your internet."
            null -> "An unknown error occurred."
            else -> e.message ?: "An unexpected error occurred."
        }
    }

    override suspend fun verify(verifyRequest: VerifyRequest): ApiResult<VerifyResponse> {
        val result = try {
            api.verify(verifyRequest.toVerifyRequestDto())
        } catch (e: Exception) {
            return ApiResult.Error(
                message = getErrorMessage(e),
                exception = e
            )
        }

        val credentials = dao.getCredentials()

        return if (result.isSuccess) {
            val response = result.getOrNull()
            val verifyResponse = response?.toVerifyResponse()
            if (verifyResponse != null) {

                if (credentials.any {
                        it.credentialId == verifyResponse.id && it.mode == verifyResponse.mode
                    }
                ) {
                    ApiResult.Error(
                        exception = InternalServerException(),
                        message = "This business is already added"
                    )
                } else {
                    dao.createCredential(
                        CredentialEntity(
                            accessKey = verifyRequest.accessKey,
                            secretKey = verifyRequest.secretKey,
                            mode = verifyResponse.mode,
                            credentialId = verifyResponse.id,
                            businessName = verifyResponse.name,
                            enabled = true,
                            icon = verifyResponse.icon,
                            unauthorized = false,
                        )
                    )
                    ApiResult.Success(data = response.toVerifyResponse())
                }

            } else {
                ApiResult.Error(
                    exception = InternalServerException(),
                    message = "Invalid response from server"
                )
            }
        } else {
            val exception = result.exceptionOrNull()
            ApiResult.Error(
                message = getErrorMessage(exception),
                exception = exception as? Exception ?: Exception(getErrorMessage(exception))
            )
        }
    }

    override suspend fun send(credential: Credential, sms: SMS): ApiResult<SendMessageResponse> {

        val sendMessageRequest = SendMessageRequest(
            accessKey = credential.accessKey,
            secretKey = credential.secretKey,
            mode = credential.mode,
            senderId = sms.sender,
            body = sms.body
        )

        if (sms.status != SMSStatus.PROCESSING) {
            dao.updateSMS(
                smsEntity = sms.copy(
                    status = SMSStatus.PROCESSING
                ).toSMSEntity()
            )
        }

        val result = try {
            api.send(sendMessageRequest.toSendMessageRequestDto())
        } catch (e: Exception) {
            dao.updateSMS(
                smsEntity = sms.copy(
                    status = SMSStatus.ERROR
                ).toSMSEntity()
            )
            return ApiResult.Error(
                message = getErrorMessage(e),
                exception = e
            )
        }

        if (result.isSuccess) {
            val response = result.getOrNull()
            val verifyResponse = response?.toSendMessageResponse()
            if (verifyResponse != null) {
                if (verifyResponse.stored) {
                    dao.updateSMS(
                        smsEntity = sms.copy(
                            status = SMSStatus.STORED
                        ).toSMSEntity()
                    )
                } else {
                    dao.updateSMS(
                        smsEntity = sms.copy(
                            status = SMSStatus.NOT_STORED
                        ).toSMSEntity()
                    )
                }
                return ApiResult.Success(data = response.toSendMessageResponse())
            } else {
                dao.updateSMS(
                    smsEntity = sms.copy(
                        status = SMSStatus.ERROR
                    ).toSMSEntity()
                )
                return ApiResult.Error(
                    exception = InternalServerException(),
                    message = "Invalid response from server"
                )
            }
        } else {
            val exception = result.exceptionOrNull()
            when (exception) {
                is DuplicateRequestException -> {
                    dao.updateSMS(
                        smsEntity = sms.copy(
                            status = SMSStatus.DUPLICATE
                        ).toSMSEntity()
                    )
                    return ApiResult.Success(data = null)
                }
                is InvalidCredentialException -> {
                    dao.updateSMS(
                        smsEntity = sms.copy(
                            status = SMSStatus.UNAUTHORIZED
                        ).toSMSEntity()
                    )
                    dao.updateCredential(
                        credentialEntity = credential.copy(
                            unauthorized = true,
                            enabled = false
                        ).toCredentialEntity()
                    )
                    return ApiResult.Error(exception = exception, message = "Invalid credentials")
                }
                else -> {
                    dao.updateSMS(
                        smsEntity = sms.copy(
                            status = SMSStatus.ERROR
                        ).toSMSEntity()
                    )
                }
            }
            return ApiResult.Error(
                message = getErrorMessage(exception),
                exception = exception as? Exception ?: Exception(getErrorMessage(exception))
            )
        }
    }

    override suspend fun syncCredentials() {
        val credentials = getCredentials()

        credentials.forEach { credential ->
            val result = try {
                api.verify(
                    VerifyRequest(
                        accessKey = credential.accessKey,
                        secretKey = credential.secretKey,
                    ).toVerifyRequestDto()
                )
            } catch (e: Exception) {
                Result.failure(e)
            }

            if (result.isSuccess) {
                val response = result.getOrNull()
                val verifyResponse = response?.toVerifyResponse()
                if (verifyResponse != null) {
                    dao.updateCredential(
                        credentialEntity = credential.copy(
                            businessName = verifyResponse.name,
                            icon = verifyResponse.icon
                        ).toCredentialEntity()
                    )
                }
            } else {
                val error = result.exceptionOrNull()
                when (error) {
                    is InvalidCredentialException -> {
                        dao.updateCredential(
                            credentialEntity = credential.copy(
                                enabled = false,
                                unauthorized = true,
                            ).toCredentialEntity()
                        )
                    }
                    else -> {

                    }
                }
            }
        }
    }

    override fun getCredentialsLiveData(): LiveData<List<Credential>> {
        return dao.getCredentialsLiveData().map {
            it.map { ce ->
                ce.toCredential()
            }
        }
    }

    override suspend fun getCredentials(): List<Credential> {
        return dao.getCredentials().map {
            it.toCredential()
        }
    }

    override suspend fun deleteCredential(credential: Credential) {
        dao.deleteCredential(credential.toCredentialEntity())
    }

    override suspend fun setCredentialEnabled(credential: Credential, enabled: Boolean) {
        dao.updateCredential(credential.copy(enabled = enabled).toCredentialEntity())
    }

    override suspend fun getCredentialWithSMS(): List<CredentialWithSMS> {
        return dao.getCredentialsWithSMS().map { it.toCredentialWithSMS() }
    }

    override fun getCredentialWithSMSLiveData(): LiveData<List<CredentialWithSMS>> {
        return dao.getCredentialsWithSMSLiveData().map {
            it.map { csms ->
                csms.toCredentialWithSMS()
            }
        }
    }

    override fun getTotalPaymentsCount(): LiveData<Int> {
        return dao.getTotalPaymentsCount()
    }

    override fun getPaymentCountBySender(): LiveData<List<SenderStat>> {
        return dao.getPaymentCountBySender()
    }

    override fun getPaymentCountByStatus(): LiveData<List<StatusStat>> {
        return dao.getPaymentCountByStatus()
    }

    override fun getAllStoredSMS(): LiveData<List<SMS>> {
        return dao.getAllStoredSMS().map { entities ->
            entities.map { it.toSMS() }
        }
    }
}