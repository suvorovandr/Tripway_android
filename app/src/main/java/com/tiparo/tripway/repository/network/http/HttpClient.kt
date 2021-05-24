package com.tiparo.tripway.repository.network.http

import retrofit2.Retrofit

interface HttpClient {
    fun <T> getApiService(apiServiceClass: Class<T>): T
    fun getRetrofit(): Retrofit
}