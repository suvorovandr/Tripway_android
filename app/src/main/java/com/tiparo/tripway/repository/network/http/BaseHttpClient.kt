package com.tiparo.tripway.repository.network.http

import android.app.Application
import com.tiparo.tripway.utils.LiveDataCallAdapterFactory
import com.google.firebase.auth.FirebaseAuth
import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import com.tiparo.tripway.repository.network.api.HEADER_ACCEPT_LANGUAGE
import com.tiparo.tripway.repository.network.api.SET_TOKEN
import com.tiparo.tripway.utils.ApiInvocationException
import com.tiparo.tripway.utils.LocaleUtil
import com.tiparo.tripway.utils.NullOnEmptyConverterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber

class BaseHttpClient constructor(
    private val baseURL: String,
    private val application: Application
) : HttpClient {

    private val okHttpClient: OkHttpClient
    private val retrofit: Retrofit

    init {
        okHttpClient = createOkHttpClient()
        retrofit = createRetrofit()
    }

    override fun <T> getApiService(apiServiceClass: Class<T>): T {
        return retrofit.create(apiServiceClass)
    }

    override fun getRetrofit(): Retrofit {
        return retrofit
    }

    private fun createOkHttpClient() = OkHttpClient.Builder()
        .addInterceptor(TokenInterceptor(application))
        .addInterceptor(GeneralResponseInterceptor(application))
        .build()


    private fun createRetrofit() =
        Retrofit.Builder()
            .baseUrl(baseURL)
            .addConverterFactory(NullOnEmptyConverterFactory)
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .addCallAdapterFactory(LiveDataCallAdapterFactory())
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()


    /**
     * Set, add authorization token using FirebaseAuth
     */

    class TokenInterceptor(val application: Application) : Interceptor {

        override fun intercept(chain: Interceptor.Chain): Response {
            var request = chain.request()
            val requestBuilder = request.newBuilder()

            try {
                FirebaseAuth.getInstance().currentUser?.getIdToken(false)?.result?.token?.let { token ->
                    requestBuilder.addHeader(SET_TOKEN, token)
                } ?: throw ApiInvocationException(1000)
            } catch (e: Exception) {
                Timber.e(e, "Cant receive token from FirebaseAuth")
            }

            requestBuilder.addHeader(HEADER_ACCEPT_LANGUAGE, LocaleUtil.getLanguage())

            val response = chain.proceed(requestBuilder.build())

            return response
        }
    }

    class GeneralResponseInterceptor(val application: Application) : Interceptor {

        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            val requestBuilder = request.newBuilder()
            val response = chain.proceed(requestBuilder.build())

            if (!response.isSuccessful || response.code == 204) {
                throw ApiInvocationException(response.code, response.body?.string())
            }
            return response
        }
    }
}