package com.tiparo.tripway.discovery.ui

import com.tiparo.tripway.discovery.api.dto.DiscoveryInfo
import com.tiparo.tripway.utils.ErrorBody
import com.tiparo.tripway.utils.LceUiState
import com.tiparo.tripway.utils.UnaryOperator

class DiscoveryUiState private constructor(
    loading: Boolean = false,
    errorBody: ErrorBody? = null,
    data: DiscoveryInfo? = null
) : LceUiState<DiscoveryInfo>(loading, errorBody, data) {

    companion object {
        fun idle() = DiscoveryUiState()

        fun loading() =
            DiscoveryUiState(loading = true)

        fun data(discoveryInfo: DiscoveryInfo?) =
            DiscoveryUiState(data = discoveryInfo)

        fun error(errorBody: ErrorBody?): DiscoveryUiState =
            DiscoveryUiState(errorBody = errorBody)
    }

    class Mutators private constructor() {

        companion object {
            fun discoveryLoading(): UnaryOperator<DiscoveryUiState> =
                UnaryOperator { loading() }

            fun discoveryData(discoveryInfo: DiscoveryInfo?): UnaryOperator<DiscoveryUiState> =
                UnaryOperator { prevState ->
                    val prevList = prevState.data.orNull()?.trips ?: listOf()
                    val newList = discoveryInfo?.trips ?: listOf()

                    data(discoveryInfo?.copy(trips = prevList + newList))
                }

            fun discoveryError(error: Throwable?): UnaryOperator<DiscoveryUiState> =
                UnaryOperator { prevState ->
                    DiscoveryUiState.error(ErrorBody.fromException(error))
                }
        }
    }
}