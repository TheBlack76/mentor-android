package com.mentor.application.repository.networkrequests

import retrofit2.Response


interface NetworkRequestCallbacks<T> {

    fun onSuccess(response: Response<*>)

    fun onError(errorThrow: Int)

    fun onNetworkError(noInternet: Int)

}