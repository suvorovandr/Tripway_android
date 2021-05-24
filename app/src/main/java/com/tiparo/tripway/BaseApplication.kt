package com.tiparo.tripway

import android.app.Application
import com.facebook.drawee.backends.pipeline.Fresco
import com.tiparo.tripway.di.DaggerAppComponent
import timber.log.Timber

open class BaseApplication : Application() {

    val APP_NAME = "com.tiparo.tripway"

    val appComponent by lazy {
        DaggerAppComponent.builder().application(this)
            .builder()
    }

    override fun onCreate() {
        super.onCreate()
        if(BuildConfig.DEBUG){
            Timber.plant(Timber.DebugTree())
        }
        Fresco.initialize(this);
    }
}