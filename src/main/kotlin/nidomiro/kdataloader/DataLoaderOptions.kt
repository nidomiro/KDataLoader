package nidomiro.kdataloader

import kotlinx.coroutines.CompletableDeferred

data class DataLoaderOptions<K, R>(
    /**
     * The cache implementation
     */
    val cache: Cache<K, CompletableDeferred<R>> = DefaultCacheImpl(),

    /**
     * Cache Exceptional States?
     */
    val cacheExceptions: Boolean = true,

    /**
     * Cache Results?
     */
    val cacheEnabled: Boolean = true,

    /**
     * Load individually (and immediately) or in batch
     */
    val batchLoadEnabled: Boolean = true,

    /**
     * The maximum size of one batch
     */
    val batchSize: Int = Int.MAX_VALUE
)