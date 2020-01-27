package nidomiro.kdataloader.statistics

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.util.concurrent.atomic.AtomicLong

class SimpleStatisticsCollector : StatisticsCollector {

    private val internalStatistics = Statistics()

    private val statisticsScope = CoroutineScope(Dispatchers.Default)
    private var statisticsJobs = mutableListOf<Deferred<*>>()

    override fun incLoadAsyncMethodCalledAsync() =
        executeInStatisticsScopeAsync { internalStatistics.loadAsyncMethodCalled.incrementAndGet() }

    override fun incLoadManyAsyncMethodCalledAsync() =
        executeInStatisticsScopeAsync { internalStatistics.loadAsyncManyMethodCalled.incrementAndGet() }

    override fun incDispatchMethodCalledAsync() =
        executeInStatisticsScopeAsync { internalStatistics.dispatchMethodCalled.incrementAndGet() }

    override fun incClearMethodCalledAsync() =
        executeInStatisticsScopeAsync { internalStatistics.clearMethodCalled.incrementAndGet() }

    override fun incClearAllMethodCalledAsync() =
        executeInStatisticsScopeAsync { internalStatistics.clearAllMethodCalled.incrementAndGet() }

    override fun incPrimeMethodCalledAsync() =
        executeInStatisticsScopeAsync { internalStatistics.primeMethodCalled.incrementAndGet() }

    override fun incObjectsRequestedAsync(objects: Long) =
        executeInStatisticsScopeAsync { internalStatistics.objectsRequested.addAndGet(objects) }

    override fun incBatchCallsExecutedAsync() =
        executeInStatisticsScopeAsync { internalStatistics.batchCallsExecuted.incrementAndGet() }

    override fun incCacheHitCountAsync() =
        executeInStatisticsScopeAsync { internalStatistics.cacheHitCount.incrementAndGet() }

    private fun <T> executeInStatisticsScopeAsync(block: () -> T): Deferred<T> {
        val deferred = statisticsScope.async { block() }
        statisticsJobs.add(deferred)
        return deferred
    }

    override suspend fun createStatisticsSnapshot(): DataLoaderStatistics {
        val currentJobs = statisticsJobs
        statisticsJobs = mutableListOf()
        currentJobs.forEach { it.await() }
        return internalStatistics.snapshot()
    }


    internal data class Statistics(
        var loadAsyncMethodCalled: AtomicLong = AtomicLong(0),
        val loadAsyncManyMethodCalled: AtomicLong = AtomicLong(0),
        val dispatchMethodCalled: AtomicLong = AtomicLong(0),
        val clearMethodCalled: AtomicLong = AtomicLong(0),
        val clearAllMethodCalled: AtomicLong = AtomicLong(0),
        val primeMethodCalled: AtomicLong = AtomicLong(0),
        val objectsRequested: AtomicLong = AtomicLong(0),
        val batchCallsExecuted: AtomicLong = AtomicLong(0),
        val cacheHitCount: AtomicLong = AtomicLong(0)
    ) {
        fun snapshot() = DataLoaderStatistics(
            loadAsyncMethodCalled = this@Statistics.loadAsyncMethodCalled.get(),
            loadManyAsyncMethodCalled = this@Statistics.loadAsyncManyMethodCalled.get(),
            dispatchMethodCalled = this@Statistics.dispatchMethodCalled.get(),
            clearMethodCalled = this@Statistics.clearMethodCalled.get(),
            clearAllMethodCalled = this@Statistics.clearAllMethodCalled.get(),
            primeMethodCalled = this@Statistics.primeMethodCalled.get(),
            objectsRequested = this@Statistics.objectsRequested.get(),
            batchCallsExecuted = this@Statistics.batchCallsExecuted.get(),
            cacheHitCount = this@Statistics.cacheHitCount.get()
        )
    }
}