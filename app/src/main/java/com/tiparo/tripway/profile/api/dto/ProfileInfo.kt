package com.tiparo.tripway.profile.api.dto

import com.google.gson.annotations.SerializedName
import java.sql.Timestamp

data class ProfileInfo(
    val id: String,
    val trips: List<Trip>,
    val nickname: String,

    @SerializedName("subscribers_count")
    val subscribersCount: Int,

    @SerializedName("subscriptions_count")
    val subscriptionsCount: Int
//        val avatar: String
){
    data class Trip(
        val id: Long,
        val tripname: String,

        @SerializedName("is_completed")
        val isCompleted: Boolean = false,

        @SerializedName("first_point_name")
        val firstPointName: String,

        @SerializedName("last_point_name")
        val lastPointName: String,

        val photo: String,

        val updated: Timestamp
    )
}