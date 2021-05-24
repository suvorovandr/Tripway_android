package com.tiparo.tripway.utils

data class ErrorBody(var message: String? = null, var code: Int? = null, val type: ErrorType) {
    enum class ErrorType {
        GENERAL,
        NO_CONTENT,
        NO_INTERNET
    }

    companion object {
        fun fromException(error: Throwable?): ErrorBody =
            when (error) {
                is java.io.IOException -> {
                    ErrorBody(error.message, type = ErrorType.NO_INTERNET)
                }
                is ApiInvocationException -> {
                    fromServerException(error)
                }
                else -> ErrorBody(error?.message, type = ErrorType.GENERAL)
            }

        private fun fromServerException(exception: ApiInvocationException): ErrorBody =
            ErrorBody(exception.errorMessage, exception.errorCode, when (exception.errorCode) {
                204 -> ErrorType.NO_CONTENT
                else -> ErrorType.GENERAL
            })
    }
}