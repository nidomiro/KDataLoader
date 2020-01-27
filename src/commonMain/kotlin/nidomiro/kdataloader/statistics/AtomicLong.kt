package nidomiro.kdataloader.statistics

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class AtomicLong(private var value: Long = 0) {

    private val mutex = Mutex()

    suspend fun incrementAndGet(): Long {
        return mutex.withLock { value++ }
    }

    suspend fun addAndGet(offset: Long): Long {
        return mutex.withLock {
            value += offset
            return@withLock value
        }
    }

    fun get(): Long {
        return value
    }
}