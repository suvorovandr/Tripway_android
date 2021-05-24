package com.tiparo.tripway.repository.database

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.room.*
import com.tiparo.tripway.models.Trip
import com.tiparo.tripway.models.TripWithPoints

@Dao
abstract class TripDao {
    @Transaction
    @Query("SELECT * FROM Trip WHERE id = :tripId")
    abstract suspend fun getTripWithPoints(tripId: Long): TripWithPoints

    @Query("SELECT * FROM Trip WHERE id = :tripId")
    abstract suspend fun getTripById(tripId: Long): Trip

    @Insert
    abstract suspend fun insertTrip(trip: Trip): Long

    @Query("SELECT * FROM Trip")
    abstract fun getTrips(): LiveData<List<Trip>>

    @Query("UPDATE Trip SET last_point_name = :name, photo_uri = :photoUri WHERE id = :tripId")
    abstract suspend fun updateTripByNewPoint(tripId: Long, name: String, photoUri: Uri)

    @Update
    abstract suspend fun updateTrip(trip: Trip): Int

    @Query("DELETE FROM Trip WHERE id = :tripId")
    abstract suspend fun deleteTrip(tripId: Long)
}