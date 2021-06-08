package nidomiro.kdataloader.factories

import nidomiro.kdataloader.BatchLoader
import nidomiro.kdataloader.DataLoaderOptions
import nidomiro.kdataloader.ExecutionResult
import nidomiro.kdataloader.AutoDispatcherDataLoaderImpl
import nidomiro.kdataloader.AutoDispatcherDataLoaderOptions

class AutoDispatcherDataLoaderFactory<K, R>(
    optionsFactory: () -> AutoDispatcherDataLoaderOptions<K, R>,
    cachePrimes: Map<K, ExecutionResult<R>>,
    batchLoader: BatchLoader<K, R>
) : DataLoaderFactory<K, R>(optionsFactory, batchLoader, cachePrimes, { _: DataLoaderOptions<K, R>, bl: BatchLoader<K, R> ->
    AutoDispatcherDataLoaderImpl(optionsFactory(), bl)
})

