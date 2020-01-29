package nidomiro.kdataloader

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withTimeout

fun runBlockingWithTimeout(millis: Long = 1000L, block: suspend CoroutineScope.() -> Unit): Unit = runCoroutineTest {
    withTimeout(millis) {
        block()
    }
}

fun Int.isEven() = this % 2 == 0


expect fun log(msg: String)

/**
 * Workaround for suspending functions in tests
 */
expect fun runCoroutineTest(block: suspend (scope: CoroutineScope) -> Unit)
