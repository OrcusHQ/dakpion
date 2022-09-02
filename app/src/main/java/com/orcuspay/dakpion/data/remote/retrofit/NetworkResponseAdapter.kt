package com.orcuspay.dakpion.data.remote.retrofit

import retrofit2.Call
import retrofit2.CallAdapter
import java.lang.reflect.Type

class NetworkResponseAdapter<T>(
    private val successType: Type,
) : CallAdapter<T, Call<Result<T?>>> {

    override fun responseType(): Type = successType

    override fun adapt(call: Call<T>): Call<Result<T?>> {
        return NetworkResponseCall(call)
    }
}