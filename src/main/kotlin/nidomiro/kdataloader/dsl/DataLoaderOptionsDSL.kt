package nidomiro.kdataloader.dsl

import kotlinx.coroutines.CompletableDeferred
import nidomiro.kdataloader.Cache
import nidomiro.kdataloader.DataLoaderOptions
import nidomiro.kdataloader.DefaultCacheImpl

class DataLoaderOptionsDSL<K, R> {
    var cache: Cache<K, CompletableDeferred<R>> = DefaultCacheImpl()
    var cacheExceptions: Boolean = true
    var cacheEnabled: Boolean = true
    var batchLoadEnabled: Boolean = true
    var batchSize: Int = Int.MAX_VALUE

    fun toDataLoaderOptions() = DataLoaderOptions(
        cache,
        cacheExceptions,
        cacheEnabled,
        batchLoadEnabled,
        batchSize
    )

}