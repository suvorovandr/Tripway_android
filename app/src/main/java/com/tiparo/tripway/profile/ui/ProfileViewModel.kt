package com.tiparo.tripway.profile.ui

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.tiparo.tripway.profile.domain.ProfileRepository
import com.tiparo.tripway.utils.RxUnit
import com.tiparo.tripway.utils.Transformers
import com.tiparo.tripway.utils.UnaryOperator
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject
import javax.inject.Inject

class ProfileViewModel @Inject constructor(private val profileRepository: ProfileRepository) : ViewModel() {
    val uiStateLiveData = MutableLiveData<ProfileUiState>()
    private val compositeDisposable = CompositeDisposable()

    private val initialIntent = PublishSubject.create<Any>()
    private val retryIntent = PublishSubject.create<Any>()

    init {
        val disposable = getProfile(profileRepository)
            .scan(ProfileUiState.idle(), { prevState, mutator ->
                mutator.apply(prevState)
            })
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe { value: ProfileUiState -> uiStateLiveData.postValue(value) }

        compositeDisposable.add(disposable)
    }

    fun loadProfile(userId: String?) {
        initialIntent.onNext(userId?: RxUnit.INSTANCE)
    }

    fun retryLoadProfile(userId: String?) {
        retryIntent.onNext(userId?: RxUnit.INSTANCE)
    }

    private fun getProfile(repository: ProfileRepository): Observable<UnaryOperator<ProfileUiState>> {
        return Observable.merge(initialIntent, retryIntent)
            .flatMap {
                val userId = if (it == RxUnit.INSTANCE) null else it as String
                repository.getProfile(userId)
                    .map { result ->
                        if (result.isRight) {
                            ProfileUiState.Mutators.discoveryData(result.right)
                        } else {
                            ProfileUiState.Mutators.discoveryError(result.left)
                        }
                    }
                    .compose(Transformers.startWithInMain(ProfileUiState.Mutators.discoveryLoading()))
            }
    }
}