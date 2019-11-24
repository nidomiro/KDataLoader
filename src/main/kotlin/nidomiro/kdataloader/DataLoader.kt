package nidomiro.kdataloader

import kotlinx.coroutines.*

typealias BatchLoader<K, R> = suspend (ids: List<K>) -> List<R>

@Suppress("RedundantVisibilityModifier")
public class DataLoader<K, R>(
    private val batchLoader: BatchLoader<K, R>,
    @Suppress("MemberVisibilityCanBePrivate") val options: DataLoaderOptions<K, R>
) {
    constructor(batchLoader: BatchLoader<K, R>) : this(batchLoader, DataLoaderOptions())

    val dataLoaderScope = CoroutineScope(Dispatchers.Default)


    private val queue: LoaderQueue<K, R> = DafaultLoaderQueueImpl()

    /**
     * Loads the value for the given Key.
     * The returned [Deferred] completes with the finish of [dispatch]
     */
    public suspend fun loadAsync(key: K): Deferred<R> {
        return options.cache.getOrCreate(key) {
            val newDeferred = CompletableDeferred<R>()
            queue.enqueue(key, newDeferred)
            return@getOrCreate newDeferred
        }
    }


    public suspend fun loadManyAsync(vararg keys: K): Deferred<List<R>> {
        val deferreds = keys.map { loadAsync(it) }

        return dataLoaderScope.async {
            return@async deferreds.map { it.await() }
        }
    }


    /**
     * Executes all stored requests via the given [batchLoader].
     * After this function finishes all [Deferred] created before are completed.
     */
    public suspend fun dispatch() {
        val values = queue.getAllItemsAsList().distinctBy { it.key }
        val keys = values.map { it.key }
        if (keys.isNotEmpty()) {
            batchLoader(keys)
                .forEachIndexed { i, result -> values[i].value.complete(result) }
        }
    }


}
