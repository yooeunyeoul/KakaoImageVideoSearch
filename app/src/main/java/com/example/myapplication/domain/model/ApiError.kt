package com.example.myapplication.domain.model

sealed class ApiError(val code: Int) {
    data object NetworkError : ApiError(NETWORK_ERROR_CODE)
    data class ServerError(val serverCode: Int) : ApiError(serverCode)
    data class UnknownError(val unknownCode: Int) : ApiError(unknownCode)

    companion object {
        const val NETWORK_ERROR_CODE = -1

        fun fromCode(code: Int): ApiError = when (code) {
            NETWORK_ERROR_CODE -> NetworkError
            in 500..599 -> ServerError(code)
            else -> UnknownError(code)
        }
    }
} 