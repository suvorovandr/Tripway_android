package com.tiparo.tripway.utils;

import android.os.Looper;

import androidx.annotation.NonNull;

import io.reactivex.Completable;
import io.reactivex.CompletableTransformer;
import io.reactivex.MaybeTransformer;
import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.SingleTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.internal.schedulers.ImmediateThinScheduler;

public class Transformers {
    public static <T> ObservableTransformer<T, T> startWithInMain(T item) {
        return upstream -> upstream.startWith(
                Observable.just(item).observeOn(
                        Looper.getMainLooper() == Looper.myLooper()
                                ? ImmediateThinScheduler.INSTANCE
                                : AndroidSchedulers.mainThread()));
    }

    @NonNull
    public static <T> ObservableTransformer<T, Either<Throwable, T>> neverThrowO() {
        return upstream -> upstream
                .map(Either.Companion::<Throwable, T>right)
                .onErrorReturn(Either.Companion::left);
    }

    @NonNull
    public static <T> SingleTransformer<T, Either<Throwable, T>> neverThrowS() {
        return upstream -> upstream
                .map(Either.Companion::<Throwable, T>right)
                .onErrorReturn(Either.Companion::left);
    }
}
