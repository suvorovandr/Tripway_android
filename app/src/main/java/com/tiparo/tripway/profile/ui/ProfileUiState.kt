package com.tiparo.tripway.profile.ui

import com.tiparo.tripway.profile.api.dto.ProfileInfo
import com.tiparo.tripway.utils.ErrorBody
import com.tiparo.tripway.utils.LceUiState
import com.tiparo.tripway.utils.UnaryOperator

class ProfileUiState private constructor(
    loading: Boolean = false,
    errorBody: ErrorBody? = null,
    data: ProfileInfo? = null
) : LceUiState<ProfileInfo>(loading, errorBody, data) {

    companion object {
        fun idle() = ProfileUiState()

        fun loading() =
            ProfileUiState(loading = true)

        fun data(info: ProfileInfo?) =
            ProfileUiState(data = info)

        fun error(errorBody: ErrorBody?): ProfileUiState =
            ProfileUiState(errorBody = errorBody)
    }

    class Mutators private constructor() {

        companion object {
            fun discoveryLoading(): UnaryOperator<ProfileUiState> =
                UnaryOperator { loading() }

            fun discoveryData(info: ProfileInfo?): UnaryOperator<ProfileUiState> =
                UnaryOperator { prevState -> data(info) }

            fun discoveryError(error: Throwable?): UnaryOperator<ProfileUiState> =
                UnaryOperator { prevState ->
                    ProfileUiState.error(ErrorBody.fromException(error))
                }
        }
    }
}