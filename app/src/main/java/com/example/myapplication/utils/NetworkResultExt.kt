package com.example.myapplication.utils

import com.example.myapplication.domain.model.ApiError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

inline fun <T, R> Flow<NetworkResult<T>>.mapResult(
    crossinline transform: (T) -> R
): Flow<R> = map { result ->
    when (result) {
        is NetworkResult.Success -> transform(result.data)
        is NetworkResult.Error -> throw ApiException(result.code, result.message)
        is NetworkResult.NetworkError -> throw ApiException(ApiError.NETWORK_ERROR_CODE, "네트워크 연결 오류")
    }
} 