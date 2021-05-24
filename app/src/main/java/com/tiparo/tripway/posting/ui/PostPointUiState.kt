package com.tiparo.tripway.posting.ui

import com.tiparo.tripway.repository.network.api.services.TripsService
import com.tiparo.tripway.utils.ErrorBody
import com.tiparo.tripway.utils.LceUiState
import com.tiparo.tripway.utils.UnaryOperator

class PostPointUiState private constructor(
    loading: Boolean = false,
    errorBody: ErrorBody? = null,
    data: TripsService.PointPostResult? = null
) : LceUiState<TripsService.PointPostResult>(loading, errorBody, data) {

    companion object {
        fun idle() = PostPointUiState()

        fun loading() =
            PostPointUiState(loading = true)

        fun data(postResult: TripsService.PointPostResult?) =
            PostPointUiState(data = postResult)

        fun error(errorBody: ErrorBody?): PostPointUiState =
            PostPointUiState(errorBody = errorBody)
    }

    class Mutators private constructor() {

        companion object {
            fun discoveryLoading(): UnaryOperator<PostPointUiState> =
                UnaryOperator { loading() }

            fun discoveryData(postResult: TripsService.PointPostResult?): UnaryOperator<PostPointUiState> =
                UnaryOperator { prevState -> data(postResult) }

            fun discoveryError(error: Throwable?): UnaryOperator<PostPointUiState> =
                UnaryOperator { prevState ->
                    PostPointUiState.error(ErrorBody.fromException(error))
                }
        }
    }
}