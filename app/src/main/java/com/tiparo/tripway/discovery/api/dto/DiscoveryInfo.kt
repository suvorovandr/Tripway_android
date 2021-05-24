package com.tiparo.tripway.discovery.api.dto

import com.google.gson.annotations.SerializedName
import com.tiparo.tripway.repository.network.api.services.TripsService

data class DiscoveryInfo(
    @SerializedName("anchor") val anchor: String?,
    @SerializedName("has_more") val hasMore: Boolean?,
    @SerializedName("trips") val trips: List<TripsService.Trip>
)