package dev.vibebridge.core

sealed interface VbResult<out T> {
    data class Ok<T>(val value: T) : VbResult<T>
    data class Err(val message: String, val http: Int = 0) : VbResult<Nothing>
}

inline fun <T, R> VbResult<T>.map(f: (T) -> R): VbResult<R> = when (this) {
    is VbResult.Ok -> VbResult.Ok(f(value))
    is VbResult.Err -> this
}

fun <T> VbResult<T>.getOrNull(): T? = (this as? VbResult.Ok)?.value

fun <T> VbResult<T>.errorOrNull(): String? = (this as? VbResult.Err)?.message

val <T> VbResult<T>.isOk: Boolean get() = this is VbResult.Ok
