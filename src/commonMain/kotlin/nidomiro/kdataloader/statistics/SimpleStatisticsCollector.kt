package nidomiro.kdataloader.statistics

import java.util.concurrent.atomic.AtomicLong

class SimpleStatisticsCollector : StatisticsCollector {

    private val internalStatistics = Statistics()

    override fun incLoadAsyncMethodCalled() = internalStatistics.loadAsyncMethodCalled.incrementAndGet()

    override fun incLoadManyAsyncMethodCalled() = internalStatistics.loadAsyncManyMethodCalled.incrementAndGet()

    override fun incDispatchMethodCalled() = internalStatistics.dispatchMethodCalled.incrementAndGet()

    override fun incClearMethodCalled() = internalStatistics.clearMethodCalled.incrementAndGet()

    override fun incClearAllMethodCalled() = internalStatistics.clearAllMethodCalled.incrementAndGet()

    override fun incPrimeMethodCalled() = internalStatistics.primeMethodCalled.incrementAndGet()

    override fun incObjectsRequested(objects: Long) = internalStatistics.objectsRequested.addAndGet(objects)

    override fun incBatchCallsExecuted() = internalStatistics.batchCallsExecuted.incrementAndGet()

    override fun incCacheHitCount() = internalStatistics.cacheHitCount.incrementAndGet()

    override fun createStatisticsSnapshot() = internalStatistics.snapshot()


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