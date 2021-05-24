package com.tiparo.tripway.repository.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.tiparo.tripway.models.Point

@Dao
abstract class PointDao {
    @Insert
    abstract suspend fun insertPoint(point: Point): Long

    @Query("SELECT * FROM Point WHERE id = :pointId")
    abstract suspend fun getPointById(pointId: Long): Point

    @Query("SELECT * FROM Point WHERE trip_id = :tripId")
    abstract suspend fun getPointsByTripId(tripId: Long): List<Point>

    @Query("DELETE FROM Point WHERE id = :pointId")
    abstract suspend fun deletePoint(pointId: Long)
}