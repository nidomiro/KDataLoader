package nidomiro.kdataloader.dsl

import kotlinx.coroutines.CompletableDeferred
import nidomiro.kdataloader.Cache
import nidomiro.kdataloader.DataLoaderOptions
import nidomiro.kdataloader.DefaultCacheImpl

class DataLoaderOptionsDSL<K, R> {
    /**
     * The cache implementation
     */
    var cache: Cache<K, CompletableDeferred<R>> = DefaultCacheImpl()

    /**
     * Cache Results?
     */
    var cacheEnabled: Boolean = true

    /**
     * Cache Exceptional States?
     */
    var cacheExceptions: Boolean = true

    /**
     * Load individually (and immediately) or in batch
     */
    var batchLoadEnabled: Boolean = true

    /**
     * The maximum size of one batch
     */
    var batchSize: Int = Int.MAX_VALUE

    internal fun toDataLoaderOptions() = DataLoaderOptions(
        cache,
        cacheExceptions,
        cacheEnabled,
        batchLoadEnabled,
        batchSize
    )

}