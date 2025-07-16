package com.mentor.application.repository.networkrequests

import com.mentor.application.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject

/**
 * Internal utility class for handling api response.
 */
class ApiUtil @Inject constructor() {
    /**
     * Generic function which handles the response from api call.
     */
    fun <T> fetchResource(
        apiCall: suspend () -> ResultWrapper<Response<T>>,
        callback: NetworkRequestCallbacks<T>
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            val response = apiCall.invoke()
            withContext(Dispatchers.Main) {
                when (response) {
                    is ResultWrapper.NetworkError -> {
                        callback.onNetworkError(R.string.no_internet)
                    }
                    is ResultWrapper.GenericError -> {
                        callback.onError(R.string.retrofit_failure)
                    }
                    is ResultWrapper.Success -> {
                        callback.onSuccess(response.value)
                    }

                }
            }
        }
    }

}