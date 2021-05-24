package com.tiparo.tripway.trippage.api.dto

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import java.sql.Timestamp

data class TripPageInfo(
    val id: Long,
    val tripname: String,

    @SerializedName("is_completed")
    val isCompleted: Boolean,

    @SerializedName("first_point_name")
    val firstPointName: String,

    @SerializedName("last_point_name")
    val lastPointName: String,

    val photo: String,

    val updated: Timestamp,

    val points: List<Point>
) {
    @Parcelize
    data class Point(
        val id: Long,
        val name: String,
        val description: String?,
        val photos: List<String>,
        val created: Timestamp,
        val lat: Double,
        val lng: Double,
        val address: String,
        @SerializedName("address_components") val addressComponents: String
    ): Parcelable
}