package nidomiro.kdataloader

import kotlinx.coroutines.CompletableDeferred

data class DataLoaderOptions<K, R>(
    val cache: Cache<K, CompletableDeferred<R>> = DefaultCacheImpl()
)