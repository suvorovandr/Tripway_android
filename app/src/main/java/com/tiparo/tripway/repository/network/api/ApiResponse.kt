package com.tiparo.tripway.repository.network.api

import retrofit2.Response

@Suppress("unused") // T is used in extending classes
sealed class ApiResponse<T> {
    companion object {
        fun <T> create(error: Throwable): ApiErrorResponse<T> {
            return ApiErrorResponse(ErrorDescription(error.message ?: "unknown error"))
        }

        fun <T> create(response: Response<T>): ApiResponse<T> {
            return if (response.isSuccessful) {
                val body = response.body()
                if (body == null || response.code() == 204) {
                    ApiEmptyResponse()
                } else {
                    ApiSuccessResponse(body)
                }
            } else {
                val msg = response.errorBody()?.string()
                val params = mutableMapOf<String, String?>()

                val errorMsg = if (msg.isNullOrEmpty()) {
                    response.message()
                } else {
                    msg
                }

                if (response.code() == 401) {
                    params[HEADER_SESSION_EXPIRED] = response.headers()[HEADER_SESSION_EXPIRED]
                }

                ApiErrorResponse(ErrorDescription(errorMsg, response.code(), params))
            }
        }
    }
}

/**
 * separate class for HTTP 204 responses so that we can make ApiSuccessResponse's body non-null.
 */
class ApiEmptyResponse<T> : ApiResponse<T>()

data class ApiSuccessResponse<T>(val body: T) : ApiResponse<T>()

data class ApiErrorResponse<T>(val errorDescription: ErrorDescription) : ApiResponse<T>()

data class ErrorDescription(
    val message: String,
    val code: Int? = null,
    val params: Map<String, String?>? = null
)