package com.tiparo.tripway.trippage.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tiparo.tripway.repository.TripsRepository
import com.tiparo.tripway.utils.Event
import com.tiparo.tripway.utils.Transformers
import com.tiparo.tripway.utils.UnaryOperator
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.launch
import javax.inject.Inject

class TripDetailViewModel @Inject constructor(private val tripsRepository: TripsRepository) :
    ViewModel() {
    val uiStateLiveData = MutableLiveData<TripDetailUiState>()
    private val compositeDisposable = CompositeDisposable()

    private val initialIntent = PublishSubject.create<Long>()
    private val retryIntent = PublishSubject.create<Long>()

//    private var tripWithPoint: TripWithPoints? = null
//
//    val _locationsItems = MutableLiveData<List<LatLng>>()
//    val locationsItems: LiveData<List<LatLng>> = _locationsItems
//
//    private val _pointsList = MutableLiveData<List<Point>>()
//    val pointsList: LiveData<List<Point>> = _pointsList
//
//    private val _tripRoute = MutableLiveData<String>()
//    val tripRoute: LiveData<String> = _tripRoute

    private val _snackbarText = MutableLiveData<Event<Int>>()
    val snackbarText: LiveData<Event<Int>> = _snackbarText

    private val _deletedEvent = MutableLiveData<Event<Unit>>()
    val deletedEvent: LiveData<Event<Unit>> = _deletedEvent

    init {
        val disposable = gerTripPage(tripsRepository)
            .scan(TripDetailUiState.idle(), { prevState, mutator ->
                mutator.apply(prevState)
            })
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { value: TripDetailUiState -> uiStateLiveData.postValue(value) }

        compositeDisposable.add(disposable)
    }

    fun loadTripPage(tripId: Long) {
        initialIntent.onNext(tripId)
    }

    fun retryLoadTripPage(tripId: Long) {
        retryIntent.onNext(tripId)
    }

    private fun gerTripPage(repository: TripsRepository): Observable<UnaryOperator<TripDetailUiState>> {
        return Observable.merge(initialIntent, retryIntent)
            .flatMap { tripId ->
                repository.getTripPage(tripId)
                    .map { result ->
                        if (result.isRight) {
                            TripDetailUiState.Mutators.discoveryData(result.right)
                        } else {
                            TripDetailUiState.Mutators.discoveryError(result.left)
                        }
                    }
                    .compose(Transformers.startWithInMain(TripDetailUiState.Mutators.discoveryLoading()))
            }
    }

    fun deleteTrip() {
//        viewModelScope.launch {
//            tripWithPoint?.let {
//                val result = tripsRepository.deleteTrip(it.trip.id)
//                when (result.status) {
//                    Resource.Status.SUCCESS -> {
//                        _deletedEvent.value = Event(Unit)
//                    }
//                    else -> {
//                        showSnackbarMessage(R.string.delete_trip_failed)
//                    }
//                }
//            }
//        }
    }

    fun deletePoint(pointPosition: Int) {
//        viewModelScope.launch {
//            tripWithPoint?.let {
//                val result = tripsRepository.deletePoint(it, pointPosition)
//                when (result.status) {
//                    Resource.Status.SUCCESS -> {
//                        _deletedEvent.value = Event(Unit)
//                    }
//                    else -> {
//                        showSnackbarMessage(R.string.delete_point_failed)
//                    }
//                }
//            }
//        }
    }

    private fun showSnackbarMessage(messageResource: Int) {
        _snackbarText.value = Event(messageResource)
    }
}