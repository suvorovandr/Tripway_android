package com.tiparo.tripway.utils

import com.google.common.base.Optional

abstract class LceUiState<TData>(val loading: Boolean, val error: ErrorBody?, data: TData?) {
    val data: Optional<TData> = Optional.fromNullable(data)

    fun fold(loadingConsumer: () -> Unit,
             dataConsumer: (TData) -> Unit,
             errorConsumer: (ErrorBody) -> Unit) {
        when {
            loading -> loadingConsumer.invoke()
            error != null -> errorConsumer.invoke(error)
            data.isPresent -> dataConsumer.invoke(data.get())
        }
    }
}
