package nidomiro.kdataloader.factories

import kotlinx.coroutines.Job
import nidomiro.kdataloader.BatchLoader
import nidomiro.kdataloader.DataLoaderOptions
import nidomiro.kdataloader.ExecutionResult
import nidomiro.kdataloader.TimedAutoDispatcherImpl
import nidomiro.kdataloader.TimedAutoDispatcherDataLoaderOptions

class TimedAutoDispatcherDataLoaderFactory<K, R>(
    optionsFactory: () -> TimedAutoDispatcherDataLoaderOptions<K, R>,
    cachePrimes: Map<K, ExecutionResult<R>>,
    batchLoader: BatchLoader<K, R>,
    parent: Job?
) : DataLoaderFactory<K, R>(optionsFactory, batchLoader, cachePrimes, { _: DataLoaderOptions<K, R>, bl: BatchLoader<K, R> ->
    TimedAutoDispatcherImpl(optionsFactory(), bl, parent)
})
