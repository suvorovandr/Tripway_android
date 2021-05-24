package com.tiparo.tripway.discovery.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tiparo.tripway.discovery.api.dto.DiscoveryInfo
import com.tiparo.tripway.discovery.ui.DiscoveryUiState.Mutators
import com.tiparo.tripway.repository.TripsRepository
import com.tiparo.tripway.utils.RxUnit
import com.tiparo.tripway.utils.Transformers.startWithInMain
import com.tiparo.tripway.utils.UnaryOperator
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.BiFunction
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class DiscoveryViewModel @Inject constructor(private val tripsRepository: TripsRepository) :
    ViewModel() {

    val query = MutableLiveData<String>()
    val uiStateLiveData = MutableLiveData<DiscoveryUiState>()
    private val compositeDisposable = CompositeDisposable()

    private val loadFirstPageIntent = PublishSubject.create<RxUnit>()
    private val retryFirstPageIntent = PublishSubject.create<RxUnit>()
    private val loadNextPageIntent = PublishSubject.create<RxUnit>()
    private val innerState: BehaviorSubject<DiscoveryUiState> = BehaviorSubject.create()


    init {
        val disposable = Observable.merge(firstPageChanges(), nextPageChanges())
            .scan(DiscoveryUiState.idle(), { prevState, mutator ->
                mutator.apply(prevState)
            })
            .observeOn(AndroidSchedulers.mainThread())
            .doOnNext(innerState::onNext)
            .subscribe { value: DiscoveryUiState -> uiStateLiveData.postValue(value) }

        compositeDisposable.add(disposable)
    }

    fun loadFirstPageIntent() {
        loadFirstPageIntent.onNext(RxUnit.INSTANCE)
    }

    fun loadNextPageIntent() {
        loadNextPageIntent.onNext(RxUnit.INSTANCE)
    }

    fun retryFirstPageIntent() {
        retryFirstPageIntent.onNext(RxUnit.INSTANCE)
    }

    private fun firstPageChanges(): Observable<UnaryOperator<DiscoveryUiState>> {
        return Observable.merge(loadFirstPageIntent, retryFirstPageIntent)
            .flatMap {
                tripsRepository.discoveryFirstPageResult()
                    .map { result ->
                        if (result.isRight) {
                            Mutators.discoveryData(result.right)
                        } else {
                            Mutators.discoveryError(result.left)
                        }
                    }
                    .compose(startWithInMain(Mutators.discoveryLoading()))
            }
    }

    private fun nextPageChanges(): Observable<UnaryOperator<DiscoveryUiState>> {
        return loadNextPageIntent
            .withLatestFrom(
                innerState,
                BiFunction<Any, DiscoveryUiState, DiscoveryUiState> { _, s -> s })
            .map { it.data.orNull() }
            .filter { d: DiscoveryInfo? -> d != null && d.hasMore ?: false }
            .flatMap { tripsNextPageResult() }

    }

    private fun tripsNextPageResult(): Observable<UnaryOperator<DiscoveryUiState>> {
        return tripsRepository.discoveryNextPageResult()
            .map { result ->
                if (result.isRight) Mutators.discoveryData(result.right)
                else Mutators.discoveryError(result.left)
            }
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }


//    private val _items = MediatorLiveData<List<Trip>>()
//
//    //Lock on changes
//    private val items: LiveData<List<Trip>> = _items
//
//    private val _filteredItems = MediatorLiveData<List<Trip>>().apply {
//        addSource(query) { queryString ->
//            viewModelScope.launch {
//                val trips = searchByQuery(_items.value!!, queryString)
//                value = trips
//            }
//        }
//        addSource(_items) {
//            value = items.value
//        }
//    }
//    val filteredItems: MediatorLiveData<List<Trip>> = _filteredItems
//
//    private val _snackbarText = MutableLiveData<Event<Int>>()
//    val snackbarText: LiveData<Event<Int>> = _snackbarText
//
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
//
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
//
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

//    private fun searchByTripAttributes(trip: Trip, queryString: String) =
//        trip.tripName.contains(queryString, true)
//
//
//    fun showSnackbarMessage(messageResource: Int) {
//        _snackbarText.value = Event(messageResource)
//    }
//
//    fun clearSearchingInfo(): Boolean {
//        filteredItems.value = items.value
//        return true
//    }
}