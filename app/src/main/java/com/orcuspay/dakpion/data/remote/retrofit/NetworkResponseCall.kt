package com.orcuspay.dakpion.data.remote.retrofit

import com.orcuspay.dakpion.data.exception.*
import okhttp3.Request
import okhttp3.ResponseBody
import okio.IOException
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Converter
import retrofit2.Response

internal class NetworkResponseCall<T>(
    private val delegate: Call<T>,
) : Call<Result<T?>> {

    override fun enqueue(callback: Callback<Result<T?>>) {
        return delegate.enqueue(
            object : Callback<T> {
                override fun onResponse(call: Call<T>, response: Response<T>) {
                    val body = response.body()
                    val code = response.code()
                    val error = response.errorBody()

                    if (response.isSuccessful && code == 200) {
                        callback.onResponse(
                            this@NetworkResponseCall,
                            Response.success(Result.success(body))
                        )
                    } else {
                        val exception = when (code) {
                            201 -> UnsuccessfulRequestException()
                            400 -> InvalidRequestException()
                            401 -> InvalidCredentialException()
                            409 -> DuplicateRequestException()
                            else -> InternalServerException()
                        }
                        callback.onResponse(
                            this@NetworkResponseCall,
                            Response.success(Result.failure(exception))
                        )
                    }
                }

                override fun onFailure(call: Call<T>, throwable: Throwable) {
                    val exception = when (throwable) {
                        is IOException -> throwable
                        else -> InternalServerException()
                    }
                    callback.onResponse(
                        this@NetworkResponseCall,
                        Response.success(Result.failure(exception))
                    )
                }
            }
        )
    }

    override fun isExecuted() = delegate.isExecuted

    override fun clone() = NetworkResponseCall(delegate.clone())

    override fun isCanceled() = delegate.isCanceled

    override fun cancel() = delegate.cancel()

    override fun execute(): Response<Result<T?>> {
        val response = delegate.execute()
        val code = response.code()
        return if (response.isSuccessful && code == 200) {
            Response.success(Result.success(response.body()))
        } else {
            val exception = when (code) {
                201 -> UnsuccessfulRequestException()
                400 -> InvalidRequestException()
                401 -> InvalidCredentialException()
                409 -> DuplicateRequestException()
                else -> InternalServerException()
            }

            Response.success(Result.failure(exception))
        }
    }

    override fun request(): Request = delegate.request()

    override fun timeout(): Timeout = delegate.timeout()
}