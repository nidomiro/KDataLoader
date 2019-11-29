package nidomiro.kdataloader

import kotlinx.coroutines.*

typealias BatchLoader<K, R> = suspend (ids: List<K>) -> List<ExecutionResult<R>>

@Suppress("RedundantVisibilityModifier")
public class DataLoader<K, R>(
    @Suppress("MemberVisibilityCanBePrivate") val options: DataLoaderOptions<K, R>,
    private val batchLoader: BatchLoader<K, R>
) {
    constructor(batchLoader: BatchLoader<K, R>) : this(DataLoaderOptions(), batchLoader)

    private val dataLoaderScope = CoroutineScope(Dispatchers.Default)
    private val queue: LoaderQueue<K, R> = DefaultLoaderQueueImpl()

    /**
     * Loads the value for the given Key.
     * The returned [Deferred] completes with the finish of [dispatch] in case of [DataLoaderOptions.batchLoadEnabled] = true.
     * If [DataLoaderOptions.batchLoadEnabled] = false it calls the BatchLoader immediately and returns the retrieved value.
     */
    public suspend fun loadAsync(key: K): Deferred<R> {
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

    /**
     * The same as [loadAsync] but for multiple Keys at once.
     */
    public suspend fun loadManyAsync(vararg keys: K): Deferred<List<R>> {
        val deferreds = keys.map { loadAsync(it) }

        return dataLoaderScope.async(Dispatchers.Default) {
            return@async deferreds.map { it.await() }
        }
    }


    /**
     * Executes all stored requests via the given [batchLoader].
     * After this function finishes all [Deferred] created before are completed.
     */
    public suspend fun dispatch() {
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

    /**
     * Removes the value of the given Key from the cache
     */
    suspend fun clear(key: K) {
        options.cache.clear(key)
    }

    /**
     * Removes all values from the cache
     */
    suspend fun clearAll() {
        options.cache.clear()
    }

    /**
     * Primes the cache with the given values.
     * After priming the [BatchLoader] will not be called with this key.
     */
    suspend fun prime(key: K, value: R) {
        options.cache.getOrCreate(key) {
            CompletableDeferred(value)
        }
    }

    /**
     * Primes the cache with the given [Throwable].
     * After priming the [BatchLoader] will not be called with this key, if [DataLoaderOptions.cacheExceptions] = true.
     */
    suspend fun prime(key: K, value: Throwable) {
        options.cache.getOrCreate(key) {
            CompletableDeferred<R>().apply {
                completeExceptionally(value)
            }
        }
    }

    internal suspend fun prime(key: K, value: ExecutionResult<R>) =
        when (value) {
            is ExecutionResult.Success -> prime(key, value.value)
            is ExecutionResult.Failure -> prime(key, value.throwable)
        }


}

/**
 * @see DataLoader.prime(K, R)
 */
suspend fun <K, R> DataLoader<K, R>.prime(cacheEntry: Pair<K, R>) {
    prime(cacheEntry.first, cacheEntry.second)
}

/**
 * @see DataLoader.prime(K, Throwable)
 */
@JvmName("primeFailure")
suspend fun <K, R> DataLoader<K, R>.prime(cacheEntry: Pair<K, Throwable>) {
    prime(cacheEntry.first, cacheEntry.second)
}
