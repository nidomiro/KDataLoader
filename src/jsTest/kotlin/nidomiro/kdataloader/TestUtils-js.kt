package nidomiro.kdataloader

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise


actual fun log(msg: String) = println(" $msg")

/**
 * Workaround for suspending functions in tests
 */
actual fun runCoroutineTest(block: suspend (scope: CoroutineScope) -> Unit): dynamic =
    GlobalScope.promise { block(this) }