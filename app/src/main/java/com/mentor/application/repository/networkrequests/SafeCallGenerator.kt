package com.mentor.application.repository.networkrequests

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

/**
 * Internal class for creating and handling an api call.
 */
internal object SafeCallGenerator {
    suspend fun <T> safeApiCall(
        dispatcher: CoroutineDispatcher,
        apiCall: suspend () -> T
    ): ResultWrapper<T> {
        return withContext(dispatcher) {
            try {
                ResultWrapper.Success(apiCall.invoke())
            } catch (throwable: Throwable) {
                throwable.printStackTrace()
                when (throwable) {
                    is IOException ->
                        ResultWrapper.NetworkError
                    is HttpException -> {
                        ResultWrapper.GenericError
                    }
                    else -> {
                        ResultWrapper.GenericError
                    }
                }
            }
        }
    }

}