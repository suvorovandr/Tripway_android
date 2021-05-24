package com.tiparo.tripway.repository.database

import android.net.Uri
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tiparo.tripway.MainCoroutineRule
import com.tiparo.tripway.models.Point
import com.tiparo.tripway.models.Trip
import com.tiparo.tripway.models.TripWithPoints
import com.tiparo.tripway.repository.network.api.services.ReverseGeocodingResponse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.InputStreamReader

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class PointDaoTest : DbTest() {
    // Set the main coroutines dispatcher for unit testing.
    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    // Executes each task synchronously using Architecture Components.
    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Test
    fun createPoint() = runBlockingTest {
        // ARRANGE
        val position = LatLng(-33.8523341, 151.2106085)

        val inputStream =
            javaClass.classLoader!!.getResourceAsStream("api-response/address_components_locality.json")
        val addressComponents = Gson().fromJson(
            InputStreamReader(inputStream, Charsets.UTF_8),
            object :
                TypeToken<List<ReverseGeocodingResponse.GeocodingResult.AddressComponent>>() {}.type
        ) as List<ReverseGeocodingResponse.GeocodingResult.AddressComponent>

        val pickedPhotosOnAdding = listOf(
            Uri.parse("file:///storage/emulated/0/Android/data/com.tiparo.tripway/files/Pictures/1"),
            Uri.parse("file:///storage/emulated/0/Android/data/com.tiparo.tripway/files/Pictures/2"),
            Uri.parse("file:///storage/emulated/0/Android/data/com.tiparo.tripway/files/Pictures/3")
        )

        val pointOnAdding = Point(
            location = Point.Location(position, "Salsburg", addressComponents),
            description = "The best trip ever",
            photos = pickedPhotosOnAdding,
            tripId = 1
        )

        //ACT
        val pointIdInserted = db.pointDao().insertPoint(pointOnAdding)
        pointOnAdding.id = pointIdInserted

        //ASSERT
        val point = db.pointDao().getPointById(pointIdInserted)
        assertThat(point, `is`(pointOnAdding))
    }

    @Test
    fun getTripWithPoints() = runBlockingTest {
        // ARRANGE
        val trip =
            Trip(tripName = "Best ever trip!", isCompleted = false, firstPointName = "Salsburg", photoUri = Uri.EMPTY)
        val tripIdInserted = db.tripDao().insertTrip(trip)
        trip.id = tripIdInserted

        val pickedPhotosOnAdding = listOf(
            Uri.parse("file:///storage/emulated/0/Android/data/com.tiparo.tripway/files/Pictures/1"),
            Uri.parse("file:///storage/emulated/0/Android/data/com.tiparo.tripway/files/Pictures/2"),
            Uri.parse("file:///storage/emulated/0/Android/data/com.tiparo.tripway/files/Pictures/3")
        )
        val pointsOnAdding = arrayListOf(
            Point(tripId = tripIdInserted, photos = pickedPhotosOnAdding),
            Point(tripId = tripIdInserted, photos = pickedPhotosOnAdding)
        )
        pointsOnAdding.forEach {
            it.id = db.pointDao().insertPoint(it)
        }

        //ACT
        val tripWithPointsResult = db.tripDao().getTripWithPoints(tripIdInserted)

        //ASSERT
        val tripWithPoints = TripWithPoints(trip, pointsOnAdding)
        assertThat(tripWithPointsResult, `is`(tripWithPoints))
    }
}
