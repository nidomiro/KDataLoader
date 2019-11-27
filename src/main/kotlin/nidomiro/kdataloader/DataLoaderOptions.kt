package nidomiro.kdataloader

import kotlinx.coroutines.CompletableDeferred

data class DataLoaderOptions<K, R>(
    val cache: Cache<K, CompletableDeferred<R>> = DefaultCacheImpl(),
    val cacheExceptions: Boolean = true,
    val cacheEnabled: Boolean = true,
    val batchLoadEnabled: Boolean = true,
    val batchSize: Int = Int.MAX_VALUE
)