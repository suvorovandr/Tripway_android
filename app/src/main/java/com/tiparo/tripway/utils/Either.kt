package com.tiparo.tripway.utils

data class Either<L, R> private constructor(val isRight: Boolean, private val value: Any?) {
    val isLeft: Boolean
        get() = !isRight

    val left: L?
        get() {
            check(!isRight) { "Either is right" }
            return value as L?
        }

    val right: R?
        get() {
            check(isRight) { "Either is left" }
            return value as R?
        }

    override fun toString(): String {
        return String.format(if (isRight) "Either.right[%s]" else "Either.left[%s]", value)
    }

    companion object {
        private val LEFT_NULL: Either<*, *> = Either<Any, Any>(false, null)
        private val RIGHT_NULL: Either<*, *> = Either<Any, Any>(true, null)

        fun <L, R> left(value: L?): Either<L, R> {
            return if (value == null) {
                LEFT_NULL as Either<L, R>
            } else Either(false, value)
        }

        fun <L, R> right(value: R?): Either<L, R> {
            return if (value == null) {
                RIGHT_NULL as Either<L, R>
            } else Either(true, value)
        }
    }

}