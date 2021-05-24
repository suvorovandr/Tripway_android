package com.tiparo.tripway.di

import android.app.Application
import com.tiparo.tripway.BaseApplication
import com.tiparo.tripway.discovery.ui.DiscoveryFragment
import com.tiparo.tripway.login.ui.LoginFragment
import com.tiparo.tripway.posting.ui.PostPointDescriptionFragment
import com.tiparo.tripway.posting.ui.PostPointListFragment
import com.tiparo.tripway.posting.ui.PostPointMapFragment
import com.tiparo.tripway.posting.ui.PostPointPhotosFragment
import com.tiparo.tripway.profile.ui.ProfileFragment
import com.tiparo.tripway.trippage.ui.PointFragment
import com.tiparo.tripway.trippage.ui.TripDetailFragment
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class
    ]
)
interface AppComponent {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder

        fun builder(): AppComponent
    }

    fun inject(baseApplication: BaseApplication)

    fun inject(fragment: LoginFragment)
    fun inject(fragment: DiscoveryFragment)
    fun inject(fragment: ProfileFragment)
    fun inject(fragment: TripDetailFragment)
    fun inject(fragment: PointFragment)
    fun inject(fragment: PostPointListFragment)
    fun inject(fragment: PostPointMapFragment)
    fun inject(fragment: PostPointPhotosFragment)
    fun inject(fragment: PostPointDescriptionFragment)
}