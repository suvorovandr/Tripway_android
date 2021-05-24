package com.tiparo.tripway.repository

import com.tiparo.tripway.AppExecutors
import com.tiparo.tripway.discovery.api.dto.DiscoveryInfo
import com.tiparo.tripway.repository.network.api.services.TripsService
import com.tiparo.tripway.trippage.api.dto.TripPageInfo
import com.tiparo.tripway.utils.*
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.Function
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import javax.inject.Inject

class TripsRepository @Inject constructor(
    private val appExecutors: AppExecutors,
    private val tripsService: TripsService
) {
//
//    suspend fun deleteTrip(tripId: Long) = withContext(Dispatchers.IO) {
//        try {
//            tripDao.deleteTrip(tripId)
//            //TODO удалять еще ресурсы, связанные с трипом
//            //TODO в будущем можно поместить это в настройках в виде опции для выбора
//            Resource.success(null)
//        } catch (exception: Exception) {
//            Timber.e(exception, "Error when delete trip with id = $tripId from database")
//            Resource.error(null, ErrorDescription(""))
//        }
//    }
//
//    //TODO написать тесты
//    suspend fun deletePoint(
//        tripWithPoints: TripWithPoints,
//        pointPosition: Int
//    ) = withContext(Dispatchers.IO) {
//        val trip = tripWithPoints.trip
//        val points = tripWithPoints.points
//        try {
//            tripwayDB.withTransaction {
//                if (points.size == 1) {
//                    pointDao.deletePoint(points[0].id)
//                    tripDao.deleteTrip(trip.id)
//                }
//                if (points.size > 1) {
//                    if (pointPosition == 0) {
//                        val newFirstNameTrip = points[pointPosition + 1].name
//                        val updatedTrip = trip.copy(firstPointName = newFirstNameTrip)
//
//                        tripDao.updateTrip(updatedTrip)
//                    }
//                    if (pointPosition == points.lastIndex) {
//                        val newLastNameTrip = points[points.lastIndex - 1].name
//                        val newPhotoUriTrip = points[points.lastIndex - 1].photos.last()
//                        val updatedTrip =
//                            trip.copy(lastPointName = newLastNameTrip, photoUri = newPhotoUriTrip)
//
//                        tripDao.updateTrip(updatedTrip)
//                    }
//
//                    pointDao.deletePoint(points[pointPosition].id)
//                }
//                Resource.success(null)
//            }
//        } catch (exception: Exception) {
//            Timber.e(
//                exception,
//                "Error when delete point with id = ${points[pointPosition].id} from database"
//            )
//            Resource.error(null, ErrorDescription(""))
//        }
//    }

    fun discoveryFirstPageResult(): Observable<Either<Throwable, DiscoveryInfo>> = paginator
        .getFirstPage(Unit)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .toObservable()

    fun discoveryNextPageResult(): Observable<Either<Throwable, DiscoveryInfo>> = paginator
        .nextPage()
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .toObservable()

    fun getOwnTrips(): Observable<Either<Throwable, List<TripsService.Trip>>> = tripsService.getOwnTrips()
            .toObservable()
            .doOnError { er: Throwable -> Timber.e(er.toString()) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .compose(Transformers.neverThrowO())

    fun getTripPage(tripId: Long): Observable<Either<Throwable, TripPageInfo>> = tripsService.getTrip(tripId)
        .toObservable()
        .doOnError { er: Throwable -> Timber.e(er.toString()) }
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .compose(Transformers.neverThrowO())

    private val paginator: RxPaginator<DiscoveryInfo, Unit> = RxPaginator(
        BiFunction { pageParams, anchor ->
            tripsService.getTripsDiscoveryPage(anchor)
        },
        Function { discovery: DiscoveryInfo ->
            PageToken(discovery.anchor, discovery.hasMore ?: false)
        }
    )
}