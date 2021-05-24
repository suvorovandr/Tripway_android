package com.tiparo.tripway

import junit.framework.Assert.assertEquals
import okhttp3.Cookie
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.junit.Test

class CookieTest {
    @Test
    fun parseValidCookie() {
        //Assign
        val httpCookieString =
            "session-id=f43f93hreufnu29fbi24nf3; Expires=Wed, 21 Oct 2015 07:28:00 GMT"
        val sessionID = "f43f93hreufnu29fbi24nf3";

        //Act
        val httpCookie =
            Cookie.parse("https://tripway.ru.com/tripway".toHttpUrlOrNull()!!, httpCookieString)

        //Assert
        assertEquals(httpCookie?.value, sessionID)
    }
}