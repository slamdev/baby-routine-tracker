package com.github.slamdev.babyroutinetracker.model

/**
 * Represents the different states that UI components can be in
 */
sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val exception: Throwable, val message: String) : UiState<Nothing>()
    
    val isLoading: Boolean get() = this is Loading
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    
    fun getDataOrNull(): T? = if (this is Success) data else null
    fun getErrorOrNull(): Throwable? = if (this is Error) exception else null
    fun getErrorMessage(): String? = if (this is Error) message else null
}

/**
 * Represents states for optional data that might not exist
 */
sealed class OptionalUiState<out T> {
    object Loading : OptionalUiState<Nothing>()
    object Empty : OptionalUiState<Nothing>()
    data class Success<T>(val data: T) : OptionalUiState<T>()
    data class Error(val exception: Throwable, val message: String) : OptionalUiState<Nothing>()
    
    val isLoading: Boolean get() = this is Loading
    val isEmpty: Boolean get() = this is Empty
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    
    fun getDataOrNull(): T? = if (this is Success) data else null
    fun getErrorOrNull(): Throwable? = if (this is Error) exception else null
    fun getErrorMessage(): String? = if (this is Error) message else null
}

/**
 * Extension functions to help convert between different state types
 */
fun <T> UiState<T>.toOptionalUiState(): OptionalUiState<T> = when (this) {
    is UiState.Loading -> OptionalUiState.Loading
    is UiState.Success -> OptionalUiState.Success(data)
    is UiState.Error -> OptionalUiState.Error(exception, message)
}

fun <T> OptionalUiState<T>.toUiState(): UiState<T?> = when (this) {
    is OptionalUiState.Loading -> UiState.Loading
    is OptionalUiState.Empty -> UiState.Success(null)
    is OptionalUiState.Success -> UiState.Success(data)
    is OptionalUiState.Error -> UiState.Error(exception, message)
}
