package nidomiro.kdataloader

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.runBlocking


@Suppress("RedundantVisibilityModifier")
public class DataLoader<K, R>(
    @Suppress("MemberVisibilityCanBePrivate") val options: DataLoaderOptions<K, R> = DataLoaderOptions(),
    private val batchLoader: suspend (ids: List<K>) -> List<R>
) {

    private val queue: LoaderQueue<K, R> = DafaultLoaderQueueImpl()

    /**
     * Loads the value for the given Key.
     * The returned [Deferred] completes with the finish of [dispatch]
     */
    public suspend fun loadAsync(key: K): Deferred<R> {
        val deferred = options.cache.getOrCreate(key) { CompletableDeferred() }
        queue.enqueue(key, deferred)
        return deferred
    }

    /**
     * The functionality is equivalent to [loadAsync] but encapsulated in a [runBlocking]-Block for internal resource access.
     *
     * Use [loadAsync] to call from a coroutine.
     */
    @Suppress("DeferredIsResult")
    public fun load(key: K): Deferred<R> =
        runBlocking { loadAsync(key) }


    /**
     * Executes all stored requests via the given [batchLoader].
     * After this function finishes all [Deferred] created before are completed.
     */
    public suspend fun dispatch() {
        val values = queue.getAllItemsAsList().distinctBy { it.key }

        batchLoader(values.map { it.key })
            .forEachIndexed { i, result -> values[i].value.complete(result) }
    }


}
