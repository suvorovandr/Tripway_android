package com.tiparo.tripway.login.domain

import androidx.lifecycle.LiveData
import com.tiparo.tripway.AppExecutors
import com.tiparo.tripway.models.AuthResponse
import com.tiparo.tripway.repository.NetworkBoundResource
import com.tiparo.tripway.utils.Resource
import com.tiparo.tripway.repository.network.api.ApiResponse
import com.tiparo.tripway.repository.network.api.services.AuthService
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val authService: AuthService,
    private val appExecutors: AppExecutors
) {
    fun authUser(tokenId: String): LiveData<Resource<AuthResponse>> {
        return object : NetworkBoundResource<AuthResponse, AuthResponse>(appExecutors) {
            override fun createCall(): LiveData<ApiResponse<AuthResponse>> {
                return authService.authBackend(tokenId)
            }
        }.asLiveData()
    }

    fun createUser(email: String, nickname: String, password: String): Completable {
        return authService.createUser(email, nickname, password)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}