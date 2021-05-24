import RetrofitException.Companion.asRetrofitException
import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.ObservableTransformer
import io.reactivex.Single
import io.reactivex.SingleTransformer
import io.reactivex.functions.Function
import retrofit2.*
import retrofit2.adapter.rxjava2.Result
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import java.io.IOException
import java.lang.reflect.Type

class RxErrorHandlingCallAdapterFactory private constructor() : CallAdapter.Factory() {
    private val original = RxJava2CallAdapterFactory.create()

    override fun get(
        returnType: Type,
        annotations: Array<Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        return RxCallAdapterWrapper(original.get(returnType, annotations, retrofit) ?: return null)
    }

    private class RxCallAdapterWrapper<R>(private val wrapped: CallAdapter<R, *>) :
        CallAdapter<R, Any> {

        override fun responseType(): Type {
            return wrapped.responseType()
        }

        override fun adapt(call: Call<R>): Any {
            val result = wrapped.adapt(call)
            return when (result) {
                is Single<*> -> {
                    result.compose { upstream: Single<out Any> ->
                        upstream.flatMap {
                            val result = it as Result<R>
                            result.response()
                            return@flatMap Single.just("d")
                        }
                    }
                }
                is Observable<*> -> result.onErrorResumeNext(Function { throwable ->
                    Observable.error(
                        asRetrofitException(throwable)
                    )
                })
                is Completable -> result.onErrorResumeNext(Function { throwable ->
                    Completable.error(
                        asRetrofitException(throwable)
                    )
                })
                else -> result
            }
        }
    }

    companion object {
        fun create(): CallAdapter.Factory {
            return RxErrorHandlingCallAdapterFactory()
        }
    }
}

class RetrofitException
private constructor(
    message: String?,
    /**
     * The request URL which produced the error.
     */
    val url: String?,
    /**
     * Response object containing status code, headers, body, etc.
     */
    val response: Response<*>?,
    /**
     * The event kind which triggered this error.
     */
    val kind: Kind,
    exception: Throwable
) : RuntimeException(message, exception) {

    override fun toString(): String {
        return super.toString() + " : " + kind + " : " + url + " : " + response?.errorBody()
            ?.string()
    }

    /**
     * Identifies the event kind which triggered a [RetrofitException].
     */
    enum class Kind {
        /**
         * An [IOException] occurred while communicating to the server.
         */
        NETWORK,

        /**
         * A non-200 HTTP status code was received from the server.
         */
        HTTP,

        /**
         * An internal error occurred while attempting to execute a request. It is best practice to
         * re-throw this exception so your application crashes.
         */
        UNEXPECTED
    }

    companion object {
        fun httpError(
            url: String,
            response: Response<*>,
            httpException: HttpException
        ): RetrofitException {
            val message = response.code().toString() + " " + response.message()
            return RetrofitException(message, url, response, Kind.HTTP, httpException)
        }

        fun networkError(exception: IOException): RetrofitException {
            return RetrofitException(exception.message, null, null, Kind.NETWORK, exception)
        }

        fun unexpectedError(exception: Throwable): RetrofitException {
            return RetrofitException(exception.message, null, null, Kind.UNEXPECTED, exception)
        }

        fun asRetrofitException(throwable: Throwable): RetrofitException {
            if (throwable is RetrofitException) {
                return throwable
            }
            // We had non-200 http error
            if (throwable is HttpException) {
                val response = throwable.response()
                return httpError(response?.raw()?.request?.url.toString(), response!!, throwable)
            }
            // A network error happened
            return if (throwable is IOException) {
                networkError(throwable)
            } else unexpectedError(throwable)
            // We don't know what happened. We need to simply convert to an unknown error
        }
    }
}