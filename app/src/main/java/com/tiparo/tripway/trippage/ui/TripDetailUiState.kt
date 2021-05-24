package com.tiparo.tripway.trippage.ui

import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName
import com.tiparo.tripway.trippage.api.dto.TripPageInfo
import com.tiparo.tripway.utils.ErrorBody
import com.tiparo.tripway.utils.LceUiState
import com.tiparo.tripway.utils.UnaryOperator
import java.sql.Timestamp

class TripDetailUiState private constructor(
    loading: Boolean = false,
    errorBody: ErrorBody? = null,
    data: Data? = null
) : LceUiState<TripDetailUiState.Data>(loading, errorBody, data) {

    companion object {
        fun idle() = TripDetailUiState()

        fun loading() =
            TripDetailUiState(loading = true)

        fun data(info: TripPageInfo?) =
            TripDetailUiState(data = info?.let {
                Data(
                    it.id,
                    it.tripname,
                    "${it.firstPointName} > ${it.lastPointName}",
                    it.points.map {point-> LatLng(point.lat, point.lng)},
                    it.photo,
                    it.updated,
                    it.points
                )
            })

        fun error(errorBody: ErrorBody?): TripDetailUiState =
            TripDetailUiState(errorBody = errorBody)
    }

    data class Data(
        val id: Long,
        val tripname: String,
        val tripRoute: String,
        val locations: List<LatLng>,
        val photo: String,
        val updated: Timestamp,
        val points: List<TripPageInfo.Point>
    )

    class Mutators private constructor() {

        companion object {
            fun discoveryLoading(): UnaryOperator<TripDetailUiState> =
                UnaryOperator { loading() }

            fun discoveryData(info: TripPageInfo?): UnaryOperator<TripDetailUiState> =
                UnaryOperator { prevState -> data(info) }

            fun discoveryError(error: Throwable?): UnaryOperator<TripDetailUiState> =
                UnaryOperator { prevState ->
                    TripDetailUiState.error(ErrorBody.fromException(error))
                }
        }
    }
}