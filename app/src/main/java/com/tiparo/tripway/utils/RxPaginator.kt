package com.tiparo.tripway.utils

import android.util.Pair
import io.reactivex.Single
import io.reactivex.functions.Function
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.Subject
import timber.log.Timber

class RxPaginator<TPage, TParams>(
    val pageFetcher: BiFunction<TParams, String?, Single<TPage>>,
    val tokenFromPage: Function<TPage, PageToken>
) {
    private val state: Subject<Pair<TParams, PageToken>> =
        BehaviorSubject.create<Pair<TParams, PageToken>>().toSerialized()

    fun getFirstPage(params: TParams): Single<Either<Throwable, TPage>> {
        return Single
            .just(Pair<TParams, PageToken>(params, PageToken.FIRST))
            .flatMap<Either<Throwable, TPage>> { s: Pair<TParams, PageToken> ->
                fetch(s.first, s.second)
            }
    }

    fun nextPage(): Single<Either<Throwable, TPage>> =
        state
            .take(1)
            .switchMapSingle<Either<Throwable, TPage>> { s: Pair<TParams, PageToken> ->
                fetch(s.first, s.second)
            }
            .singleOrError()

    private fun fetch(
        params: TParams,
        token: PageToken
    ): Single<Either<Throwable, TPage>> {
        return if (token.hasMore) {
            pageFetcher.apply(params, token.anchor)
                .doOnSuccess { page ->
                    val newToken: PageToken = tokenFromPage.apply(page)
                    if (newToken.anchor != null) {
                        state.onNext(Pair(params, newToken))
                    }
                }
                .doOnError { th ->
                    Timber.e(th, "Error while fetching page for params $params, token $token")
                }
                .compose(Transformers.neverThrowS())
        } else {
            Single.just(Either.left<Throwable, TPage>(IllegalStateException("No more pages: params $params, token $token")))
        }
    }
}