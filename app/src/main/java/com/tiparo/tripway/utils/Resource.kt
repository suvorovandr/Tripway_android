package com.tiparo.tripway.utils

import com.tiparo.tripway.repository.network.api.ErrorDescription

data class Resource<out T>(
    val status: Status,
    val data: T?,
    val message: String? = null,
    val code: Int? = null,
    val params: Map<String, String?>? = null
) {
    enum class Status {
        SUCCESS, ERROR, LOADING
    }

    companion object {
        fun <T> success(data: T?): Resource<T> {
            return Resource(
                Status.SUCCESS,
                data
            )
        }

        fun <T> error(data: T?, errorDescription: ErrorDescription): Resource<T> {
            return Resource(
                Status.ERROR,
                data,
                errorDescription.message,
                errorDescription.code,
                errorDescription.params
            )
        }

        fun <T> loading(data: T?): Resource<T> {
            return Resource(
                Status.LOADING,
                data
            )
        }
    }
}