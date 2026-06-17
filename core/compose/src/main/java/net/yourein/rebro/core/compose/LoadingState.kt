package net.yourein.rebro.core.compose

import androidx.compose.runtime.Immutable

@Immutable
sealed class LoadingState<out T> {
    abstract val value: T?

    @Immutable
    data class Loading<T>(
        override val value: T?
    ) : LoadingState<T>()

    @Immutable
    data class Success<T>(
        override val value: T,
    ) : LoadingState<T>()

    @Immutable
    data class Error<T>(
        override val value: T?,
        val error: Throwable,
    ) : LoadingState<T>()
}

fun <T> LoadingState<T>.asLoading(): LoadingState.Loading<T> {
    return when(this) {
        is LoadingState.Loading -> this
        is LoadingState.Success -> LoadingState.Loading(this.value)
        is LoadingState.Error -> LoadingState.Loading(this.value)
    }
}

fun <T> LoadingState<T>.asError(e: Throwable): LoadingState.Error<T> {
    return when(this) {
        is LoadingState.Loading -> LoadingState.Error(this.value, e)
        is LoadingState.Success -> LoadingState.Error(this.value, e)
        is LoadingState.Error -> this
    }
}