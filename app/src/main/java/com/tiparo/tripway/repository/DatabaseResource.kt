package com.tiparo.tripway.repository

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.tiparo.tripway.AppExecutors
import com.tiparo.tripway.utils.Resource
import com.tiparo.tripway.repository.network.api.ErrorDescription

abstract class DatabaseResource<ResultType>
@MainThread constructor(private val appExecutors: AppExecutors) {

    private val result = MediatorLiveData<Resource<ResultType>>()

    init {
        result.value = Resource.loading(null)
        try {
            @Suppress("LeakingThis")
            val dbSource = loadFromDb()
            result.addSource(dbSource) { data ->
                setValue(Resource.success(data))
            }
        } catch (exception: Exception) {
            setValue(
                Resource.error(
                    null,
                    ErrorDescription(exception.message ?: "Error when trying to load from database")
                )
            )
        }

    }

    @MainThread
    private fun setValue(newValue: Resource<ResultType>) {
        if (result.value != newValue) {
            result.value = newValue
        }
    }

    fun asLiveData() = result as LiveData<Resource<ResultType>>

    @MainThread
    protected abstract fun loadFromDb(): LiveData<ResultType>
}
