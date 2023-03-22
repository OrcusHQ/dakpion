package com.orcuspay.dakpion.data.remote

sealed class ApiResult<T>(val data: T? = null, val message: String? = null) {
    class Success<T>(data: T?) : ApiResult<T>(data)
    class Error<T>(
        exception: Exception?,
        message: String?, data: T? = null
    ) : ApiResult<T>(data, message)
}