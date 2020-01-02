package nidomiro.kdataloader

import kotlinx.coroutines.*

class SimpleDataLoaderImpl<K, R>(
    override val options: DataLoaderOptions<K, R>,
    private val batchLoader: BatchLoader<K, R>
) : DataLoader<K, R> {
    constructor(batchLoader: BatchLoader<K, R>) : this(DataLoaderOptions(), batchLoader)

    private val dataLoaderScope = CoroutineScope(Dispatchers.Default)
    private val queue: LoaderQueue<K, R> = DefaultLoaderQueueImpl()

    override suspend fun loadAsync(key: K): Deferred<R> {
        val block: suspend (key: K) -> CompletableDeferred<R> = {
            val newDeferred = CompletableDeferred<R>()
            queue.enqueue(key, newDeferred)
            if (!options.batchLoadEnabled) {
                dispatch()
            }
            newDeferred
        }

        return if (options.cacheEnabled) {
            options.cache.getOrCreate(key, block)
        } else {
            block(key)
        }
    }

    override suspend fun loadManyAsync(vararg keys: K): Deferred<List<R>> {
        val deferreds = keys.map { loadAsync(it) }

        return dataLoaderScope.async(Dispatchers.Default) {
            return@async deferreds.map { it.await() }
        }
    }

    override suspend fun dispatch() {
        val queueEntries = if (options.cacheEnabled) {
            queue.getAllItemsAsList().distinctBy { it.key }
        } else {
            queue.getAllItemsAsList()
        }
        queueEntries.chunked(options.batchSize).forEach {
            executeDispatchOnQueueEntries(it)
        }
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
        try {
            batchLoader(keys).forEachIndexed { i, result ->
                val queueEntry = queueEntries[i]
                handleSingleBatchLoaderResult(result, queueEntry)
            }
        } catch (e: Throwable) {
            handleCompleteBatchLoaderFailure(queueEntries, e)
        }
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
        options.cache.clear(key)
    }

    override suspend fun clearAll() {
        options.cache.clear()
    }

    override suspend fun prime(key: K, value: R) {
        options.cache.getOrCreate(key) {
            CompletableDeferred(value)
        }
    }

    override suspend fun prime(key: K, value: Throwable) {
        options.cache.getOrCreate(key) {
            CompletableDeferred<R>().apply {
                completeExceptionally(value)
            }
        }
    }

}
