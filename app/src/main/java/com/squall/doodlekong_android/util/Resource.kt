package com.squall.doodlekong_android.util

// A generic class that contains data and status about loading this data.
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
//    class Loading<T>(data: T? = null) : Resource<T>(data)
}
