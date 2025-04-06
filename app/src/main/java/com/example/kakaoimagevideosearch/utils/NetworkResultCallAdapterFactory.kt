package com.example.kakaoimagevideosearch.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Retrofit
import java.io.IOException
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type


class NetworkResultCallAdapter<R>(
    private val responseType: Type
) : CallAdapter<R, Flow<NetworkResult<R>>> {

    override fun responseType() = responseType

    override fun adapt(call: Call<R>): Flow<NetworkResult<R>> = flow {
        try {
            val response = call.execute()
            if (response.isSuccessful) {
                response.body()?.let { body ->
                    emit(NetworkResult.Success(body))
                } ?: emit(NetworkResult.Error(response.code(), "응답 본문이 비어있음"))
            } else {
                val errorBody = response.errorBody()?.string() ?: "알 수 없는 에러"
                emit(NetworkResult.Error(response.code(), errorBody))
            }
        } catch (e: IOException) {
            emit(NetworkResult.NetworkError)
        }
    }.flowOn(Dispatchers.IO)
}

class NetworkResultCallAdapterFactory : CallAdapter.Factory() {
    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        if (getRawType(returnType) != Flow::class.java) return null
        val flowType = getParameterUpperBound(0, returnType as ParameterizedType)
        val responseType = getParameterUpperBound(0, flowType as ParameterizedType)
        return NetworkResultCallAdapter<Any>(responseType)
    }
}