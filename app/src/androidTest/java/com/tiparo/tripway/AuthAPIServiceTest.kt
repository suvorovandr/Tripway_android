//package com.tiparo.tripway
//
//import android.content.Context
//import androidx.test.platform.app.InstrumentationRegistry
//import com.tiparo.tripway.repository.network.api.HEADER_AUTHORIZATION
//import com.tiparo.tripway.repository.network.api.services.AuthService
//import com.tiparo.tripway.repository.network.dto.ResourceErrorDAO
//import okhttp3.mockwebserver.MockResponse
//import okhttp3.mockwebserver.MockWebServer
//import org.junit.After
//import org.junit.Assert.assertEquals
//import org.junit.Before
//import org.junit.Test
//import java.net.HttpURLConnection
//
//
///**
// * Example local unit test, which will execute on the development machine (host).
// *
// * See [testing documentation](http://d.android.com/tools/testing).
// */
//class AuthAPIServiceTest {
//
//    private var mockWebServer = MockWebServer()
//
//    private lateinit var authService: AuthService
//
//    @Before
//    fun setup() {
//        mockWebServer.start()
//
//        authService = InjectorUtils.createRetrofit(mockWebServer.url("/"))
//            .create(AuthService::class.java)
//    }
//
//    @Test
//    fun authBackend_InvalidToken() {
//        // Assign
//        val responseServer = MockResponse()
//            .setResponseCode(HttpURLConnection.HTTP_INTERNAL_ERROR)
//            .setBody("""{"error":"INVALID_TOKEN","params":[]}""")
//
//        mockWebServer.enqueue(responseServer)
//
//        // Act
//        val response = authService.authBackend("INVALID_TOKEN_STRING").execute()
//        val resourceErrorDAO = ErrorUtils().parseError(response.errorBody(), response.code())
//
//        // Assert
//        assertEquals(
//            ResourceErrorDAO(
//                Error("INVALID_TOKEN", 500),
//                listOf()
//            ), resourceErrorDAO
//        )
//    }
//
//    @Test
//    fun authBackendSaveToken() {
//        // Assign
//        val sessionIDTest = "3489f3n-43f03n3-lkssffn8934f"
//        val responseServer = MockResponse()
//            .setResponseCode(HttpURLConnection.HTTP_OK)
//            .addHeader(HEADER_AUTHORIZATION, sessionIDTest)
//
//
//        mockWebServer.enqueue(responseServer)
//
//        // Act
//        val response = authService.authBackend("RANDOM_TOKEN").execute()
//
//        val context = InstrumentationRegistry.getInstrumentation().targetContext
//
//        val preferences =
//            context.getSharedPreferences(BaseApplication.APP_NAME, Context.MODE_PRIVATE)
//
//        val sessionID = preferences.getString(HEADER_AUTHORIZATION, null)
//        assertEquals(sessionID, sessionIDTest)
//    }
//
//    @After
//    fun teardown() {
//        mockWebServer.shutdown()
//    }
//}
