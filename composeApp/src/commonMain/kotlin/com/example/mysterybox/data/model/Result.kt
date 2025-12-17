package com.example.mysterybox.data.model

sealed class ApiError {
    data class HttpError(val code: Int, val message: String) : ApiError()
    data class NetworkError(val message: String, val cause: Throwable? = null) : ApiError()
    data class AuthenticationError(val message: String) : ApiError()
    data class NotFoundError(val message: String) : ApiError()
    data class ValidationError(val message: String) : ApiError()
    data object UnknownError : ApiError()

    fun toMessage(): String = when (this) {
        is HttpError -> "HTTP $code: $message"
        is NetworkError -> message
        is AuthenticationError -> message
        is NotFoundError -> message
        is ValidationError -> message
        is UnknownError -> "Unknown error occurred"
    }
}

sealed class Result<out T> {
    data class Success<T>(val data: T) : Result<T>()
    data class Error(val error: ApiError) : Result<Nothing>()

    inline fun <R> map(transform: (T) -> R): Result<R> = when (this) {
        is Success -> Success(transform(data))
        is Error -> this
    }

    inline fun onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Success) action(data)
        return this
    }

    inline fun onError(action: (ApiError) -> Unit): Result<T> {
        if (this is Error) action(error)
        return this
    }

    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }

    companion object {
        fun error(message: String): Error = Error(ApiError.HttpError(0, message))
    }
}
