package com.tiparo.tripway.repository.database

import android.net.Uri
import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tiparo.tripway.repository.network.api.services.ReverseGeocodingResponse.GeocodingResult.AddressComponent

class Converter {
    @TypeConverter
    fun latLngToString(latLng: LatLng) =
        "${latLng.latitude},${latLng.longitude}"

    @TypeConverter
    fun stringToLatLng(latLng: String): LatLng {
        val (lat, lng) = latLng.split(",").map { it.toDouble() }
        return LatLng(lat, lng)
    }

    @TypeConverter
    fun addressComponentsToString(addressComponents: List<AddressComponent>): String =
        Gson().toJson(addressComponents)


    @TypeConverter
    fun stringToAddressComponents(addressComponents: String): List<AddressComponent> {
        val listType = object : TypeToken<ArrayList<AddressComponent>>() {}.type
        return Gson().fromJson(addressComponents, listType)
    }

    @TypeConverter
    fun listUriToString(list: List<Uri>): String {
        val convertedList = list.joinToString(separator = ",") { it.toString() }
        return if (convertedList == "[]") ""
        else convertedList
    }

    @TypeConverter
    fun stringToListUri(uriString: String): List<Uri> {
        return if (uriString.isEmpty()) arrayListOf()
        else uriString.split(",").map { Uri.parse(it) }
    }

    @TypeConverter
    fun stringToUri(uriString: String): Uri {
        return Uri.parse(uriString)
    }

    @TypeConverter
    fun uriToString(uri: Uri): String {
        return uri.toString()
    }
}