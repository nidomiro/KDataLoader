package nidomiro.kdataloader

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.*

class AutoDispatcherDataLoaderImpl<K, R>(
    options: AutoDispatcherDataLoaderOptions<K, R>,
    batchLoader: BatchLoader<K, R>
) : SimpleDataLoaderImpl<K, R>(options, batchLoader) {

    private val autoChannel = Channel<Unit>()

    init {
        launch {
            var job: Job? = null
            for (msg in autoChannel) {
                job?.cancelAndJoin()
                job = launch {
                    delay(options.waitInterval)
                    if (isActive) {
                        dispatch()
                    }
                }
            }
        }
    }

    override suspend fun loadAsync(key: K): Deferred<R> {
        autoChannel.send(Unit)
        return super.loadAsync(key)
    }

    override suspend fun loadManyAsync(vararg keys: K): Deferred<List<R>> {
        autoChannel.send(Unit)
        return super.loadManyAsync(*keys)
    }

    override suspend fun dispatch() {
        autoChannel.send(Unit)
        super.dispatch()
    }

    override suspend fun clear(key: K) {
        autoChannel.send(Unit)
        super.clear(key)
    }

    override suspend fun clearAll() {
        autoChannel.send(Unit)
        super.clearAll()
    }

    override suspend fun prime(key: K, value: R) {
        autoChannel.send(Unit)
        super.prime(key, value)
    }

    override suspend fun prime(key: K, value: Throwable){
        autoChannel.send(Unit)
        super.prime(key, value)
    }

}
