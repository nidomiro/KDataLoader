package nidomiro.kdataloader.factories

import nidomiro.kdataloader.BatchLoader
import nidomiro.kdataloader.DataLoaderOptions
import nidomiro.kdataloader.ExecutionResult
import nidomiro.kdataloader.SimpleDataLoaderImpl

class SimpleDataLoaderFactory<K, R>(
    optionsFactory: () -> DataLoaderOptions<K, R>,
    cachePrimes: Map<K, ExecutionResult<R>>,
    batchLoader: BatchLoader<K, R>
) : DataLoaderFactory<K, R>(optionsFactory, batchLoader, cachePrimes, ::SimpleDataLoaderImpl)
