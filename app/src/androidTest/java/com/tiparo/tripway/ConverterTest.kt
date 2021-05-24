package com.tiparo.tripway

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.tiparo.tripway.repository.database.Converter
import com.tiparo.tripway.repository.network.api.services.ReverseGeocodingResponse
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ConverterTest {
    @Test
    fun convertFromString_to_listAddressComponents() {
        val addressComponents = listOf(
            ReverseGeocodingResponse.GeocodingResult.AddressComponent(
                "a",
                "b",
                listOf("city", "locality")
            )
        )

        val convertResult = Gson().toJson(addressComponents)

        Assert.assertEquals(
            convertResult,
            """[{"long_name":"a","short_name":"b","types":["city","locality"]}]"""
        )
    }

    @Test
    fun convertFromAddressComponents_to_String() {
        val addressComponents =
            """[{"long_name":"a","short_name":"b","types":["city","locality"]}]"""

        val listType = object :
            TypeToken<List<ReverseGeocodingResponse.GeocodingResult.AddressComponent>>() {}.type
        val convertResult =
            Gson().fromJson<List<ReverseGeocodingResponse.GeocodingResult.AddressComponent>>(
                addressComponents,
                listType
            )

        Assert.assertEquals(
            convertResult, listOf(
                ReverseGeocodingResponse.GeocodingResult.AddressComponent(
                    "a",
                    "b",
                    listOf("city", "locality")
                )
            )
        )
    }

    @Test
    fun convertFromListUriToString_Empty() {
        val pickedUriOnAdding = arrayListOf<Uri>()
        val converter = Converter()

        val listUriString = converter.listUriToString(pickedUriOnAdding)

        Assert.assertEquals(
            "",
            listUriString
        )
    }

    @Test
    fun convertFromListUriToString() {
        val listUri = listOf(
                Uri.parse("file:///storage/emulated/0/Android/data/com.tiparo.tripway/files/Pictures/1"),
                Uri.parse("file:///storage/emulated/0/Android/data/com.tiparo.tripway/files/Pictures/2")
            )

        val converter = Converter()
        val photosList = converter.listUriToString(listUri)

        Assert.assertEquals(
            "file:///storage/emulated/0/Android/data/com.tiparo.tripway/files/Pictures/1" +
                    "," +
                    "file:///storage/emulated/0/Android/data/com.tiparo.tripway/files/Pictures/2",
            photosList
        )
    }

    @Test
    fun convertFromListUriToString_OneUri() {
        val listUri = listOf(
            Uri.parse("file:///storage/emulated/0/Android/data/com.tiparo.tripway/files/Pictures/1")
        )

        val converter = Converter()
        val photosList = converter.listUriToString(listUri)

        Assert.assertEquals(
            "file:///storage/emulated/0/Android/data/com.tiparo.tripway/files/Pictures/1",
            photosList
        )
    }

    @Test
    fun convertFromStringToListUri_Empty() {
        val photosListInString = ""
        val converter = Converter()
//        Mockito.`when`(Uri.parse(Mockito.any())).thenReturn(Uri.EMPTY)

        val photosList = converter.stringToListUri(photosListInString)

        Assert.assertEquals(
            arrayListOf<Uri>(), photosList
        )
    }

    @Test
    fun convertFromStringToListUri() {
        val photosListInString = "file:///storage/emulated/0/Android/data/com.tiparo.tripway/files/Pictures/1" +
                "," +
                "file:///storage/emulated/0/Android/data/com.tiparo.tripway/files/Pictures/2"

        val converter = Converter()
        val photosList = converter.stringToListUri(photosListInString)

        Assert.assertEquals(
            listOf(
                Uri.parse("file:///storage/emulated/0/Android/data/com.tiparo.tripway/files/Pictures/1"),
                Uri.parse("file:///storage/emulated/0/Android/data/com.tiparo.tripway/files/Pictures/2")
            ),
            photosList
        )
    }

    @Test
    fun convertFromStringToUriAndReverse() {
        val photoUriString =
            "file:///storage/emulated/0/Android/data/com.tiparo.tripway/files/Pictures/1"

        val converter = Converter()
        val photoUri = converter.stringToUri(photoUriString)
        val photoUriStringActual = converter.uriToString(photoUri)

        Assert.assertEquals(photoUriString, photoUriStringActual)
    }
}