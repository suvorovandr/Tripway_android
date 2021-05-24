package com.tiparo.tripway.repository.network.api.services

import androidx.lifecycle.LiveData
import com.google.gson.annotations.SerializedName
import com.tiparo.tripway.repository.network.api.ApiResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface GoogleMapsServices {

    @GET("maps/api/geocode/json")
    fun reverseGeocoding(@Query("latlng") location: String, @Query("key") apiKey: String): LiveData<ApiResponse<ReverseGeocodingResponse>>
}

data class ReverseGeocodingResponse(@SerializedName("results") val results: List<GeocodingResult>) {
    data class GeocodingResult(
        @SerializedName("address_components") val address_components: List<AddressComponent>?,
        @SerializedName("formatted_address") val formatted_address: String
    ) {
        data class AddressComponent(
            @SerializedName("long_name") val long_name: String,
            @SerializedName("short_name") val short_name: String,
            @SerializedName("types") val types: List<String>
        )
    }
}