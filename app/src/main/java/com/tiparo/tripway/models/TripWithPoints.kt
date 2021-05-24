package com.tiparo.tripway.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Relation

@Entity
data class TripWithPoints(
    @Embedded val trip: Trip,
    @Relation(
        parentColumn = "id",
        entityColumn = "trip_id"
    )
    val points: List<Point>
)