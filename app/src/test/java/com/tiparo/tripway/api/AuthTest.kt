package com.tiparo.tripway.api

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.tiparo.tripway.repository.network.api.ApiErrorResponse
import com.tiparo.tripway.repository.network.api.HEADER_SESSION_EXPIRED
import com.tiparo.tripway.repository.network.api.services.TripsService
import com.tiparo.tripway.utils.NullOnEmptyConverterFactory
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import util.getOrAwaitValue
import java.net.HttpURLConnection

@RunWith(JUnit4::class)
class AuthTest {
    @Rule
    @JvmField
    val instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var service: TripsService

    private lateinit var mockWebServer: MockWebServer

    @Before
    fun createService() {
        mockWebServer = MockWebServer()
        service = Retrofit.Builder()
            .baseUrl(mockWebServer.url("/"))
            .addConverterFactory(NullOnEmptyConverterFactory)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(LiveDataCallAdapterFactory())
            .build()
            .create(TripsService::class.java)
    }

    @After
    fun stopService() {
        mockWebServer.shutdown()
    }

    @Test
    fun unauthorisedUser() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_UNAUTHORIZED)
                .setHeader(HEADER_SESSION_EXPIRED, false)
        )
        val error = (service.getOwnTrips().getOrAwaitValue() as ApiErrorResponse).errorDescription

        val request = mockWebServer.takeRequest()

        assertEquals(error.code, 401)
        assertNotNull(error.params?.get(HEADER_SESSION_EXPIRED))
        assertEquals(error.params?.get(HEADER_SESSION_EXPIRED), "false")
    }

    @Test
    fun sessionExpired() {
        mockWebServer.enqueue(
            MockResponse()
                .setResponseCode(HttpURLConnection.HTTP_UNAUTHORIZED)
                .setHeader(HEADER_SESSION_EXPIRED, true)
        )
        val error = (service.getOwnTrips().getOrAwaitValue() as ApiErrorResponse).errorDescription

        val request = mockWebServer.takeRequest()

        assertEquals(error.code, 401)
        assertNotNull(error.params?.get(HEADER_SESSION_EXPIRED))
        assertEquals(error.params?.get(HEADER_SESSION_EXPIRED), "true")
    }
}
