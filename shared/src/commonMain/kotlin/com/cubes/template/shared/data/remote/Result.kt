package com.cubes.template.shared.data.remote

sealed class Result<out T : Any> {

    data class Success<T : Any>(val data: T) : Result<T>()
    data class Error(val ex: Exception) : Result<Nothing>()

    fun <R : Any> onSuccess(action: (T) -> R): Result<R> = when (this) {
        is Error -> this
        is Success -> Success(action(this.data))
    }

    fun onError(action: (Exception) -> Exception): Result<T> = when (this) {
        is Error -> Error(action(this.ex))
        is Success -> this
    }

    suspend fun <R : Any> onSuccessSuspend(action: suspend (T) -> Result<R>): Result<R> = when (this) {
        is Error -> this
        is Success -> action(this.data)
    }
}

fun <T : Any> Result<T>.getDataOrElse(or: () -> T): T = when (this) {
    is Result.Error -> or()
    is Result.Success -> this.data
}

fun <T : Any> Result<T>.getDataOrNull(): T? = (this as? Result.Success)?.data
