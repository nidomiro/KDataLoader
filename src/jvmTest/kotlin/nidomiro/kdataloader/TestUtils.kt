package nidomiro.kdataloader

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout

fun runBlockingWithTimeout(millis: Long = 1000L, block: suspend CoroutineScope.() -> Unit): Unit = runBlocking {
    withTimeout(millis) {
        block()
    }
}

fun Int.isEven() = this % 2 == 0
fun Long.isEven() = this % 2 == 0L


fun log(msg: String) = println("[${Thread.currentThread().name}] $msg")