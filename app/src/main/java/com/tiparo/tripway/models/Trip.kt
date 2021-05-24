package com.tiparo.tripway.models

import android.net.Uri
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Trip(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    @ColumnInfo(name = "trip_name") val tripName: String = "",
    @ColumnInfo(name = "is_completed") val isCompleted: Boolean = false,
    @ColumnInfo(name = "first_point_name") val firstPointName: String = "",
    @ColumnInfo(name = "last_point_name") val lastPointName: String = "",

    @ColumnInfo(name = "photo_uri") val photoUri: Uri
)