package nidomiro.kdataloader.statistics

interface StatisticsCollector {

    fun incLoadAsyncMethodCalled(): Long
    fun incLoadManyAsyncMethodCalled(): Long
    fun incDispatchMethodCalled(): Long
    fun incClearMethodCalled(): Long
    fun incClearAllMethodCalled(): Long
    fun incPrimeMethodCalled(): Long

    /**
     * Increment by the number of requested objects
     */
    fun incObjectsRequested(objects: Long = 1): Long

    fun incBatchCallsExecuted(): Long

    fun incCacheHitCount(): Long

    /**
     * returns a immutable copy of the statistics at the point of calling this method
     */
    fun createStatisticsSnapshot(): DataLoaderStatistics
}