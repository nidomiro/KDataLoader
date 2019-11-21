package nidomiro.kdataloader

import kotlinx.coroutines.CompletableDeferred


@Suppress("RedundantVisibilityModifier")
public class DataLoader<T, R>(val batchLoader: suspend (ids: List<T>) -> List<R>) {

    private val batch: MutableMap<T, Pair<T, CompletableDeferred<R>>> = mutableMapOf()


    public fun load(key: T): CompletableDeferred<R> {
        val completableDeferredMapping = batch.computeIfAbsent(key) { key to CompletableDeferred() }
        return completableDeferredMapping.second
    }

    public suspend fun dispatch() {
        val entries = batch.values.toList()
        val completables = entries.map { it.second }

        batchLoader(entries.map { it.first })
            .forEachIndexed { i, result -> completables[i].complete(result) }
    }
}
