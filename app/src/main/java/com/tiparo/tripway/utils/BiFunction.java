package com.tiparo.tripway.utils;

public interface BiFunction<T, U, R> {
    /**
     * @see java.util.function.BiFunction#apply(Object, Object)
     */

    R apply(T t, U u);
}