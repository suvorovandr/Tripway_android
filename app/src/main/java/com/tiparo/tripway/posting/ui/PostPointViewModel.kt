package com.tiparo.tripway.posting.ui

import android.annotation.SuppressLint
import android.net.Uri
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.AddressComponents
import com.google.android.libraries.places.api.model.Place
import com.tiparo.tripway.models.Point
import com.tiparo.tripway.posting.domain.PostRepository
import com.tiparo.tripway.repository.TripsRepository
import com.tiparo.tripway.repository.network.api.services.ReverseGeocodingResponse.GeocodingResult
import com.tiparo.tripway.repository.network.api.services.TripsService.Trip
import com.tiparo.tripway.utils.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

@SuppressLint("CheckResult")
class PostPointViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val tripsRepository: TripsRepository
) : ViewModel() {

    private val pointOnAdding = Point(tripName = null)
    private var pickedPhotosOnAdding: List<Uri> = arrayListOf()
    var isNewPoint = false

    private val loadTripsIntent = PublishSubject.create<RxUnit>()
    private val retryTripsIntent = PublishSubject.create<RxUnit>()
    private val savePointIntent = PublishSubject.create<Point>()
    val compositeDisposable = CompositeDisposable()

    val trips = MutableLiveData<OwnTripsListUiState>()
    val description = MutableLiveData<String>()
    val tripName = MutableLiveData<String>()
    val pickedLocation = MutableLiveData<LatLng>()
    val pickedPlace = MutableLiveData<Place>()
    val snackbarText = MutableLiveData<Event<Int>>()
    val pointSaved = MutableLiveData<PostPointUiState>()

    init {
        val disposable = getTrips(tripsRepository)
            .scan(OwnTripsListUiState.idle(), { prevState, mutator ->
                mutator.apply(prevState)
            })
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { value: OwnTripsListUiState -> trips.postValue(value) }

        savePoint(postRepository)
            .scan(PostPointUiState.idle(), { prevState, mutator ->
                mutator.apply(prevState)
            })
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { value: PostPointUiState -> pointSaved.postValue(value) }

        compositeDisposable.add(disposable)
    }

    private fun getTrips(repository: TripsRepository): Observable<UnaryOperator<OwnTripsListUiState>> {
        return Observable.merge(loadTripsIntent, retryTripsIntent)
            .flatMap {
                repository.getOwnTrips()
                    .map { result ->
                        if (result.isRight) {
                            OwnTripsListUiState.Mutators.discoveryData(result.right)
                        } else {
                            OwnTripsListUiState.Mutators.discoveryError(result.left)
                        }
                    }
                    .compose(Transformers.startWithInMain(OwnTripsListUiState.Mutators.discoveryLoading()))
            }
    }

    private fun savePoint(repository: PostRepository): Observable<UnaryOperator<PostPointUiState>> {
        return savePointIntent
            .flatMap { point->
                point.name = locationName.value?.data?: "Неизестное"
                repository.savePoint(point)
                    .map { result->
                        if (result.isRight) {
                            PostPointUiState.Mutators.discoveryData(result.right)
                        } else {
                            PostPointUiState.Mutators.discoveryError(result.left)
                        }
                    }
                    .compose(Transformers.startWithInMain(PostPointUiState.Mutators.discoveryLoading()))
            }
    }

    val locationName = MediatorLiveData<Resource<String>>().apply {
        addSource(pickedLocation) { position ->
            //TODO вынести в отдельный обзервер для удобочитаемости
            val resource = postRepository.reverseGeocode(position)
            addSource(resource) {
                saveGeocodingResults(position, it.data)

                value = Resource.success(it.data?.formatted_address)

                //TODO обработать случай, когда data = null (когда Google возвращает Empty Body)
            }
        }
        addSource(pickedPlace) { place ->
            savePlace(place)

            //todo если не удалось получить name, то выдавать ошибку! или default value
            value = Resource.success(place.name)
        }
    }

    private fun savePlace(place: Place) {
        with(pointOnAdding.location) {
            place.latLng?.let { position = it }
            place.address?.let { address = it }
            place.addressComponents?.let { addressComponents = it.mapToLocalAddressComponent() }
        }
    }

    private fun saveGeocodingResults(
        pickedPosition: LatLng,
        results: GeocodingResult?
    ) {
        if (results?.address_components != null) {
            with(pointOnAdding.location) {
                position = pickedPosition
                address = results.formatted_address
                addressComponents = results.address_components
            }
        }
    }

    fun selectTripToPost(trip: Trip?) {
        pointOnAdding.tripId = trip?.id
        isNewPoint = trip == null
    }

    fun savePickedPhotos(obtainResult: List<Uri>) {
        pickedPhotosOnAdding = obtainResult
    }

    fun savePoint(description: String?, tripName: String?) {
        pointOnAdding.description = description
        pointOnAdding.tripName = tripName
        pointOnAdding.photos = pickedPhotosOnAdding

        savePointIntent.onNext(pointOnAdding)
    }

    fun loadTrips() {
        loadTripsIntent.onNext(RxUnit.INSTANCE)
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }
}

private fun AddressComponents.mapToLocalAddressComponent(): List<GeocodingResult.AddressComponent> =
    asList().map {
        GeocodingResult.AddressComponent(it.name, it.shortName ?: it.name, it.types)
    }

