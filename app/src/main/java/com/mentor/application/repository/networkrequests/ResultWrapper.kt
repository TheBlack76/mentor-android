package com.mentor.application.repository.networkrequests

/**
 * Internal sealed class for containing all the possible responses from server.
 */
sealed class ResultWrapper<out T> {
    data class Success<out T>(val value: T) : ResultWrapper<T>()

    data object GenericError :
        ResultWrapper<Nothing>()

    data object NetworkError : ResultWrapper<Nothing>()
}