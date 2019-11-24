package nidomiro.kdataloader

sealed class ExecutionResult<out T : Any> {
    data class Success<out T : Any>(val value: T) : ExecutionResult<T>()
    data class Failure(val throwable: Throwable) : ExecutionResult<Nothing>()
}