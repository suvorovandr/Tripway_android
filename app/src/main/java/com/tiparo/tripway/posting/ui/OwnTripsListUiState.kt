package com.tiparo.tripway.posting.ui

import com.tiparo.tripway.repository.network.api.services.TripsService
import com.tiparo.tripway.utils.ErrorBody
import com.tiparo.tripway.utils.LceUiState
import com.tiparo.tripway.utils.UnaryOperator

class OwnTripsListUiState private constructor(
    loading: Boolean = false,
    errorBody: ErrorBody? = null,
    data: List<TripsService.Trip>? = null
) : LceUiState<List<TripsService.Trip>>(loading, errorBody, data) {

    companion object {
        fun idle() = OwnTripsListUiState()

        fun loading() =
            OwnTripsListUiState(loading = true)

        fun data(trips: List<TripsService.Trip>?) =
            OwnTripsListUiState(data = trips)

        fun error(errorBody: ErrorBody?): OwnTripsListUiState =
            OwnTripsListUiState(errorBody = errorBody)
    }

    class Mutators private constructor() {

        companion object {
            fun discoveryLoading(): UnaryOperator<OwnTripsListUiState> =
                UnaryOperator { loading() }

            fun discoveryData(trips: List<TripsService.Trip>?): UnaryOperator<OwnTripsListUiState> =
                UnaryOperator { prevState -> data(trips) }

            fun discoveryError(error: Throwable?): UnaryOperator<OwnTripsListUiState> =
                UnaryOperator { prevState ->
                    OwnTripsListUiState.error(ErrorBody.fromException(error))
                }
        }
    }
}