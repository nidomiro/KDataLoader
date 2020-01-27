package nidomiro.kdataloader.statistics

import kotlinx.coroutines.Deferred

interface StatisticsCollector {

    fun incLoadAsyncMethodCalledAsync(): Deferred<Long>
    fun incLoadManyAsyncMethodCalledAsync(): Deferred<Long>
    fun incDispatchMethodCalledAsync(): Deferred<Long>
    fun incClearMethodCalledAsync(): Deferred<Long>
    fun incClearAllMethodCalledAsync(): Deferred<Long>
    fun incPrimeMethodCalledAsync(): Deferred<Long>

    /**
     * Increment by the number of requested objects
     */
    fun incObjectsRequestedAsync(objects: Long = 1): Deferred<Long>

    fun incBatchCallsExecutedAsync(): Deferred<Long>

    fun incCacheHitCountAsync(): Deferred<Long>

    /**
     * returns a immutable copy of the statistics at the point of calling this method
     */
    suspend fun createStatisticsSnapshot(): DataLoaderStatistics
}