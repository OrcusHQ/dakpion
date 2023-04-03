package com.orcuspay.dakpion.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.orcuspay.dakpion.data.exception.DuplicateRequestException
import com.orcuspay.dakpion.data.exception.InternalServerException
import com.orcuspay.dakpion.data.exception.InvalidCredentialException
import com.orcuspay.dakpion.data.local.CredentialEntity
import com.orcuspay.dakpion.data.local.DakpionDatabase
import com.orcuspay.dakpion.data.mapper.*
import com.orcuspay.dakpion.data.remote.ApiResult
import com.orcuspay.dakpion.data.remote.DakpionApi
import com.orcuspay.dakpion.domain.model.*
import com.orcuspay.dakpion.domain.repository.DakpionRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DakpionRepositoryImp @Inject constructor(
    private val api: DakpionApi,
    private val db: DakpionDatabase,
) : DakpionRepository {

    private val dao = db.dao

    override suspend fun verify(verifyRequest: VerifyRequest): ApiResult<VerifyResponse> {

        val result = api.verify(verifyRequest.toVerifyRequestDto())
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
                    message = InternalServerException().message
                )
            }
        } else {
            ApiResult.Error(
                message = result.exceptionOrNull()?.message,
                exception = result.exceptionOrNull() as Exception
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

        val result = api.send(sendMessageRequest.toSendMessageRequestDto())

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
                    message = InternalServerException().message
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
                    return ApiResult.Error(exception = exception)
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
                message = result.exceptionOrNull()?.message,
                exception = result.exceptionOrNull() as Exception
            )
        }
    }

    override suspend fun syncCredentials() {
        val credentials = getCredentials()

        credentials.forEach { credential ->
            val result = api.verify(
                VerifyRequest(
                    accessKey = credential.accessKey,
                    secretKey = credential.secretKey,
                ).toVerifyRequestDto()
            )

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
        return Transformations.map(dao.getCredentialsLiveData()) {
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
        return Transformations.map(dao.getCredentialsWithSMSLiveData()) {
            it.map { csms ->
                csms.toCredentialWithSMS()
            }
        }
    }
}