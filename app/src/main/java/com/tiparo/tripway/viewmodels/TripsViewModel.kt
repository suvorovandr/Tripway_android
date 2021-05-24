package com.tiparo.tripway.viewmodels

import androidx.lifecycle.*
import com.tiparo.tripway.R
import com.tiparo.tripway.models.Trip
import com.tiparo.tripway.repository.TripsRepository
import com.tiparo.tripway.utils.Event
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

class TripsViewModel @Inject constructor(private val tripsRepository: TripsRepository) :
    ViewModel() {

    val query = MutableLiveData<String>()

    private val _items = MediatorLiveData<List<Trip>>()
    //Lock on changes
    private val items: LiveData<List<Trip>> = _items

    private val _filteredItems = MediatorLiveData<List<Trip>>().apply {
        addSource(query) { queryString ->
            viewModelScope.launch {
//                val trips = searchByQuery(_items.value!!, queryString)
//                value = trips
            }
        }
        addSource(_items) {
            value = items.value
        }
    }
    val filteredItems: MediatorLiveData<List<Trip>> = _filteredItems

    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarText: LiveData<Event<Int>> = _snackbarText

//    fun loadTrips() {
//        val tripsResult = tripsRepository.loadTrips()
//        _items.addSource(tripsResult) {
//            when (it.status) {
//                Resource.Status.SUCCESS -> {
//                    _items.value = it.data
//                }
//                Resource.Status.ERROR -> {
//                    _items.value = emptyList()
//                    showSnackbarMessage(R.string.loading_trips_error)
//                }
//                Resource.Status.LOADING -> {
//                    //TODO добавить позже прогресс бар ожидания
//                }
//            }
//        }
//    }

//    private suspend fun searchByQuery(
//        trips: List<Trip>,
//        queryString: String
//    ) = withContext(Dispatchers.Default) {
//        trips.filter { trip ->
//            searchByTripAttributes(trip, queryString) || searchByPointsAttributes(
//                trip.id,
//                queryString
//            )
//        }
//    }

//    private suspend fun searchByPointsAttributes(id: Long, queryString: String): Boolean {
//        val points = tripsRepository.loadPointsByTripId(id)
//        return points.data?.any { point ->
//            setOf(
//                point.name,
//                point.description,
//                point.location.address
//            ).any { it.contains(queryString, true) }
//        } ?: false
//    }

    private fun searchByTripAttributes(trip: Trip, queryString: String) =
        trip.tripName.contains(queryString, true)


    fun showSnackbarMessage(messageResource: Int) {
        _snackbarText.value = Event(messageResource)
    }

    fun clearSearchingInfo(): Boolean {
        filteredItems.value = items.value
        return true
    }
}