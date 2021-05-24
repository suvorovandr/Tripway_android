package com.tiparo.tripway.di

import android.app.Application
import androidx.room.Room
import com.tiparo.tripway.BuildConfig
import com.tiparo.tripway.repository.database.PointDao
import com.tiparo.tripway.repository.database.TripDao
import com.tiparo.tripway.repository.database.TripwayDB
import com.tiparo.tripway.repository.network.api.services.AuthService
import com.tiparo.tripway.repository.network.api.services.GoogleMapsServices
import com.tiparo.tripway.repository.network.api.services.TripsService
import com.tiparo.tripway.repository.network.http.BaseHttpClient
import com.tiparo.tripway.repository.network.http.HttpClient
import dagger.Module
import dagger.Provides
import javax.inject.Qualifier
import javax.inject.Singleton

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class GoogleMapsHTTPClient

@Retention(AnnotationRetention.BINARY)
@Qualifier
annotation class TripwayHTTPClient

@Module(includes = [ViewModelModule::class])
class AppModule {
    @Singleton
    @Provides
    fun provideAuthService(@TripwayHTTPClient httpClient: HttpClient): AuthService {
        return httpClient.getApiService(AuthService::class.java)
    }

    @Singleton
    @Provides
    fun provideGoogleService(@GoogleMapsHTTPClient httpClient: HttpClient): GoogleMapsServices {
        return httpClient.getApiService(GoogleMapsServices::class.java)
    }

    @Singleton
    @Provides
    fun provideTripsService(@TripwayHTTPClient httpClient: HttpClient): TripsService {
        return httpClient.getApiService(TripsService::class.java)
    }

    @TripwayHTTPClient
    @Singleton
    @Provides
    fun provideTripwayHttpClient(application: Application): HttpClient {
        return BaseHttpClient(BuildConfig.BASE_URL + "api/v1/", application)
    }

    @GoogleMapsHTTPClient
    @Singleton
    @Provides
    fun provideGoogleMapsHttpClient(application: Application): HttpClient {
        return BaseHttpClient(BuildConfig.GOOGLE_MAPS_URL, application)
    }


    @Singleton
    @Provides
    fun provideDb(app: Application): TripwayDB {
        return Room
            .databaseBuilder(app, TripwayDB::class.java, "tripway.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Singleton
    @Provides
    fun providePointDao(db: TripwayDB): PointDao {
        return db.pointDao()
    }

    @Singleton
    @Provides
    fun provideTripDao(db: TripwayDB): TripDao {
        return db.tripDao()
    }
}
