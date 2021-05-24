package com.tiparo.tripway.repository

import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tiparo.tripway.posting.domain.PostRepository
import com.tiparo.tripway.repository.database.PointDao
import com.tiparo.tripway.repository.database.TripDao
import com.tiparo.tripway.repository.database.TripwayDB
import com.tiparo.tripway.repository.network.api.services.GoogleMapsServices
import com.tiparo.tripway.repository.network.api.services.ReverseGeocodingResponse
import com.tiparo.tripway.repository.network.api.services.TripsService
import com.tiparo.tripway.util.InstantAppExecutors
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import java.io.InputStreamReader


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class PostRepositoryTest {

    private lateinit var repository: PostRepository
    private val tripDao = Mockito.mock(TripDao::class.java)
    private val pointDao = Mockito.mock(PointDao::class.java)
    private val tripsService = Mockito.mock(TripsService::class.java)
    private val googleMapsServices = Mockito.mock(GoogleMapsServices::class.java)

    @Before
    fun init() {
        val db = Mockito.mock(TripwayDB::class.java)
        Mockito.`when`(db.pointDao()).thenReturn(pointDao)
        Mockito.`when`(db.tripDao()).thenReturn(tripDao)
        repository = PostRepository(
            ApplicationProvider.getApplicationContext(),
            InstantAppExecutors(),
            tripDao,
            pointDao,
            tripsService,
            googleMapsServices
        )
    }

    @Test
    fun getPointName_Test_get_sublocality_level_2() = runBlocking{
        val inputStream = javaClass.classLoader!!.getResourceAsStream("api-response/address_components_sub_locality_level_2.json")
        val listType = object : TypeToken<List<ReverseGeocodingResponse.GeocodingResult.AddressComponent>>() {}.type
        val addressComponents = Gson().fromJson(
            InputStreamReader(inputStream, Charsets.UTF_8),
            listType
        ) as List<ReverseGeocodingResponse.GeocodingResult.AddressComponent>

        val pointName = repository.getPointName(addressComponents)

        assertEquals("SalzburgD1", pointName)
    }

    @Test
    fun getPointName_Test_get_locality() = runBlocking{
        val inputStream = javaClass.classLoader!!.getResourceAsStream("api-response/address_components_locality.json")
        val listType = object : TypeToken<List<ReverseGeocodingResponse.GeocodingResult.AddressComponent>>() {}.type
        val addressComponents = Gson().fromJson(
            InputStreamReader(inputStream, Charsets.UTF_8),
            listType
        ) as List<ReverseGeocodingResponse.GeocodingResult.AddressComponent>

        val pointName = repository.getPointName(addressComponents)

        assertEquals("Salzburg", pointName)
    }

    @Test
    fun getPointName_Test_get_administrative_area_level_2() = runBlocking{
        val inputStream = javaClass.classLoader!!.getResourceAsStream("api-response/address_components_administrative_area_level_2.json")
        val listType = object : TypeToken<List<ReverseGeocodingResponse.GeocodingResult.AddressComponent>>() {}.type
        val addressComponents = Gson().fromJson(
            InputStreamReader(inputStream, Charsets.UTF_8),
            listType
        ) as List<ReverseGeocodingResponse.GeocodingResult.AddressComponent>

        val pointName = repository.getPointName(addressComponents)

        assertEquals("Dawson County", pointName)
    }

    //TODO тест побочный, сделать tear-down
    @Test
    fun savePickedPhotos_Test_Success() = runBlocking {
        val pickedPhotosOnAdding = listOf(
            Uri.parse("content://media/external/images/media/91783"),
            Uri.parse("content://media/external/images/media/91782"),
            Uri.parse("content://media/external/images/media/91790")
        )

        val uriPhotosList = repository.savePickedPhotos(pickedPhotosOnAdding)
        println(uriPhotosList.toString())

        assertTrue(uriPhotosList.filterNotNull().size == pickedPhotosOnAdding.size)
    }
}
