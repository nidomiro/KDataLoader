package nidomiro.kdataloader

import kotlinx.coroutines.*
import nidomiro.kdataloader.statistics.SimpleStatisticsCollector
import nidomiro.kdataloader.statistics.StatisticsCollector

class SimpleDataLoaderImpl<K, R>(
    override val options: DataLoaderOptions<K, R>,
    private val statisticsCollector: StatisticsCollector,
    private val batchLoader: BatchLoader<K, R>
) : DataLoader<K, R> {
    constructor(options: DataLoaderOptions<K, R>, batchLoader: BatchLoader<K, R>) : this(
        options,
        SimpleStatisticsCollector(),
        batchLoader
    )

    constructor(batchLoader: BatchLoader<K, R>) : this(DataLoaderOptions(), batchLoader)

    private val dataLoaderScope = CoroutineScope(Dispatchers.Default)
    private val statisticsScope = CoroutineScope(Dispatchers.Default)
    private val queue: LoaderQueue<K, R> = DefaultLoaderQueueImpl()

    override suspend fun loadAsync(key: K): Deferred<R> {
        val statisticsJob = statisticsScope.launch {
            statisticsCollector.incLoadAsyncMethodCalled()
            statisticsCollector.incObjectsRequested()
        }

        val ret = internalLoadAsync(key)
        statisticsJob.join()
        return ret
    }

    private suspend fun internalLoadAsync(key: K): Deferred<R> {
        val block: suspend (key: K) -> CompletableDeferred<R> = {
            val newDeferred = CompletableDeferred<R>()
            queue.enqueue(key, newDeferred)
            if (!options.batchLoadEnabled) {
                dispatch()
            }
            newDeferred
        }

        return if (options.cacheEnabled) {
            options.cache.getOrCreate(key, block, { statisticsScope.launch { statisticsCollector.incCacheHitCount() } })
        } else {
            block(key)
        }
    }

    override suspend fun loadManyAsync(vararg keys: K): Deferred<List<R>> {
        val statisticsJob = statisticsScope.launch {
            statisticsCollector.incLoadManyAsyncMethodCalled()
            statisticsCollector.incObjectsRequested(keys.size.toLong())
        }
        val deferreds = keys.map { internalLoadAsync(it) }

        val ret = dataLoaderScope.async(Dispatchers.Default) {
            return@async deferreds.map { it.await() }
        }
        statisticsJob.join()
        return ret
    }

    override suspend fun dispatch() {
        val statisticsJob = statisticsScope.launch {
            statisticsCollector.incDispatchMethodCalled()
        }
        val queueEntries = if (options.cacheEnabled) {
            queue.getAllItemsAsList().distinctBy { it.key }
        } else {
            queue.getAllItemsAsList()
        }
        queueEntries.chunked(options.batchSize).forEach {
            executeDispatchOnQueueEntries(it)
        }
        statisticsJob.join()
    }

    private suspend fun executeDispatchOnQueueEntries(queueEntries: List<LoaderQueueEntry<K, CompletableDeferred<R>>>) {
        val keys = queueEntries.map { it.key }
        if (keys.isNotEmpty()) {
            executeBatchLoader(keys, queueEntries)
        }
    }

    private suspend fun executeBatchLoader(
        keys: List<K>,
        queueEntries: List<LoaderQueueEntry<K, CompletableDeferred<R>>>
    ) {
        val statisticsJob = statisticsScope.launch { statisticsCollector.incBatchCallsExecuted() }
        try {
            batchLoader(keys).forEachIndexed { i, result ->
                val queueEntry = queueEntries[i]
                handleSingleBatchLoaderResult(result, queueEntry)
            }
        } catch (e: Throwable) {
            handleCompleteBatchLoaderFailure(queueEntries, e)
        }
        statisticsJob.join()
    }

    private suspend fun handleSingleBatchLoaderResult(
        result: ExecutionResult<R>,
        queueEntry: LoaderQueueEntry<K, CompletableDeferred<R>>
    ) {
        when (result) {
            is ExecutionResult.Success -> queueEntry.value.complete(result.value)
            is ExecutionResult.Failure -> {
                queueEntry.value.completeExceptionally(result.throwable)
                if (!options.cacheExceptions) {
                    clear(queueEntry.key)
                }
            }

        }
    }

    private suspend fun handleCompleteBatchLoaderFailure(
        queueEntries: List<LoaderQueueEntry<K, CompletableDeferred<R>>>,
        e: Throwable
    ) {
        queueEntries.forEach {
            clear(it.key)
            it.value.completeExceptionally(e)
        }
    }

    override suspend fun clear(key: K) {
        val statisticsJob = statisticsScope.launch {
            statisticsCollector.incClearMethodCalled()
        }
        options.cache.clear(key)
        statisticsJob.join()
    }

    override suspend fun clearAll() {
        val statisticsJob = statisticsScope.launch {
            statisticsCollector.incClearAllMethodCalled()
        }
        options.cache.clear()
        statisticsJob.join()
    }

    override suspend fun prime(key: K, value: R) {
        val statisticsJob = statisticsScope.launch {
            statisticsCollector.incPrimeMethodCalled()
        }
        options.cache.getOrCreate(key) {
            CompletableDeferred(value)
        }
        statisticsJob.join()
    }

    override suspend fun prime(key: K, value: Throwable) {
        val statisticsJob = statisticsScope.launch {
            statisticsCollector.incPrimeMethodCalled()
        }
        options.cache.getOrCreate(key) {
            CompletableDeferred<R>().apply {
                completeExceptionally(value)
            }
        }
        statisticsJob.join()
    }

    override fun createStatisticsSnapshot() = statisticsCollector.createStatisticsSnapshot()

}
